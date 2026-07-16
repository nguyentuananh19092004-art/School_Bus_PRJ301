package controller;

import dal.HocSinhDAO;
import dal.StopDAO;
import dal.NotificationDAO;
import dal.ScheduleDAO;
import model.HocSinh;
import model.Stop;
import model.Notification;
import model.Schedule;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý logic giao diện trang chủ (Dashboard) của Phụ huynh.
 */
@WebServlet(name = "ParentDashboardServlet", urlPatterns = {"/parent-dashboard"})
public class ParentDashboardServlet extends HttpServlet {
    /**
     * Tải thông tin học sinh (con em của phụ huynh), điểm đón hiện tại và lịch sử điểm danh.
     * Theo dõi tiến trình của chuyến xe đang chạy theo thời gian thực (nếu xe đang hoạt động).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"phuhuynh".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        HocSinhDAO hsDAO = new HocSinhDAO();
        HocSinh student = hsDAO.getHocSinhByTenTK(username);
        
        if (student != null) {
            dal.StudentLeaveDAO leaveDAO = new dal.StudentLeaveDAO();
            if (student.getDefaultStopID() != null && leaveDAO.isStudentOnLeave(student.getMaHocSinh(), new java.sql.Date(System.currentTimeMillis()))) {
                student.setTrangThai("Nghỉ");
            }
            java.util.List<java.sql.Date> upcomingLeaves = leaveDAO.getUpcomingLeaves(student.getMaHocSinh());
            request.setAttribute("upcomingLeaves", upcomingLeaves);
            request.setAttribute("student", student);
            
            dal.RouteDAO routeDAO = new dal.RouteDAO();
            List<model.StopRouteOption> stopRouteOptions = routeDAO.getStopRouteOptions();
            request.setAttribute("stopRouteOptions", stopRouteOptions);
            
            StopDAO stopDAO = new StopDAO();
            List<Stop> allStops = stopDAO.getAllStops();
            
            if (student.getDefaultStopID() != null) {
                // Find stop details
                Stop currentStop = allStops.stream().filter(s -> s.getStopID() == student.getDefaultStopID()).findFirst().orElse(null);
                request.setAttribute("currentStop", currentStop);
                
                // Find if there is an active schedule moving towards this stop
                ScheduleDAO scheduleDAO = new ScheduleDAO();
                Schedule activeSchedule = scheduleDAO.getActiveScheduleForStop(student.getDefaultStopID());
                
                if (activeSchedule != null) {
                    dal.BusDAO busDAO = new dal.BusDAO();
                    model.Bus bus = busDAO.getBusById(activeSchedule.getBusID());
                    activeSchedule.setBus(bus);
                    
                    List<Stop> routeStops = stopDAO.getStopsByRoute(activeSchedule.getRouteID());
                    if ("TO_HOME".equals(activeSchedule.getDirection()) || "Về nhà".equals(activeSchedule.getDirection())) {
                        java.util.Collections.reverse(routeStops);
                    }
                    request.setAttribute("routeStops", routeStops);
                    
                    dal.ScheduleProgressDAO progressDAO = new dal.ScheduleProgressDAO();
                    List<model.ScheduleProgress> progresses = progressDAO.getProgressBySchedule(activeSchedule.getScheduleID());
                    java.util.List<Integer> reachedStops = new java.util.ArrayList<>();
                    if (progresses != null) {
                        for (model.ScheduleProgress p : progresses) {
                            reachedStops.add(p.getStopID());
                        }
                    }
                    request.setAttribute("reachedStops", reachedStops);
                }
                
                request.setAttribute("activeSchedule", activeSchedule);
            }
            
            if (student.getPendingStopID() != null && student.getPendingStopID() != -1) {
                Stop pendingStop = allStops.stream().filter(s -> s.getStopID() == student.getPendingStopID()).findFirst().orElse(null);
                request.setAttribute("pendingStop", pendingStop);
            }
            
            dal.AttendanceDAO attDAO = new dal.AttendanceDAO();
            boolean hasBoardedToday = attDAO.hasBoardedToday(student.getMaHocSinh());
            request.setAttribute("hasBoardedToday", hasBoardedToday);
            
            
            NotificationDAO notifDAO = new NotificationDAO();
            List<Notification> notifications = notifDAO.getNotificationsByUsername(username);
            request.setAttribute("notifications", notifications);
        }

        request.getRequestDispatcher("phuhuynh_dashboard.jsp").forward(request, response);
    }
}
