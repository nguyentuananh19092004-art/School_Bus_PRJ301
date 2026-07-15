package controller;

import dal.ScheduleDAO;
import java.io.IOException;
import java.sql.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet quản lý chức năng xếp lịch làm việc cho Kỹ thuật viên (Bảo dưỡng).
 * Cung cấp chức năng thêm và xóa ca trực cho nhân viên bảo trì.
 */
@WebServlet(name = "TechnicianScheduleServlet", urlPatterns = {"/tech-schedule"})
public class TechnicianScheduleServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu POST để thêm mới hoặc xóa lịch trực của Kỹ thuật viên.
     * Kiểm tra tính hợp lệ về ngày và trạng thái nghỉ phép trước khi thêm.
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        String action = request.getParameter("action");
        // Khởi tạo đối tượng DAO để tương tác CSDL
        ScheduleDAO dao = new ScheduleDAO();

        if ("delete".equals(action)) {
            String idRaw = request.getParameter("id");
            String selectedDate = request.getParameter("selectedDate");
            try {
                int id = Integer.parseInt(idRaw);
                boolean success = dao.deleteTechnicianSchedule(id);
                if (success) {
                    // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?selectedDate=" + selectedDate + "&msg=tech_deleted");
                } else {
                    // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?selectedDate=" + selectedDate + "&msg=tech_error");
                }
            } catch (Exception e) {
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?selectedDate=" + selectedDate + "&msg=tech_error");
            }
            return;
        }

        // Default: Add
        try {
            int technicianID = Integer.parseInt(request.getParameter("technicianID"));
            Date date = Date.valueOf(request.getParameter("date"));

            java.time.LocalDate scheduleDate = date.toLocalDate();
            java.time.LocalDate today = java.time.LocalDate.now();
            
            if (scheduleDate.isBefore(today)) {
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_past_date");
                return;
            }

            dal.UserDAO userDAO = new dal.UserDAO();
            if (userDAO.isLeaveApproved(technicianID, date)) {
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_on_leave");
                return;
            }

            boolean success = dao.insertTechnicianSchedule(technicianID, date);
            if (success) {
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_success");
            } else {
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_conflict");
            }
        } catch (Exception e) {
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("ScheduleServlet?msg=tech_error");
        }
    }

    /**
     * Xử lý yêu cầu GET bằng cách chuyển tiếp sang phương thức doPost.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        doPost(request, response);
    }
}
