package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet quản lý chức năng xóa Học sinh khỏi hệ thống.
 * Hỗ trợ chuyển đổi trạng thái của học sinh thành "Đã xóa" (xóa mềm).
 */
@WebServlet(name = "HocSinhDeleteServlet", urlPatterns = {"/hocsinh-delete"})
public class HocSinhDeleteServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để thực hiện xóa mềm Học sinh dựa trên Mã cung cấp.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (Mã học sinh)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String maHocSinh = request.getParameter("id");
        HocSinhDAO dao = new HocSinhDAO();
        
        // 1. Thực hiện xóa mềm học sinh theo mã
        dao.deleteHocSinh(maHocSinh);
        
        // 2. Quay lại danh sách học sinh
        response.sendRedirect("hocsinh-list");
    }
}
