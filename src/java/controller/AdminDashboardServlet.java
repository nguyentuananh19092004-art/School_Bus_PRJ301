package controller;

import dal.DashboardDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/AdminDashboardServlet"})
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        DashboardDAO dao = new DashboardDAO();
        int totalStudents = dao.countTable("HocSinh");
        int totalBuses = dao.countTable("Buses");
        int totalRoutes = dao.countTable("Routes");
        int totalUsers = dao.countTable("Users");

        request.setAttribute("totalStudents", totalStudents);
        request.setAttribute("totalBuses", totalBuses);
        request.setAttribute("totalRoutes", totalRoutes);
        request.setAttribute("totalUsers", totalUsers);

        dal.UserDAO userDAO = new dal.UserDAO();
        boolean hasPendingLeaves = !userDAO.getPendingLeaves().isEmpty();
        request.setAttribute("hasPendingLeaves", hasPendingLeaves);

        dal.NotificationDAO notifDAO = new dal.NotificationDAO();
        String username = (String) session.getAttribute("username");
        java.util.List<model.Notification> notifications = notifDAO.getNotificationsByUsername(username);
        request.setAttribute("notifications", notifications);

        dal.BusDAO busDAO = new dal.BusDAO();
        java.util.List<model.Bus> allActiveBusesForJS = busDAO.getAllBusesIncludingDeleted().stream()
            .filter(b -> "Sẵn sàng".equals(b.getStatus()) || "Hoạt động".equals(b.getStatus()))
            .collect(java.util.stream.Collectors.toList());
            
        dal.ScheduleDAO sDao = new dal.ScheduleDAO();
        java.util.List<model.Schedule> allSchedules = sDao.getAllSchedules();
        java.util.Map<Integer, java.util.List<Integer>> availableBusesMap = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> studentCountMap = new java.util.HashMap<>();
        dal.HocSinhDAO hsDao = new dal.HocSinhDAO();
        
        for (model.Notification n : notifications) {
            String text = n.getMessage();
            if (text.contains("|SCHEDULE_ID:")) {
                int start = text.indexOf("|SCHEDULE_ID:") + 13;
                int end = text.indexOf("|", start);
                int schId = Integer.parseInt(text.substring(start, end));
                model.Schedule sch = allSchedules.stream().filter(s -> s.getScheduleID() == schId).findFirst().orElse(null);
                if (sch != null) {
                    int actualStudents = hsDao.countActiveHocSinhByRoute(sch.getRouteID(), sch.getDate());
                    studentCountMap.put(schId, actualStudents);
                    
                    java.util.List<model.Bus> busesOnThatDate = busDAO.getBusesByDate(sch.getDate());
                    
                    java.util.List<Integer> assignedBusIDs = allSchedules.stream()
                        .filter(s -> s.getDate().toString().equals(sch.getDate().toString()) 
                                && s.getDirection().equals(sch.getDirection())
                                && !"CANCELLED".equals(s.getStatus()))
                        .map(model.Schedule::getBusID)
                        .collect(java.util.stream.Collectors.toList());
                    
                    java.util.List<Integer> available = busesOnThatDate.stream()
                        .filter(b -> !"Bảo dưỡng/Sửa chữa".equals(b.getStatus()))
                        .map(model.Bus::getBusID)
                        .filter(id -> !assignedBusIDs.contains(id))
                        .collect(java.util.stream.Collectors.toList());
                        
                    availableBusesMap.put(schId, available);
                }
            }
        }
        
        request.setAttribute("allActiveBuses", allActiveBusesForJS);
        request.setAttribute("availableBusesMap", availableBusesMap);
        request.setAttribute("studentCountMap", studentCountMap);

        request.getRequestDispatcher("admin_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("mark_read".equals(action)) {
            int notifID = Integer.parseInt(request.getParameter("notifID"));
            dal.NotificationDAO dao = new dal.NotificationDAO();
            dao.markAsRead(notifID);
        } else if ("redirect_to_schedule".equals(action)) {
            String scheduleIdStr = request.getParameter("scheduleID");
            String targetDateStr = request.getParameter("targetDate");
            int notifID = Integer.parseInt(request.getParameter("notifID"));
            
            dal.NotificationDAO nDao = new dal.NotificationDAO();
            nDao.markAsRead(notifID);
            
            if (targetDateStr != null && !targetDateStr.isEmpty()) {
                response.sendRedirect("ScheduleServlet?selectedDate=" + targetDateStr);
                return;
            } else if (scheduleIdStr != null && !scheduleIdStr.isEmpty()) {
                dal.ScheduleDAO sDao = new dal.ScheduleDAO();
                model.Schedule sch = sDao.getScheduleById(Integer.parseInt(scheduleIdStr));
                if (sch != null) {
                    response.sendRedirect("ScheduleServlet?selectedDate=" + sch.getDate().toString());
                    return;
                }
            }
        } else if ("change_bus".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            int newBusID = Integer.parseInt(request.getParameter("newBusID"));
            int notifID = Integer.parseInt(request.getParameter("notifID"));
            
            dal.ScheduleDAO sDao = new dal.ScheduleDAO();
            model.Schedule sch = sDao.getScheduleById(scheduleID);
            
            sDao.updateScheduleBus(scheduleID, newBusID);
            
            dal.NotificationDAO nDao = new dal.NotificationDAO();
            nDao.markAsRead(notifID);
            
            if (sch != null) {
                dal.BusDAO bDao = new dal.BusDAO();
                model.Bus newBus = bDao.getBusById(newBusID);
                dal.HocSinhDAO hsDao = new dal.HocSinhDAO();
                int students = hsDao.countActiveHocSinhByRoute(sch.getRouteID(), sch.getDate());
                
                if (newBus != null && newBus.getCapacity() < students) {
                    // Tạo thông báo vào inbox của Admin để tránh trường hợp Admin quên phân thêm xe
                    String msg = "Chưa đủ xe để chở học sinh cho Tuyến " + sch.getRouteID() + " ngày " + sch.getDate() + " (" + newBus.getCapacity() + "/" + students + " chỗ). Vui lòng phân thêm xe!";
                    dal.UserDAO uDao = new dal.UserDAO();
                    java.util.List<model.User> admins = uDao.getUsersByRole("ADMIN");
                    for (model.User admin : admins) {
                        nDao.insertNotification(admin.getUsername(), msg);
                    }
                    
                    response.sendRedirect("ScheduleServlet?msg=need_more_bus&selectedDate=" + sch.getDate().toString() + "&direction=" + sch.getDirection() + "&routeID=" + sch.getRouteID());
                    return;
                }
            }
        }
        response.sendRedirect("AdminDashboardServlet");
    }
}
