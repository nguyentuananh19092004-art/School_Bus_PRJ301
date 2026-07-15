package controller;

import dal.HocSinhDAO;
import dal.NotificationDAO;
import model.HocSinh;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý các yêu cầu của Phụ huynh từ giao diện Parent Dashboard.
 * Bao gồm: Xin nghỉ phép cho học sinh, Đổi điểm đón/trả, Ngừng dịch vụ và Đánh dấu đã đọc thông báo.
 */
@WebServlet(name = "ParentActionServlet", urlPatterns = {"/parent-action"})
public class ParentActionServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu POST chứa các hành động nghiệp vụ từ Phụ huynh.
     * Kiểm tra quyền, lấy thông tin học sinh liên kết với tài khoản phụ huynh và thực hiện yêu cầu tương ứng.
     * 
     * @param request đối tượng HttpServletRequest chứa các tham số hành động
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"phuhuynh".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String action = request.getParameter("action");
        String username = (String) session.getAttribute("username");
        HocSinhDAO hsDAO = new HocSinhDAO();
        HocSinh student = hsDAO.getHocSinhByTenTK(username);
        
        if (student == null) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        if ("report_absent".equals(action)) {
            if (student.getDefaultStopID() == null) {
                response.sendRedirect("parent-dashboard?msg=no_active_bus");
                return;
            }
            dal.StudentLeaveDAO leaveDAO = new dal.StudentLeaveDAO();
            boolean success = leaveDAO.insertLeave(student.getMaHocSinh(), new java.sql.Date(System.currentTimeMillis()));
            response.sendRedirect("parent-dashboard?msg=" + (success ? "leave_success" : "duplicate_leave"));
            return;
        } else if ("change_stop".equals(action)) {
            if (student.getPendingStopID() != null) {
                response.sendRedirect("parent-dashboard?msg=limit_exceeded");
                return;
            }
            String stopRoute = request.getParameter("stopRoute");
            if (stopRoute != null && stopRoute.contains("_")) {
                String[] parts = stopRoute.split("_");
                int stopID = Integer.parseInt(parts[0]);
                int routeID = Integer.parseInt(parts[1]);
                
                if (student.getDefaultStopID() != null && student.getDefaultStopID() == stopID && student.getDefaultRouteID() == routeID) {
                    response.sendRedirect("parent-dashboard");
                    return;
                }
                
                // set pending change for tomorrow
                java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
                hsDAO.setPendingStopChange(student.getMaHocSinh(), stopID, routeID, java.sql.Date.valueOf(tomorrow));
                response.sendRedirect("parent-dashboard?msg=change_pending");
                return;
            }
        } else if ("mark_read".equals(action)) {
            int notifID = Integer.parseInt(request.getParameter("notifID"));
            NotificationDAO notifDAO = new NotificationDAO();
            notifDAO.markAsRead(notifID);
        } else if ("stop_service".equals(action)) {
            if (student.getPendingStopID() != null) {
                response.sendRedirect("parent-dashboard?msg=limit_exceeded");
                return;
            }
            java.time.LocalDate tomorrow = java.time.LocalDate.now().plusDays(1);
            hsDAO.stopService(student.getMaHocSinh(), java.sql.Date.valueOf(tomorrow));
            response.sendRedirect("parent-dashboard?msg=stop_pending");
            return;
        } else if ("report_leave".equals(action)) {
            String leaveDateStr = request.getParameter("leaveDate");
            if (leaveDateStr != null && !leaveDateStr.isEmpty()) {
                dal.StudentLeaveDAO leaveDAO = new dal.StudentLeaveDAO();
                boolean success = leaveDAO.insertLeave(student.getMaHocSinh(), java.sql.Date.valueOf(leaveDateStr));
                response.sendRedirect("parent-dashboard?msg=" + (success ? "leave_success" : "duplicate_leave"));
                return;
            }
        }

        response.sendRedirect("parent-dashboard");
    }
}
