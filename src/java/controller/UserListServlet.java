package controller;

import dal.UserDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Servlet xử lý việc hiển thị danh sách người dùng (Nhân viên) theo vai trò.
 * Hỗ trợ lọc danh sách người dùng theo vai trò (DRIVER, MONITOR, v.v.) và ngày làm việc.
 */
@WebServlet(name = "UserListServlet", urlPatterns = {"/user-list"})
public class UserListServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để lấy và hiển thị danh sách người dùng.
     * Lấy tham số vai trò và ngày từ yêu cầu, nếu không có sẽ dùng giá trị mặc định.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. Kiểm tra tham số vai trò, nếu không có mặc định lấy DRIVER (Tài xế)
        String role = request.getParameter("role"); // DRIVER or MONITOR
        if (role == null || role.isEmpty()) {
            role = "DRIVER"; // default
        }
        
        // 2. Lấy tham số ngày lọc, nếu không có mặc định lấy ngày hôm nay
        String dateParam = request.getParameter("date");
        Date selectedDate;
        if (dateParam != null && !dateParam.isEmpty()) {
            selectedDate = Date.valueOf(dateParam);
        } else {
            selectedDate = Date.valueOf(LocalDate.now());
        }
        
        // 3. Lấy danh sách user theo vai trò và ngày (có xét logic nghỉ phép)
        UserDAO dao = new UserDAO();
        List<User> userList = dao.getUsersByRoleAndDate(role, selectedDate);
        
        // 4. Trả kết quả về View
        request.setAttribute("userList", userList);
        request.setAttribute("role", role);
        request.setAttribute("selectedDate", selectedDate.toString());
        request.getRequestDispatcher("user_list.jsp").forward(request, response);
    }
}
