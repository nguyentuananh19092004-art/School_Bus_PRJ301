package controller;

import dal.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet quản lý chức năng xóa người dùng (Nhân viên).
 * Hỗ trợ chuyển đổi trạng thái của người dùng (xóa mềm).
 */
@WebServlet(name = "UserDeleteServlet", urlPatterns = {"/user-delete"})
public class UserDeleteServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để thực hiện xóa người dùng dựa trên ID cung cấp.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (ID người dùng)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        int id = Integer.parseInt(request.getParameter("id"));
        String role = request.getParameter("role");
        
        // Khởi tạo đối tượng DAO để tương tác CSDL
        UserDAO dao = new UserDAO();
        dao.deleteUser(id);
        
        // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("user-list?role=" + role);
    }
}
