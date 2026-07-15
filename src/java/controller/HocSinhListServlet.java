package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import dal.RouteDAO;
import model.Route;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HocSinh;

/**
 * Servlet xử lý việc hiển thị danh sách Học sinh trong hệ thống.
 * Hỗ trợ hiển thị danh sách học sinh cùng với tuyến đường mà học sinh đang đăng ký.
 */
@WebServlet(name = "HocSinhListServlet", urlPatterns = {"/hocsinh-list"})
public class HocSinhListServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để lấy và hiển thị danh sách toàn bộ học sinh.
     * Ánh xạ thông tin tuyến đường của từng học sinh để hiển thị mã tuyến tương ứng.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HocSinhDAO dao = new HocSinhDAO();
        List<HocSinh> list = dao.getAllHocSinh();
        
        RouteDAO routeDAO = new RouteDAO();
        List<Route> routes = routeDAO.getAllRoutes();
        Map<Integer, String> routeMap = new HashMap<>();
        for (Route r : routes) {
            routeMap.put(r.getRouteID(), r.getRouteCode());
        }
        
        request.setAttribute("listHS", list);
        request.setAttribute("routeMap", routeMap);
        request.getRequestDispatcher("hocsinh_list.jsp").forward(request, response);
    }
}
