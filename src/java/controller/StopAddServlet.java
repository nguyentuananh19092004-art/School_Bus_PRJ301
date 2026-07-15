package controller;

import dal.StopDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Stop;

/**
 * Servlet quản lý chức năng thêm điểm dừng (trạm) mới vào một tuyến đường.
 * Chỉ tài khoản Admin mới có quyền truy cập và thực hiện thao tác thêm trạm.
 */
@WebServlet(name = "StopAddServlet", urlPatterns = {"/add-stop"})
public class StopAddServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu POST để thêm một điểm dừng mới.
     * Tạo mới một điểm dừng (Stop) sau đó gắn nó vào tuyến đường với thứ tự hiển thị tự động tăng.
     * 
     * @param request đối tượng HttpServletRequest chứa thông tin về điểm dừng (tên, địa chỉ, tọa độ, thời gian)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        request.setCharacterEncoding("UTF-8");
        
        // Kiểm tra quyền đăng nhập qua Session
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("dang_nhap.jsp");
            return;
        }

        try {
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            String stopName = request.getParameter("stopName");
            String address = request.getParameter("address");
            double lat = Double.parseDouble(request.getParameter("latitude"));
            double lng = Double.parseDouble(request.getParameter("longitude"));
            String estimatedTime = request.getParameter("estimatedTime");
            String returnTime = request.getParameter("returnTime");

            StopDAO stopDAO = new StopDAO();
            
            // Create stop
            Stop newStop = new Stop(0, stopName, address, lat, lng);
            int stopID = stopDAO.insertStop(newStop);

            if (stopID > 0) {
                // Determine order. Usually add before the last stop (the school).
                // Or just append it. Let's just append it for simplicity.
                int maxOrder = stopDAO.getMaxStopOrder(routeID);
                stopDAO.addStopToRoute(routeID, stopID, maxOrder + 1, estimatedTime, returnTime);
            }

            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("route-management?routeID=" + routeID);
            
        } catch (Exception e) {
            System.out.println("Error adding stop: " + e.getMessage());
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("route-management?error=1");
        }
    }
}
