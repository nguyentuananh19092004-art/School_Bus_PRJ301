package controller;

import dal.StopDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet quản lý chức năng cập nhật thời gian dự kiến tại các điểm dừng (trạm).
 * Chỉ tài khoản có quyền Admin mới được phép thực hiện thao tác này.
 */
@WebServlet(name = "RouteStopUpdateServlet", urlPatterns = {"/update-route-stop"})
public class RouteStopUpdateServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu POST để cập nhật thời gian đi và về tại một điểm dừng trên tuyến.
     * Kiểm tra quyền đăng nhập, lấy dữ liệu thời gian mới và lưu vào cơ sở dữ liệu.
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form cần cập nhật
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        try {
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            int stopID = Integer.parseInt(request.getParameter("stopID"));
            String estimatedTime = request.getParameter("estimatedTime");
            String returnTime = request.getParameter("returnTime");

            StopDAO stopDAO = new StopDAO();
            stopDAO.updateRouteStopTime(routeID, stopID, estimatedTime, returnTime);

            response.sendRedirect("route-management?routeID=" + routeID);
            
        } catch (Exception e) {
            System.out.println("Error updating stop: " + e.getMessage());
            response.sendRedirect("route-management?error=1");
        }
    }
}
