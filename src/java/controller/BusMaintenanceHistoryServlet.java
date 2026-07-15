package controller;

import dal.BusDAO;
import model.Bus;
import model.BusMaintenance;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet quản lý chức năng xem lịch sử bảo dưỡng của một xe bus cụ thể.
 * Chỉ cho phép người dùng có quyền admin (hoặc quyền tương đương) truy cập.
 */
@WebServlet(name = "BusMaintenanceHistoryServlet", urlPatterns = {"/bus-maintenance-history"})
public class BusMaintenanceHistoryServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để lấy và hiển thị chi tiết lịch sử bảo dưỡng của xe.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (ID xe)
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

        try {
            int busID = Integer.parseInt(request.getParameter("id"));
            BusDAO busDAO = new BusDAO();
            Bus bus = busDAO.getBusById(busID);
            
            if (bus != null) {
                List<BusMaintenance> history = busDAO.getBusMaintenances(busID);
                request.setAttribute("bus", bus);
                request.setAttribute("history", history);
                // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("bus_maintenance_history.jsp").forward(request, response);
            } else {
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("bus-list");
            }
        } catch (Exception e) {
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("bus-list");
        }
    }
}
