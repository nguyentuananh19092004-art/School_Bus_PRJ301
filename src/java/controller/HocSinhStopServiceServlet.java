package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet quản lý chức năng Ngừng dịch vụ cho Học sinh.
 * Cập nhật trạng thái của học sinh thành "Ngừng dịch vụ". Chỉ Admin mới có quyền thực hiện.
 */
@WebServlet(name = "HocSinhStopServiceServlet", urlPatterns = {"/hocsinh-stop-service"})
public class HocSinhStopServiceServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để thay đổi trạng thái dịch vụ của học sinh.
     * Kiểm tra quyền của người dùng trước khi thực hiện thao tác.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (Mã học sinh)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        // Kiểm tra quyền đăng nhập qua Session
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String maHocSinh = request.getParameter("id");
        if (maHocSinh != null && !maHocSinh.isEmpty()) {
            // Khởi tạo đối tượng DAO để tương tác CSDL
        HocSinhDAO dao = new HocSinhDAO();
            dao.stopService(maHocSinh);
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("hocsinh-list?msg=stopped");
        } else {
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("hocsinh-list?msg=error");
        }
    }
}
