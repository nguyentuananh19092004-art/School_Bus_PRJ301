package controller;

import dal.RouteDAO;
import dal.StopDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Route;
import model.Stop;

/**
 * Servlet quản lý chức năng xem thông tin tuyến đường và các điểm dừng.
 * Cho phép Admin xem danh sách tuyến và chi tiết các trạm (điểm dừng) của từng tuyến.
 */
@WebServlet(name = "RouteManagementServlet", urlPatterns = {"/route-management"})
public class RouteManagementServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu GET để hiển thị trang quản lý tuyến đường.
     * Lấy danh sách tất cả các tuyến và chi tiết các điểm dừng của một tuyến cụ thể nếu được chọn.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (ID tuyến)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        RouteDAO routeDAO = new RouteDAO();
        StopDAO stopDAO = new StopDAO();

        // 1. Lấy tất cả các tuyến đường để hiển thị danh sách
        List<Route> routes = routeDAO.getAllRoutes();
        request.setAttribute("routes", routes);

        // 2. Kiểm tra xem có routeID được chọn không
        String routeIDStr = request.getParameter("routeID");
        if (routeIDStr != null && !routeIDStr.isEmpty()) {
            try {
                int routeID = Integer.parseInt(routeIDStr);
                // Lấy thông tin tuyến đường được chọn
                Route selectedRoute = null;
                for (Route r : routes) {
                    if (r.getRouteID() == routeID) {
                        selectedRoute = r;
                        break;
                    }
                }
                request.setAttribute("selectedRoute", selectedRoute);

                // Lấy danh sách các trạm của tuyến đường này
                List<Stop> stops = stopDAO.getStopsByRoute(routeID);
                request.setAttribute("stops", stops);
                
            } catch (NumberFormatException e) {
                // Xử lý lỗi ép kiểu nếu cần
                System.out.println("Invalid routeID: " + e.getMessage());
            }
        }

        request.getRequestDispatcher("route_management.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST tương tự như GET để tiện cho các thao tác chuyển hướng form.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
