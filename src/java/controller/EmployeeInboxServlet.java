package controller;

import dal.NotificationDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "EmployeeInboxServlet", urlPatterns = {"/employee-inbox"})
public class EmployeeInboxServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("userRole");
        if (role == null || (!"taixe".equals(role) && !"giamthi".equals(role) && !"kythuat".equals(role))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        NotificationDAO notifDAO = new NotificationDAO();
        
        request.setAttribute("notifications", notifDAO.getNotificationsByUsername(username));
        notifDAO.markAllAsRead(username);
        
        request.getRequestDispatcher("employee_inbox.jsp").forward(request, response);
    }
}
