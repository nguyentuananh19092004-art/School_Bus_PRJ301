package controller;

import dal.BusDAO;
import dal.UserDAO;
import java.io.IOException;
import java.sql.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet xử lý các thay đổi trạng thái nhanh trong ngày của Nhân sự và Xe bus.
 * Ví dụ: Báo cáo nghỉ đột xuất, báo cáo xe hỏng đi bảo dưỡng khẩn cấp.
 */
@WebServlet(name = "DailyStatusServlet", urlPatterns = {"/daily-status"})
public class DailyStatusServlet extends HttpServlet {

    /**
     * Xử lý các action chuyển trạng thái.
     * Tự động kiểm tra nếu việc báo bảo dưỡng ảnh hưởng tới lịch chạy đã xếp thì gửi thông báo khẩn cho Admin.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String type = request.getParameter("type"); // "user" or "bus"
        String dateStr = request.getParameter("date");
        Date date = Date.valueOf(dateStr);
        
        if ("user".equals(type)) {
            int id = Integer.parseInt(request.getParameter("id"));
            String role = request.getParameter("role");
            UserDAO dao = new UserDAO();
            if ("cancel_leave".equals(action)) {
                dao.deleteUserLeave(id, date);
            } else if ("report_leave".equals(action)) {
                dao.insertUserLeave(id, date, "Nghỉ phép", "APPROVED");
            }
            response.sendRedirect("user-list?role=" + role + "&date=" + dateStr);
        } else if ("bus".equals(type)) {
            int id = Integer.parseInt(request.getParameter("id"));
            BusDAO dao = new BusDAO();
            if ("cancel_maint".equals(action)) {
                model.User user = (model.User) request.getSession().getAttribute("user");
                if (user == null || !"TECHNICIAN".equalsIgnoreCase(user.getRole())) {
                    request.getSession().setAttribute("error", "Chỉ Kỹ thuật viên mới có quyền hủy bảo dưỡng!");
                    response.sendRedirect("bus-list?date=" + dateStr);
                    return;
                }
                dao.deleteBusMaintenance(id, date);
                model.Bus b = dao.getBusById(id);
                if (b != null) {
                    b.setStatus("Sẵn sàng");
                    dao.updateBus(b);
                }
            } else if ("report_maint".equals(action)) {
                dao.insertBusMaintenance(id, date, "Bảo dưỡng định kỳ");
                model.Bus b = dao.getBusById(id);
                if (b != null) {
                    b.setStatus("Bảo dưỡng/Sửa chữa");
                    dao.updateBus(b);
                }
                notifyAdminForMaintenance(id, date);
            }
            response.sendRedirect("bus-list?date=" + dateStr);
        }
    }
    
    private void notifyAdminForMaintenance(int busID, Date maintDate) {
        dal.UserDAO userDAO = new dal.UserDAO();
        java.util.List<model.User> admins = userDAO.getUsersByRole("ADMIN");
        if (admins.isEmpty()) return;

        dal.NotificationDAO notifDAO = new dal.NotificationDAO();
        dal.BusDAO busDAO = new dal.BusDAO();
        dal.ScheduleDAO scheduleDAO = new dal.ScheduleDAO();
        
        model.Bus bus = busDAO.getBusById(busID);
        String licensePlate = (bus != null) ? bus.getLicensePlate() : String.valueOf(busID);

        java.util.List<model.Schedule> allSchedules = scheduleDAO.getAllSchedules();
        for (model.Schedule s : allSchedules) {
            if (s.getBusID() == busID) {
                if (s.getDate() != null && s.getDate().compareTo(maintDate) >= 0 && "PENDING".equals(s.getStatus())) {
                    long diffInMillies = s.getDate().getTime() - maintDate.getTime();
                    long diffInDays = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (diffInDays <= 2) {
                        String ca = "TO_SCHOOL".equals(s.getDirection()) ? "Sáng" : "Chiều";
                        String msg = "Xe " + licensePlate + " vừa báo bảo dưỡng ngày " + maintDate + ". Lịch trình ngày " + s.getDate() + " Ca " + ca + " bị ảnh hưởng! |SCHEDULE_ID:" + s.getScheduleID() + "|";
                        for (model.User admin : admins) {
                            notifDAO.insertNotification(admin.getUsername(), msg);
                        }
                    }
                }
            }
        }
    }
}
