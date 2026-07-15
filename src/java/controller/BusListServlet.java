package controller;

import dal.BusDAO;
import model.Bus;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Servlet xử lý việc hiển thị danh sách Xe bus trong hệ thống.
 * Hỗ trợ hiển thị danh sách xe cùng với trạng thái trong một ngày cụ thể.
 */
@WebServlet(name = "BusListServlet", urlPatterns = {"/bus-list"})
public class BusListServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu GET để lấy và hiển thị danh sách Xe bus.
     * Lấy tham số ngày từ yêu cầu, nếu không có sẽ dùng ngày hiện tại.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dateParam = request.getParameter("date");
        Date selectedDate;
        if (dateParam != null && !dateParam.isEmpty()) {
            selectedDate = Date.valueOf(dateParam);
        } else {
            selectedDate = Date.valueOf(LocalDate.now());
        }

        BusDAO dao = new BusDAO();
        List<Bus> list = dao.getBusesByDate(selectedDate);
        
        request.setAttribute("busList", list);
        request.setAttribute("selectedDate", selectedDate.toString());
        request.getRequestDispatcher("bus_list.jsp").forward(request, response);
    }
}
