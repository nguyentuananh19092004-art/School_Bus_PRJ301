package controller;

import dal.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

/**
 * Servlet quản lý chức năng tạo mới người dùng (Nhân viên).
 * Cung cấp form tạo mới và xử lý dữ liệu được gửi lên từ form.
 */
@WebServlet(name = "UserCreateServlet", urlPatterns = {"/user-create"})
public class UserCreateServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để hiển thị form tạo mới người dùng.
     * Truyền vai trò (role) hiện tại vào form để thiết lập mặc định.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String role = request.getParameter("role");
        request.setAttribute("role", role);
        request.getRequestDispatcher("user_form.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST để thêm mới người dùng vào hệ thống.
     * Kiểm tra tính hợp lệ của dữ liệu (tên đăng nhập, số điện thoại, email, tên miền email).
     * Nếu có lỗi, quay lại form với thông báo lỗi. Nếu thành công, thêm vào cơ sở dữ liệu và chuyển hướng.
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String status = request.getParameter("status");
        String password = request.getParameter("password");
        if (password == null || password.trim().isEmpty()) {
            password = "123"; // Default password for new staff if not provided
        }

        UserDAO dao = new UserDAO();
        
        // 1. Kiểm tra xem tên đăng nhập (username) đã tồn tại trong hệ thống chưa
        if (dao.checkUsernameExist(username, 0)) {
            request.setAttribute("error", "Tên tài khoản '" + username + "' đã tồn tại!");
            request.setAttribute("userObj", new User(0, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 2. Kiểm tra xem số điện thoại đã được đăng ký cho người khác chưa
        if (dao.checkPhoneExist(phone, 0, username)) {
            request.setAttribute("error", "Số điện thoại '" + phone + "' đã được người khác sử dụng!");
            request.setAttribute("userObj", new User(0, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 3. Kiểm tra xem email đã tồn tại trong hệ thống chưa (cả bảng User và Học sinh)
        if (dao.checkEmailExist(email, 0, username)) {
            request.setAttribute("error", "Email '" + email + "' đã được sử dụng bởi một tài khoản khác!");
            request.setAttribute("userObj", new User(0, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 4. Kiểm tra xem email có thuộc tên miền (domain) cho phép của trường hay không
        String allowedDomain = getServletContext().getInitParameter("ALLOWED_EMAIL_DOMAIN");
        if (!util.EmailUtil.isValidDomain(email, allowedDomain)) {
            request.setAttribute("error", "Email bắt buộc phải có tên miền thuộc danh sách: " + allowedDomain);
            request.setAttribute("userObj", new User(0, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 5. Nếu tất cả dữ liệu hợp lệ, tiến hành tạo đối tượng User và lưu vào cơ sở dữ liệu
        User u = new User(0, username, password, role, fullName, phone, email, status);
        dao.insertUser(u);

        response.sendRedirect("user-list?role=" + role);
    }
}
