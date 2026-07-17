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
 * Servlet quản lý chức năng cập nhật thông tin người dùng (Nhân viên).
 * Cung cấp form cập nhật và xử lý dữ liệu được gửi lên để lưu thay đổi.
 */
@WebServlet(name = "UserUpdateServlet", urlPatterns = {"/user-update"})
public class UserUpdateServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để hiển thị form cập nhật với thông tin hiện tại của người dùng.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (ID người dùng)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        UserDAO dao = new UserDAO();
        User u = dao.getUserById(id);
        
        request.setAttribute("userObj", u);
        request.setAttribute("role", u.getRole());
        request.getRequestDispatcher("user_form.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST để cập nhật thông tin người dùng.
     * Kiểm tra tính hợp lệ của dữ liệu tương tự như khi tạo mới (tên đăng nhập, số điện thoại, email).
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form cần cập nhật
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        int userID = Integer.parseInt(request.getParameter("userID"));
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String role = request.getParameter("role");
        String status = request.getParameter("status");

        UserDAO dao = new UserDAO();
        String password = request.getParameter("password"); // get password from form

        // 1. Kiểm tra xem Tên tài khoản đã tồn tại chưa (ngoại trừ tài khoản hiện tại)
        if (dao.checkUsernameExist(username, userID)) {
            request.setAttribute("error", "Tên tài khoản '" + username + "' đã được sử dụng!");
            request.setAttribute("userObj", new User(userID, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 2. Kiểm tra xem Số điện thoại đã được người khác sử dụng chưa
        if (dao.checkPhoneExist(phone, userID, username)) {
            request.setAttribute("error", "Số điện thoại '" + phone + "' đã được người khác sử dụng!");
            request.setAttribute("userObj", new User(userID, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 3. Kiểm tra xem Email đã được sử dụng chưa (ngoại trừ tài khoản hiện tại)
        if (dao.checkEmailExist(email, userID, username)) {
            request.setAttribute("error", "Email '" + email + "' đã được sử dụng bởi một tài khoản khác!");
            request.setAttribute("userObj", new User(userID, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 4. Kiểm tra xem Email có thuộc tên miền (domain) cho phép hay không
        String allowedDomain = getServletContext().getInitParameter("ALLOWED_EMAIL_DOMAIN");
        if (!util.EmailUtil.isValidDomain(email, allowedDomain)) {
            request.setAttribute("error", "Email bắt buộc phải có tên miền thuộc danh sách: " + allowedDomain);
            request.setAttribute("userObj", new User(userID, username, password, role, fullName, phone, email, status));
            request.setAttribute("role", role);
            request.getRequestDispatcher("user_form.jsp").forward(request, response);
            return;
        }

        // 5. Nếu mọi dữ liệu hợp lệ, cập nhật thông tin người dùng
        User u = new User(userID, username, password, role, fullName, phone, email, status);
        dao.updateUser(u);

        response.sendRedirect("user-list?role=" + role);
    }
}
