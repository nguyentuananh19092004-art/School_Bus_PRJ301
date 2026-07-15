package controller;

import dal.NotificationDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý hộp thư đến (Inbox) của nhân viên (Tài xế, Giám thị, Kỹ thuật viên).
 * Nơi nhân viên xem thông báo hệ thống và nộp đơn xin nghỉ phép.
 */
@WebServlet(name = "EmployeeInboxServlet", urlPatterns = {"/employee-inbox"})
public class EmployeeInboxServlet extends HttpServlet {

    /**
     * Lấy danh sách thông báo của người dùng và tự động đánh dấu tất cả là "Đã đọc".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("userRole");
        if (role == null || (!"taixe".equals(role) && !"giamthi".equals(role) && !"kythuat".equals(role))) {
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        NotificationDAO notifDAO = new NotificationDAO();
        
        request.setAttribute("notifications", notifDAO.getNotificationsByUsername(username));
        notifDAO.markAllAsRead(username);
        
        // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("employee_inbox.jsp").forward(request, response);
    }
}
