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
 * Servlet quản lý chức năng cập nhật thông tin Xe bus.
 * Bao gồm cả xử lý logic tự động hủy lịch trình nếu xe chuyển sang trạng thái "Bảo dưỡng/Sửa chữa".
 */
@WebServlet(name = "BusUpdateServlet", urlPatterns = {"/bus-update"})
public class BusUpdateServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu GET để hiển thị form cập nhật thông tin xe.
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
            Bus bus = dao.getBusById(id);
            request.setAttribute("bus", bus);
            request.getRequestDispatcher("bus_form.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect("bus-list");
        }
    }

    /**
     * Xử lý yêu cầu POST để cập nhật thông tin Xe bus.
     * Xử lý cảnh báo trùng biển số và gửi thông báo nếu xe vào trạng thái bảo dưỡng.
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
        
        try {
            int busID = Integer.parseInt(request.getParameter("busID"));
            String licensePlate = request.getParameter("licensePlate");
            int capacity = Integer.parseInt(request.getParameter("capacity"));
            String status = request.getParameter("status");

            BusDAO dao = new BusDAO();
            
            // 1. Kiểm tra xem Biển số xe đã được sử dụng bởi xe khác chưa
            if (dao.checkLicensePlateExist(licensePlate, busID)) {
                request.setAttribute("error", "Biển số xe '" + licensePlate + "' đã được sử dụng bởi xe khác!");
                request.setAttribute("bus", new Bus(busID, licensePlate, capacity, status));
                request.getRequestDispatcher("bus_form.jsp").forward(request, response);
                return;
            }

            // 2. Nếu dữ liệu hợp lệ, cập nhật thông tin Xe bus
            Bus b = new Bus(busID, licensePlate, capacity, status);
            dao.updateBus(b);
            
            // 3. Xử lý đặc biệt: Nếu xe chuyển sang trạng thái Bảo dưỡng, cần hủy các lịch trình tương lai gần
            if ("Bảo dưỡng/Sửa chữa".equals(status)) {
                notifyAdminForFutureSchedules(busID);
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        response.sendRedirect("bus-list");
    }
    
    /**
     * Thông báo cho Admin và hủy các lịch trình (trong vòng 2 ngày) khi xe vào trạng thái bảo dưỡng.
     * 
     * @param busID ID của xe bus được cập nhật thành trạng thái "Bảo dưỡng/Sửa chữa"
     */
    private void notifyAdminForFutureSchedules(int busID) {
        dal.UserDAO userDAO = new dal.UserDAO();
        java.util.List<model.User> admins = userDAO.getUsersByRole("ADMIN");
        if (admins.isEmpty()) return;

        dal.NotificationDAO notifDAO = new dal.NotificationDAO();
        dal.BusDAO busDAO = new dal.BusDAO();
        dal.ScheduleDAO scheduleDAO = new dal.ScheduleDAO();
        
        Bus bus = busDAO.getBusById(busID);
        String licensePlate = (bus != null) ? bus.getLicensePlate() : String.valueOf(busID);
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

        java.util.List<model.Schedule> allSchedules = scheduleDAO.getAllSchedules();
        for (model.Schedule s : allSchedules) {
            if (s.getBusID() == busID) {
                if (s.getDate() != null && s.getDate().compareTo(today) >= 0 && "PENDING".equals(s.getStatus())) {
                    long diffInMillies = s.getDate().getTime() - today.getTime();
                    long diffInDays = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (diffInDays <= 2) {
                        String ca = "TO_SCHOOL".equals(s.getDirection()) ? "Sáng" : "Chiều";
                        String msg = "Xe " + licensePlate + " vừa báo bảo dưỡng. Lịch trình ngày " + s.getDate() + " (Ca " + ca + ") đã bị HỦY tự động. Vui lòng phân ca lại! |DATE:" + s.getDate() + "|";
                        for (model.User admin : admins) {
                            notifDAO.insertNotification(admin.getUsername(), msg);
                        }
                        scheduleDAO.deleteSchedule(s.getScheduleID());
                    }
                }
            }
        }
    }
}
