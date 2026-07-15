package controller;

import dal.BusDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet quản lý chức năng xóa Xe bus.
 * Hỗ trợ chuyển đổi trạng thái của xe thành "Đã xóa" (xóa mềm).
 */
@WebServlet(name = "BusDeleteServlet", urlPatterns = {"/bus-delete"})
public class BusDeleteServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu GET để thực hiện xóa mềm xe bus dựa trên ID cung cấp.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (ID xe)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idRaw = request.getParameter("id");
        try {
            int id = Integer.parseInt(idRaw);
            BusDAO dao = new BusDAO();
            
            // 1. Thực hiện xóa mềm xe bus theo ID
            dao.deleteBus(id);
        } catch (NumberFormatException e) {
            System.out.println(e);
        }
        
        // 2. Chuyển hướng người dùng về lại trang danh sách xe bus
        response.sendRedirect("bus-list");
    }
}
