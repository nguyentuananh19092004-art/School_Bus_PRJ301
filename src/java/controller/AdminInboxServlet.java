package controller;

import dal.UserDAO;
import dal.NotificationDAO;
import model.UserLeave;
import model.User;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý hộp thư đến của Admin (chủ yếu quản lý đơn xin nghỉ phép).
 * Chức năng: Xem, duyệt, từ chối đơn xin nghỉ và phân công người thay thế.
 */
@WebServlet(name = "AdminInboxServlet", urlPatterns = {"/admin-inbox"})
public class AdminInboxServlet extends HttpServlet {

    /**
     * Hiển thị danh sách các đơn xin nghỉ phép đang chờ duyệt.
     * Tự động tìm kiếm và gợi ý danh sách nhân sự thay thế hợp lệ (rảnh rỗi) trong cùng ngày.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        UserDAO userDAO = new UserDAO();
        List<UserLeave> pendingLeaves = userDAO.getPendingLeaves();
        
        // Load available replacements and schedule check for each leave request
        java.util.Map<Integer, List<User>> replacementsMap = new java.util.HashMap<>();
        java.util.Map<Integer, Boolean> hasSchedulesMap = new java.util.HashMap<>();
        dal.ScheduleDAO scheduleDAO = new dal.ScheduleDAO();
        for (UserLeave ul : pendingLeaves) {
            List<model.Schedule> schedulesToReplace = scheduleDAO.getSchedulesByUserAndDate(ul.getUserID(), ul.getRole(), ul.getLeaveDate());
            List<model.TechnicianSchedule> techSchedulesToReplace = scheduleDAO.getTechnicianSchedulesByUserAndDate(ul.getUserID(), ul.getLeaveDate());

            List<User> available = userDAO.getUsersByRoleAndDate(ul.getRole(), ul.getLeaveDate());
            available.removeIf(u -> u.getUserID() == ul.getUserID());
            available.removeIf(u -> "Nghỉ".equals(u.getStatus()));
            
            available.removeIf(u -> {
                if ("kythuat".equalsIgnoreCase(ul.getRole()) || "TECHNICIAN".equalsIgnoreCase(ul.getRole())) {
                    return "Hoạt động".equals(u.getStatus());
                }
                List<model.Schedule> existing = scheduleDAO.getSchedulesByUserAndDate(u.getUserID(), ul.getRole(), ul.getLeaveDate());
                for (model.Schedule s1 : schedulesToReplace) {
                    for (model.Schedule s2 : existing) {
                        if (s1.getDirection() != null && s1.getDirection().equals(s2.getDirection()) && !"CANCELLED".equals(s2.getStatus())) {
                            return true;
                        }
                    }
                }
                return false;
            });

            replacementsMap.put(ul.getLeaveID(), available);
            hasSchedulesMap.put(ul.getLeaveID(), !schedulesToReplace.isEmpty() || !techSchedulesToReplace.isEmpty());
        }
        
        request.setAttribute("pendingLeaves", pendingLeaves);
        request.setAttribute("replacementsMap", replacementsMap);
        request.setAttribute("hasSchedulesMap", hasSchedulesMap);
        request.getRequestDispatcher("admin_inbox.jsp").forward(request, response);
    }

    /**
     * Xử lý hành động Duyệt (Approve) hoặc Từ chối (Reject) đơn xin nghỉ.
     * Nếu Duyệt: tự động cập nhật lịch trình sang người thay thế và gửi thông báo (Notification) cho các bên liên quan.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String action = request.getParameter("action");
        int leaveID = Integer.parseInt(request.getParameter("leaveID"));
        
        UserDAO userDAO = new UserDAO();
        NotificationDAO notifDAO = new NotificationDAO();
        
        UserLeave leave = userDAO.getLeaveById(leaveID);
        if (leave == null) {
            response.sendRedirect("admin-inbox?msg=error");
            return;
        }

        String username = userDAO.getUsernameById(leave.getUserID());
        User leaveUser = userDAO.getUserById(leave.getUserID());
        
        if ("approve".equals(action)) {
            userDAO.updateLeaveStatus(leaveID, "APPROVED");
            if (username != null) {
                notifDAO.insertNotification(username, "Đơn xin nghỉ phép của bạn vào ngày " + leave.getLeaveDate() + " đã được DUYỆT.");
            }
            
            String replacementUserIDRaw = request.getParameter("replacementUserID");
            if (replacementUserIDRaw != null && !replacementUserIDRaw.isEmpty()) {
                int replacementUserID = Integer.parseInt(replacementUserIDRaw);
                dal.ScheduleDAO scheduleDAO = new dal.ScheduleDAO();
                List<model.Schedule> schedulesToReplace = scheduleDAO.getSchedulesByUserAndDate(leave.getUserID(), leaveUser.getRole(), leave.getLeaveDate());
                List<model.TechnicianSchedule> techSchedulesToReplace = scheduleDAO.getTechnicianSchedulesByUserAndDate(leave.getUserID(), leave.getLeaveDate());
                
                for (model.Schedule s : schedulesToReplace) {
                    scheduleDAO.updateSchedulePersonnel(s.getScheduleID(), leaveUser.getRole(), replacementUserID);
                }
                for (model.TechnicianSchedule ts : techSchedulesToReplace) {
                    scheduleDAO.updateTechnicianSchedulePersonnel(ts.getTechScheduleID(), replacementUserID);
                }
                
                // Gửi thông báo cho người thay thế
                User replacementUser = userDAO.getUserById(replacementUserID);
                if (replacementUser != null) {
                    String msgText = "Bạn vừa được phân công thay thế lịch làm việc ngày " + leave.getLeaveDate() + ". Vui lòng kiểm tra lịch làm việc.";
                    notifDAO.insertNotification(replacementUser.getUsername(), msgText);
                }
            }
            
            response.sendRedirect("admin-inbox?msg=approved");
        } else if ("reject".equals(action)) {
            userDAO.updateLeaveStatus(leaveID, "REJECTED");
            if (username != null) {
                notifDAO.insertNotification(username, "Đơn xin nghỉ phép của bạn vào ngày " + leave.getLeaveDate() + " đã BỊ TỪ CHỐI. Vui lòng liên hệ Admin.");
            }
            response.sendRedirect("admin-inbox?msg=rejected");
        } else {
            response.sendRedirect("admin-inbox");
        }
    }
}
