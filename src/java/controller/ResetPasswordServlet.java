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

@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/ResetPasswordServlet"})
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("dat_lai_mat_khau.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        if (session.getAttribute("verifiedUser") == null) {
            response.sendRedirect("quen_mat_khau.jsp");
            return;
        }

        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            request.getRequestDispatcher("dat_lai_mat_khau.jsp").forward(request, response);
            return;
        }

        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";
        if (!newPassword.matches(passwordRegex)) {
            request.setAttribute("errorMessage", "Mật khẩu phải từ 8 kí tự trở lên, bao gồm chữ in thường, in hoa, số và kí tự đặc biệt.");
            request.getRequestDispatcher("dat_lai_mat_khau.jsp").forward(request, response);
            return;
        }

        String resetType = (String) session.getAttribute("resetType");
        boolean success = false;

        if ("USER".equals(resetType)) {
            User resetUser = (User) session.getAttribute("resetUser");
            if (resetUser != null) {
                UserDAO uDao = new UserDAO();
                success = uDao.updatePassword(resetUser.getUserID(), newPassword);
                
                dal.HocSinhDAO hsDao = new dal.HocSinhDAO();
                model.HocSinh hs = hsDao.getHocSinhByTenTK(resetUser.getUsername());
                if (hs != null) {
                    hsDao.updatePassword(hs.getMaHocSinh(), newPassword);
                }
            }
        } else if ("HOCSINH".equals(resetType)) {
            model.HocSinh resetHocSinh = (model.HocSinh) session.getAttribute("resetHocSinh");
            if (resetHocSinh != null) {
                dal.HocSinhDAO hsDao = new dal.HocSinhDAO();
                success = hsDao.updatePassword(resetHocSinh.getMaHocSinh(), newPassword);
                
                UserDAO uDao = new UserDAO();
                User u = uDao.getUserByUsername(resetHocSinh.getTenTK());
                if (u != null) {
                    uDao.updatePassword(u.getUserID(), newPassword);
                }
            }
        }

        if (success) {
            // Xóa session
            session.removeAttribute("otp");
            session.removeAttribute("resetUser");
            session.removeAttribute("resetHocSinh");
            session.removeAttribute("resetType");
            session.removeAttribute("verifiedUser");
            
            request.setAttribute("successMessage", "Đổi mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.");
            request.getRequestDispatcher("quen_mat_khau.jsp").forward(request, response);
        } else {
            request.setAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật mật khẩu. Phiên đổi mật khẩu không hợp lệ.");
            request.getRequestDispatcher("dat_lai_mat_khau.jsp").forward(request, response);
        }
    }
}
