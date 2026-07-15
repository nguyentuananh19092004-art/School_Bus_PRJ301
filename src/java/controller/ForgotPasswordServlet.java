package controller;

import dal.UserDAO;
import java.io.IOException;
import java.util.Random;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.EmailUtil;

/**
 * Servlet xử lý chức năng quên mật khẩu.
 * Thực hiện tìm kiếm tài khoản/email, sinh mã OTP tự động và gửi qua email cho người dùng.
 */
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/ForgotPasswordServlet"})
public class ForgotPasswordServlet extends HttpServlet {

    /**
     * Hiển thị trang yêu cầu khôi phục mật khẩu.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("quen_mat_khau.jsp").forward(request, response);
    }

    /**
     * Nhận yêu cầu khôi phục mật khẩu (Username).
     * Kiểm tra sự tồn tại của Email trong DB, tạo mã OTP 6 số ngẫu nhiên và gửi OTP.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        String username = request.getParameter("username");

        if (username == null || username.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập tên đăng nhập.");
            // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("quen_mat_khau.jsp").forward(request, response);
            return;
        }

        UserDAO uDao = new UserDAO();
        User user = uDao.getUserByUsername(username);
        dal.HocSinhDAO hsDao = new dal.HocSinhDAO();
        model.HocSinh hs = hsDao.getHocSinhByTenTK(username);
        String emailToUse = null;

        if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            emailToUse = user.getEmail();
            hs = null; // prioritize User
        } else if (hs != null && hs.getEmail() != null && !hs.getEmail().trim().isEmpty()) {
            emailToUse = hs.getEmail();
            user = null; // use HocSinh instead
        }

        if (user == null && hs == null) {
            // Check if they originally existed but both had no email
            User originalUser = uDao.getUserByUsername(username);
            model.HocSinh originalHs = hsDao.getHocSinhByTenTK(username);
            
            if (originalUser == null && originalHs == null) {
                request.setAttribute("errorMessage", "Tên đăng nhập không tồn tại.");
            } else {
                request.setAttribute("errorMessage", "Tài khoản này chưa được liên kết với địa chỉ email nào.");
            }
            // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("quen_mat_khau.jsp").forward(request, response);
            return;
        }

        // Sinh mã OTP 6 số
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String otp = String.format("%06d", number);

        // Gửi email
        boolean emailSent = EmailUtil.sendOTP(emailToUse, otp);

        if (emailSent) {
            HttpSession session = request.getSession();
            session.setAttribute("otp", otp);
            if (user != null) {
                session.setAttribute("resetUser", user);
                session.setAttribute("resetType", "USER");
            } else {
                session.setAttribute("resetHocSinh", hs);
                session.setAttribute("resetType", "HOCSINH");
            }
            
            // Chuyển hướng sang trang nhập OTP
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("xac_thuc_otp.jsp");
        } else {
            request.setAttribute("errorMessage", "Lỗi gửi email! Vui lòng kiểm tra lại cấu hình hoặc thử lại sau.");
            // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("quen_mat_khau.jsp").forward(request, response);
        }
    }
}
