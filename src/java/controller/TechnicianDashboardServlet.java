package controller;

import dal.ScheduleDAO;
import dal.BusDAO;
import model.Schedule;
import model.Bus;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý logic giao diện trang chủ (Dashboard) của Kỹ thuật viên.
 * Nơi theo dõi các sự cố trên đường và quản lý xe bảo dưỡng.
 */
@WebServlet(name = "TechnicianDashboardServlet", urlPatterns = {"/technician-dashboard"})
public class TechnicianDashboardServlet extends HttpServlet {
    /**
     * Tải danh sách các chuyến xe ĐANG GẶP SỰ CỐ, danh sách xe đang được bảo dưỡng
     * và lịch trực kỹ thuật của cá nhân trong ngày. 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"kythuat".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        dal.NotificationDAO notifDAO = new dal.NotificationDAO();
        java.util.List<model.Notification> notifs = notifDAO.getNotificationsByUsername(username);
        long unreadCount = notifs.stream().filter(n -> !n.isRead()).count();
        request.setAttribute("unreadCount", unreadCount);

        ScheduleDAO scheduleDAO = new ScheduleDAO();
        List<Schedule> incidentSchedules = scheduleDAO.getIncidentSchedules();
        request.setAttribute("incidentSchedules", incidentSchedules);

        BusDAO busDAO = new BusDAO();
        
        java.sql.Date scheduleDate = new java.sql.Date(System.currentTimeMillis());
        if (incidentSchedules != null && !incidentSchedules.isEmpty()) {
            scheduleDate = incidentSchedules.get(0).getDate();
        }
        final java.sql.Date finalDate = scheduleDate;

        List<Bus> maintenanceBuses = busDAO.getBusesByDate(finalDate).stream()
                .filter(b -> "Bảo dưỡng/Sửa chữa".equals(b.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        request.setAttribute("maintenanceBuses", maintenanceBuses);
        
        Map<Integer, Bus> busMap = new HashMap<>();
        for(Schedule s : incidentSchedules) {
            if(!busMap.containsKey(s.getBusID())) {
                busMap.put(s.getBusID(), busDAO.getBusById(s.getBusID()));
            }
            if(s.getReplacementBusID() > 0 && !busMap.containsKey(s.getReplacementBusID())) {
                busMap.put(s.getReplacementBusID(), busDAO.getBusById(s.getReplacementBusID()));
            }
        }
        

        request.setAttribute("busMap", busMap);
        
        dal.HocSinhDAO hocSinhDAO = new dal.HocSinhDAO();
        Map<Integer, Integer> studentCountMap = new HashMap<>();
        for (Schedule s : incidentSchedules) {
            int count = hocSinhDAO.countActiveHocSinhByRoute(s.getRouteID(), s.getDate());
            studentCountMap.put(s.getScheduleID(), count);
        }
        request.setAttribute("studentCountMap", studentCountMap);

        int userID = (int) session.getAttribute("userID");
        List<model.TechnicianSchedule> mySchedules = scheduleDAO.getTechnicianSchedulesByUser(userID);
        request.setAttribute("mySchedules", mySchedules);
        
        dal.UserDAO userDAO = new dal.UserDAO();
        boolean isTodayLeaveApproved = userDAO.isLeaveApproved(userID, new java.sql.Date(System.currentTimeMillis()));
        request.setAttribute("isTodayLeaveApproved", isTodayLeaveApproved);
        
        List<Bus> availableBuses = busDAO.getBusesByDate(finalDate).stream()
                .filter(b -> "Sẵn sàng".equals(b.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        request.setAttribute("availableBuses", availableBuses);

        request.getRequestDispatcher("kythuat_dashboard.jsp").forward(request, response);
    }
}
