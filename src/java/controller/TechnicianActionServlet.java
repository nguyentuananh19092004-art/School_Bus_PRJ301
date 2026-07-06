package controller;

import dal.ScheduleDAO;
import dal.BusDAO;
import model.Schedule;
import model.Bus;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "TechnicianActionServlet", urlPatterns = {"/technician-action"})
public class TechnicianActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"kythuat".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String action = request.getParameter("action");
        
        if ("dispatch_bus".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            String repBusStr = request.getParameter("replacementBusID");
            if (repBusStr == null || repBusStr.trim().isEmpty()) {
                response.sendRedirect("technician-dashboard?msg=error_no_bus");
                return;
            }
            int replacementBusID = Integer.parseInt(repBusStr);
            int userID = (int) session.getAttribute("userID");
            ScheduleDAO dao = new ScheduleDAO();
            
            Schedule currentSch = dao.getScheduleById(scheduleID);
            
            if (currentSch != null) {
                // Find all schedules on the same date with the same broken bus
                java.util.List<Schedule> allSchedules = dao.getAllSchedules();
                for (Schedule s : allSchedules) {
                    if (s.getDate() != null && currentSch.getDate() != null &&
                        s.getDate().toString().equals(currentSch.getDate().toString()) && 
                        (s.getBusID() == currentSch.getBusID() || s.getReplacementBusID() == currentSch.getBusID()) &&
                        !"COMPLETED".equals(s.getStatus()) && !"CANCELLED".equals(s.getStatus())) {
                        
                        if (s.getScheduleID() == currentSch.getScheduleID()) {
                            dao.updateIncidentStatus(s.getScheduleID(), "DISPATCHED", replacementBusID);
                            dao.updateHandlingTechID(s.getScheduleID(), userID);
                        } else {
                            // Update BusID directly for other schedules later in the day
                            dao.updateScheduleBus(s.getScheduleID(), replacementBusID);
                        }
                    }
                }
            } else {
                dao.updateIncidentStatus(scheduleID, "DISPATCHED", replacementBusID);
                dao.updateHandlingTechID(scheduleID, userID);
            }
        } else if ("arrive_incident".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            ScheduleDAO dao = new ScheduleDAO();
            dao.updateIncidentStatus(scheduleID, "ARRIVED", 0);
        } else if ("handover_bus".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            ScheduleDAO dao = new ScheduleDAO();
            dao.updateIncidentStatus(scheduleID, "HANDED_OVER", 0);
        } else if ("mark_maintenance".equals(action)) {
            int busID = Integer.parseInt(request.getParameter("brokenBusID"));
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            
            BusDAO busDAO = new BusDAO();
            String sql = "UPDATE Buses SET Status = N'Bảo dưỡng/Sửa chữa' WHERE BusID = ?";
            try {
                PreparedStatement st = busDAO.getConnection().prepareStatement(sql);
                st.setInt(1, busID);
                st.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            // Query schedule date
            java.sql.Date scheduleDate = null;
            String getSchDateSql = "SELECT Date FROM Schedules WHERE ScheduleID = ?";
            try {
                PreparedStatement st = busDAO.getConnection().prepareStatement(getSchDateSql);
                st.setInt(1, scheduleID);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        scheduleDate = rs.getDate("Date");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (scheduleDate == null) {
                scheduleDate = new java.sql.Date(System.currentTimeMillis());
            }
            
            // Insert into BusMaintenances to display on Bus List dynamic status
            busDAO.insertBusMaintenance(busID, scheduleDate, "Bảo dưỡng do xe hỏng dọc đường");
            
            ScheduleDAO scheduleDAO = new ScheduleDAO();
            scheduleDAO.finishIncident(scheduleID);
            
            notifyAdminForFutureSchedules(busID, scheduleID, scheduleDate, scheduleDAO, busDAO);
            
        } else if ("resolve_incident".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            ScheduleDAO dao = new ScheduleDAO();
            dao.updateIncidentStatus(scheduleID, "NORMAL", 0);
        } else if ("finish_maintenance".equals(action)) {
            int busID = Integer.parseInt(request.getParameter("busID"));
            BusDAO dao = new BusDAO();
            String sql = "UPDATE Buses SET Status = N'Sẵn sàng' WHERE BusID = ?";
            try {
                PreparedStatement st = dao.getConnection().prepareStatement(sql);
                st.setInt(1, busID);
                st.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Deletes the maintenance record for the bus
            String deleteSql = "DELETE FROM BusMaintenances WHERE BusID = ?";
            try {
                PreparedStatement st = dao.getConnection().prepareStatement(deleteSql);
                st.setInt(1, busID);
                st.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if ("start_shift".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            int userID = (int) session.getAttribute("userID");
            ScheduleDAO dao = new ScheduleDAO();
            dao.updateTechnicianScheduleStatus(scheduleID, "IN_PROGRESS");
            
        } else if ("end_shift".equals(action)) {
            java.time.LocalTime now = java.time.LocalTime.now();
            if (now.isBefore(java.time.LocalTime.of(18, 0))) {
                response.sendRedirect("technician-dashboard?msg=early_end");
                return;
            }
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            int userID = (int) session.getAttribute("userID");
            ScheduleDAO dao = new ScheduleDAO();
            dao.updateTechnicianScheduleStatus(scheduleID, "COMPLETED");
        }

        response.sendRedirect("technician-dashboard");
    }
    private void notifyAdminForFutureSchedules(int busID, int currentScheduleID, java.sql.Date scheduleDate, ScheduleDAO scheduleDAO, BusDAO busDAO) {
        dal.UserDAO userDAO = new dal.UserDAO();
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
