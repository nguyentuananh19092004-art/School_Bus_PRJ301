package controller;

import dal.UserDAO;
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
        
        // 1. Kiểm tra đăng nhập với các vai trò của Nhân viên (Admin, Kỹ thuật, Tài xế, Giám thị)
        UserDAO userDAO = new UserDAO();
        String role = null;
        if (userDAO.checkLogin(username, password, "ADMIN")) role = "admin";
        else if (userDAO.checkLogin(username, password, "TECHNICIAN")) role = "kythuat";
        else if (userDAO.checkLogin(username, password, "DRIVER")) role = "taixe";
        else if (userDAO.checkLogin(username, password, "MONITOR")) role = "giamthi";

        boolean isValid = (role != null);
        
        // 2. Nếu không phải Nhân viên, kiểm tra xem có phải là Phụ huynh (dựa trên bảng HocSinh) không
        if (!isValid) {
            dal.HocSinhDAO hocSinhDAO = new dal.HocSinhDAO();
            if (hocSinhDAO.checkLogin(username, password)) {
                isValid = true;
                role = "phuhuynh";
            }
        }
        
        // 3. Nếu đăng nhập thành công (tài khoản hợp lệ), khởi tạo Session
        if (isValid) {
            HttpSession session = request.getSession();
            session.setAttribute("userRole", role);
            session.setAttribute("username", username);
            
            // 4. Lấy và lưu UserID hoặc MaHocSinh vào Session để sử dụng cho các chức năng khác
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
