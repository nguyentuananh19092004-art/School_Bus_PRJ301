package controller;

import dal.ScheduleDAO;
import dal.UserDAO;
import dal.NotificationDAO;
import model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý yêu cầu phân công người thay thế của Admin.
 * Được sử dụng khi nhân viên (Tài xế, Giám thị, Kỹ thuật) xin nghỉ phép và Admin cần phân công người khác làm thay ca.
 */
@WebServlet(name = "AdminReplaceServlet", urlPatterns = {"/admin-replace"})
public class AdminReplaceServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu POST để lưu thông tin người thay thế vào lịch trình.
     * Cập nhật database và tự động gửi thông báo cho nhân viên mới được phân công.
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form thay thế
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String role = request.getParameter("role");
        String[] scheduleIDs = request.getParameterValues("scheduleID");
        String[] techScheduleIDs = request.getParameterValues("techScheduleID");
        String leaveDate = request.getParameter("leaveDate");

        ScheduleDAO scheduleDAO = new ScheduleDAO();
        UserDAO userDAO = new UserDAO();
        NotificationDAO notifDAO = new NotificationDAO();

        if (scheduleIDs != null) {
            for (String sID : scheduleIDs) {
                int scheduleID = Integer.parseInt(sID);
                String replacementParam = request.getParameter("replacement_" + scheduleID);
                
                if (replacementParam != null && !replacementParam.isEmpty()) {
                    int newUserID = Integer.parseInt(replacementParam);
                    scheduleDAO.updateSchedulePersonnel(scheduleID, role, newUserID);
                    
                    // Gửi thông báo cho người thay thế
                    User newUser = userDAO.getUserById(newUserID);
                    if (newUser != null) {
                        notifDAO.insertNotification(newUser.getUsername(), "Bạn vừa được phân công thay thế giám sát/lái xe cho chuyến xe số " + scheduleID + ". Vui lòng kiểm tra lịch làm việc.");
                    }
                }
            }
        }

        if (techScheduleIDs != null) {
            for (String tsID : techScheduleIDs) {
                int techScheduleID = Integer.parseInt(tsID);
                String replacementParam = request.getParameter("replacement_tech_" + techScheduleID);
                
                if (replacementParam != null && !replacementParam.isEmpty()) {
                    int newUserID = Integer.parseInt(replacementParam);
                    scheduleDAO.updateTechnicianSchedulePersonnel(techScheduleID, newUserID);
                    
                    // Gửi thông báo cho người thay thế
                    User newUser = userDAO.getUserById(newUserID);
                    if (newUser != null) {
                        notifDAO.insertNotification(newUser.getUsername(), "Bạn vừa được phân công thay thế ca kỹ thuật ngày " + (leaveDate != null ? leaveDate : "") + ". Vui lòng kiểm tra lịch làm việc.");
                    }
                }
            }
        }

        response.sendRedirect("admin-inbox?msg=approved");
    }
}
