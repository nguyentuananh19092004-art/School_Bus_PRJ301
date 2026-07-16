package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý luồng đăng nhập của tất cả người dùng hệ thống.
 * Hỗ trợ xác thực nhiều vai trò (Admin, Giám thị, Phụ huynh, Tài xế, Kỹ thuật).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet"})
public class LoginServlet extends HttpServlet {

    /**
     * Chuyển hướng người dùng truy cập trực tiếp bằng URL đến trang đăng nhập.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("dang_nhap.jsp");
    }

    /**
     * Xử lý dữ liệu form đăng nhập. Xác thực tài khoản với CSDL và khởi tạo Session.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        dal.UserDAO userDAO = new dal.UserDAO();
        String role = null;
        
        if (userDAO.checkLogin(username, password, "ADMIN")) role = "admin";
        else if (userDAO.checkLogin(username, password, "MONITOR")) role = "giamthi";
        else if (userDAO.checkLogin(username, password, "PARENT")) role = "phuhuynh";
        else if (userDAO.checkLogin(username, password, "DRIVER")) role = "taixe";
        else if (userDAO.checkLogin(username, password, "TECHNICIAN")) role = "kythuat";
        
        boolean isValid = (role != null);
        
        if (!isValid) {
            dal.HocSinhDAO hocSinhDAO = new dal.HocSinhDAO();
            if (hocSinhDAO.checkLogin(username, password)) {
                isValid = true;
                role = "phuhuynh";
            }
        }
        
        if (isValid) {
            HttpSession session = request.getSession();
            session.setAttribute("userRole", role);
            session.setAttribute("username", username);
            
            model.User user = userDAO.getUserByUsername(username);
            if (user != null) {
                session.setAttribute("userID", user.getUserID());
            } else {
                // Phụ huynh (HocSinh table)
                dal.HocSinhDAO hocSinhDAO = new dal.HocSinhDAO();
                model.HocSinh hs = hocSinhDAO.getHocSinhByTenTK(username);
                if (hs != null) {
                    session.setAttribute("userID", hs.getMaHocSinh());
                }
            }
            
            if ("admin".equals(role)) {
                response.sendRedirect("AdminDashboardServlet");
            } else if ("giamthi".equals(role)) {
                response.sendRedirect("monitor-dashboard");
            } else if ("phuhuynh".equals(role)) {
                response.sendRedirect("parent-dashboard");
            } else if ("taixe".equals(role)) {
                response.sendRedirect("driver-dashboard");
            } else if ("kythuat".equals(role)) {
                response.sendRedirect("technician-dashboard");
            } else {
                response.sendRedirect("index.jsp");
            }
        } else {
            request.setAttribute("errorMessage", "Sai tài khoản, mật khẩu!");
            request.setAttribute("username", username);
            request.setAttribute("password", password);
            request.getRequestDispatcher("dang_nhap.jsp").forward(request, response);
        }
    }
}
