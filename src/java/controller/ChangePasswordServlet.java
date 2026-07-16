package controller;

import dal.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;

/**
 * Servlet xử lý tính năng đổi mật khẩu cho người dùng đã đăng nhập thành công.
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    /**
     * Hiển thị giao diện form đổi mật khẩu.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("doi_mat_khau.jsp");
    }

    /**
     * Nhận dữ liệu đổi mật khẩu. Yêu cầu kiểm tra mật khẩu cũ hợp lệ,
     * xác nhận mật khẩu mới khớp nhau và thỏa mãn điều kiện độ phức tạp.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userID") == null) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        int userID = (int) session.getAttribute("userID");
        String oldPassword = request.getParameter("oldPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        UserDAO uDao = new UserDAO();
        User user = uDao.getUserById(userID);

        if (user == null || !user.getPassword().equals(oldPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu cũ không chính xác.");
            request.getRequestDispatcher("doi_mat_khau.jsp").forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            request.getRequestDispatcher("doi_mat_khau.jsp").forward(request, response);
            return;
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";
        if (!newPassword.matches(passwordRegex)) {
            request.setAttribute("errorMessage", "Mật khẩu phải từ 8 kí tự trở lên, bao gồm chữ in thường, in hoa, số và kí tự đặc biệt.");
            request.getRequestDispatcher("doi_mat_khau.jsp").forward(request, response);
            return;
        }

        boolean success = uDao.updatePassword(userID, newPassword);
        if (success) {
            request.setAttribute("successMessage", "Đổi mật khẩu thành công!");
            request.getRequestDispatcher("doi_mat_khau.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", "Đã có lỗi xảy ra. Vui lòng thử lại sau.");
            request.getRequestDispatcher("doi_mat_khau.jsp").forward(request, response);
        }
    }
}
