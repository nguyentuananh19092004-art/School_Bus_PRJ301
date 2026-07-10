package controller;

import dal.ScheduleDAO;
import dal.BusDAO;
import dal.UserDAO;
import model.Schedule;
import model.Bus;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DriverActionServlet", urlPatterns = {"/driver-action"})
public class DriverActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"taixe".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String action = request.getParameter("action");
        int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
        
        String busIdParam = request.getParameter("busID");
        int busID = 0;
        if (busIdParam != null && !busIdParam.isEmpty()) {
            busID = Integer.parseInt(busIdParam);
        }

        ScheduleDAO scheduleDAO = new ScheduleDAO();
        BusDAO busDAO = new BusDAO();

        if ("start_moving".equals(action)) {
            Bus bus = busDAO.getBusById(busID);
            if (bus != null && "Bảo dưỡng/Sửa chữa".equals(bus.getStatus())) {
                session.setAttribute("errorMessage", "Xe đang trong tình trạng bảo dưỡng hoặc hỏng, không thể khởi hành!");
                response.sendRedirect("driver-dashboard");
                return;
            }

            // Update Schedule Status to PREPARING
            updateScheduleStatus(scheduleID, "PREPARING", scheduleDAO);
            // Optional: Update Bus Status to Hoạt động early
            updateBusStatus(busID, "Hoạt động", busDAO);
        } else if ("start_trip".equals(action)) {
            Bus bus = busDAO.getBusById(busID);
            if (bus != null && "Bảo dưỡng/Sửa chữa".equals(bus.getStatus())) {
                session.setAttribute("errorMessage", "Xe đang trong tình trạng bảo dưỡng hoặc hỏng, không thể bắt đầu hành trình!");
                response.sendRedirect("driver-dashboard");
                return;
            }

            // Update Schedule Status to IN_PROGRESS
            updateScheduleStatus(scheduleID, "IN_PROGRESS", scheduleDAO);
        } else if ("report_incident".equals(action)) {
            // Update Schedule IncidentStatus to INCIDENT
            updateScheduleIncidentStatus(scheduleID, "INCIDENT", scheduleDAO);
            
            // Notification can also be sent to Technician here, but UI Dashboard handles reading IncidentStatus.
        } else if ("complete_trip".equals(action)) {
            String busCondition = request.getParameter("busCondition");
            String note = request.getParameter("note");
            
            // Update Schedule Status to COMPLETED
            updateScheduleStatus(scheduleID, "COMPLETED", scheduleDAO);
            
            if ("OK".equals(busCondition)) {
                updateBusStatus(busID, "Sẵn sàng", busDAO);
            } else {
                updateBusStatus(busID, "Bảo dưỡng/Sửa chữa", busDAO);
                String desc = note != null && !note.isEmpty() ? "Báo hỏng: " + note : "Cần bảo dưỡng";
                updateScheduleIncidentStatus(scheduleID, desc, scheduleDAO);
                Schedule currentSch = scheduleDAO.getScheduleById(scheduleID);
                java.sql.Date scheduleDate = currentSch != null ? currentSch.getDate() : new java.sql.Date(System.currentTimeMillis());
                busDAO.insertBusMaintenance(busID, scheduleDate, desc);
                notifyAdminForFutureSchedules(busID, scheduleID, scheduleDate, scheduleDAO, busDAO);
            }
        } else if ("switch_bus".equals(action)) {
            int newBusID = Integer.parseInt(request.getParameter("newBusID"));
            int oldBusID = Integer.parseInt(request.getParameter("oldBusID"));
            // 1. applyBusReplacement in ScheduleDAO
            scheduleDAO.applyBusReplacement(scheduleID, newBusID, oldBusID);
            // 2. update new bus status to Hoạt động
            updateBusStatus(newBusID, "Hoạt động", busDAO);
            
            // 3. Tự động đưa xe hỏng cũ vào danh sách bảo dưỡng
            updateBusStatus(oldBusID, "Bảo dưỡng/Sửa chữa", busDAO);
            Schedule currentSch = scheduleDAO.getScheduleById(scheduleID);
            java.sql.Date scheduleDate = currentSch != null ? currentSch.getDate() : new java.sql.Date(System.currentTimeMillis());
            busDAO.insertBusMaintenance(oldBusID, scheduleDate, "Bảo dưỡng do xe hỏng dọc đường");
            scheduleDAO.finishIncident(scheduleID);
        }

        response.sendRedirect("driver-dashboard");
    }

    private void updateScheduleStatus(int scheduleID, String status, ScheduleDAO dao) {
        String sql = "UPDATE Schedules SET Status = ? WHERE ScheduleID = ?";
        try {
            PreparedStatement st = dao.getConnection().prepareStatement(sql);
            st.setString(1, status);
            st.setInt(2, scheduleID);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateScheduleIncidentStatus(int scheduleID, String incidentStatus, ScheduleDAO dao) {
        String sql = "UPDATE Schedules SET IncidentStatus = ? WHERE ScheduleID = ?";
        try {
            PreparedStatement st = dao.getConnection().prepareStatement(sql);
            st.setString(1, incidentStatus);
            st.setInt(2, scheduleID);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBusStatus(int busID, String status, BusDAO dao) {
        String sql = "UPDATE Buses SET Status = ? WHERE BusID = ?";
        try {
            PreparedStatement st = dao.getConnection().prepareStatement(sql);
            st.setString(1, status);
            st.setInt(2, busID);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void notifyAdminForFutureSchedules(int busID, int currentScheduleID, java.sql.Date scheduleDate, ScheduleDAO scheduleDAO, BusDAO busDAO) {
        UserDAO userDAO = new UserDAO();
        java.util.List<model.User> admins = userDAO.getUsersByRole("ADMIN");
        if (admins.isEmpty()) return;

        dal.NotificationDAO notifDAO = new dal.NotificationDAO();
        Bus bus = busDAO.getBusById(busID);
        String licensePlate = (bus != null) ? bus.getLicensePlate() : String.valueOf(busID);

        java.util.List<Schedule> allSchedules = scheduleDAO.getAllSchedules();
        for (Schedule s : allSchedules) {
            if (s.getBusID() == busID && s.getScheduleID() != currentScheduleID) {
                if (s.getDate() != null && s.getDate().compareTo(scheduleDate) >= 0 && "PENDING".equals(s.getStatus())) {
                    long diffInMillies = Math.abs(s.getDate().getTime() - scheduleDate.getTime());
                    long diffInDays = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (diffInDays <= 2) {
                        String ca = "TO_SCHOOL".equals(s.getDirection()) ? "Sáng" : "Chiều";
                        String msg = "Xe " + licensePlate + " vừa được báo bảo dưỡng. Lịch trình ngày " + s.getDate() + " (Ca " + ca + ") đã bị HỦY tự động. Vui lòng phân ca lại! |DATE:" + s.getDate() + "|";
                        for (model.User admin : admins) {
                            notifDAO.insertNotification(admin.getUsername(), msg);
                        }
                        scheduleDAO.deleteSchedule(s.getScheduleID());
                    }
                }
            }
        }
    }
}
