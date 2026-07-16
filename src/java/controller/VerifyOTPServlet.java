package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý bước xác thực mã OTP do người dùng nhập vào.
 * Đảm bảo người dùng nhập đúng mã đã được gửi qua email.
 */
@WebServlet(name = "VerifyOTPServlet", urlPatterns = {"/VerifyOTPServlet"})
public class VerifyOTPServlet extends HttpServlet {

    /**
     * Hiển thị giao diện form nhập mã OTP.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("xac_thuc_otp.jsp").forward(request, response);
    }

    /**
     * Nhận mã OTP từ người dùng và so sánh với mã OTP đang lưu trong Session.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String inputOtp = request.getParameter("otpCode");
        HttpSession session = request.getSession();
        String sessionOtp = (String) session.getAttribute("otp");

        if (sessionOtp == null) {
            response.sendRedirect("quen_mat_khau.jsp");
            return;
        }

        if (inputOtp != null && inputOtp.equals(sessionOtp)) {
            // Xác thực thành công
            session.setAttribute("verifiedUser", true);
            response.sendRedirect("dat_lai_mat_khau.jsp");
        } else {
            // Xác thực thất bại
            request.setAttribute("errorMessage", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            request.getRequestDispatcher("xac_thuc_otp.jsp").forward(request, response);
        }
    }
}
