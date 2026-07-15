package controller;

import dal.BusDAO;
import model.Bus;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet quản lý chức năng thêm mới Xe bus vào hệ thống.
 * Cung cấp form tạo mới và xử lý dữ liệu được gửi lên từ form.
 */
@WebServlet(name = "BusCreateServlet", urlPatterns = {"/bus-create"})
public class BusCreateServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu GET để hiển thị form thêm mới Xe bus.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("bus_form.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST để thêm mới Xe bus.
     * Kiểm tra tính hợp lệ của dữ liệu (trùng biển số xe).
     * Nếu có lỗi, quay lại form với thông báo lỗi. Nếu thành công, thêm vào cơ sở dữ liệu và chuyển hướng.
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String licensePlate = request.getParameter("licensePlate");
        int capacity = Integer.parseInt(request.getParameter("capacity"));
        String status = request.getParameter("status");

        BusDAO dao = new BusDAO();
        
        // 1. Kiểm tra xem Biển số xe đã tồn tại trong hệ thống chưa
        if (dao.checkLicensePlateExist(licensePlate, 0)) {
            request.setAttribute("error", "Biển số xe '" + licensePlate + "' đã tồn tại trong hệ thống!");
            request.setAttribute("bus", new Bus(0, licensePlate, capacity, status));
            request.getRequestDispatcher("bus_form.jsp").forward(request, response);
            return;
        }

        // 2. Nếu dữ liệu hợp lệ, tạo mới Xe bus
        Bus b = new Bus(0, licensePlate, capacity, status);
        dao.insertBus(b);

        response.sendRedirect("bus-list");
    }
}
