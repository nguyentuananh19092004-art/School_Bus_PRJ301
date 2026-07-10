package controller;

import dal.ScheduleDAO;
import dal.BusDAO;
import dal.UserDAO;
import model.Schedule;
import model.Bus;
import model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "DriverDashboardServlet", urlPatterns = {"/driver-dashboard"})
public class DriverDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"taixe".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        UserDAO userDAO = new UserDAO();
        User driver = userDAO.getUserByUsername(username);

        dal.NotificationDAO notifDAO = new dal.NotificationDAO();
        java.util.List<model.Notification> notifs = notifDAO.getNotificationsByUsername(username);
        long unreadCount = notifs.stream().filter(n -> !n.isRead()).count();
        request.setAttribute("unreadCount", unreadCount);

        if (driver != null) {
            ScheduleDAO scheduleDAO = new ScheduleDAO();
            Schedule activeSchedule = scheduleDAO.getActiveScheduleByDriver(driver.getUserID());

            if (activeSchedule != null) {
                request.setAttribute("activeSchedule", activeSchedule);
                
                BusDAO busDAO = new BusDAO();
                request.setAttribute("bus", busDAO.getBusById(activeSchedule.getBusID()));
                
                request.setAttribute("monitor", userDAO.getUserById(activeSchedule.getMonitorID()));

                dal.StopDAO stopDAO = new dal.StopDAO();
                java.util.List<model.Stop> stops = stopDAO.getStopsByRoute(activeSchedule.getRouteID());
                if (stops != null && !stops.isEmpty()) {
                    if ("Về nhà".equals(activeSchedule.getDirection())) {
                        java.util.Collections.reverse(stops);
                    }
                    request.setAttribute("firstStop", stops.get(0));
                }
                
                dal.ScheduleProgressDAO progressDAO = new dal.ScheduleProgressDAO();
                java.util.List<model.ScheduleProgress> progresses = progressDAO.getProgressBySchedule(activeSchedule.getScheduleID());
                java.util.List<Integer> reachedStops = new java.util.ArrayList<>();
                if (progresses != null) {
                    for (model.ScheduleProgress p : progresses) {
                        reachedStops.add(p.getStopID());
                    }
                }
                request.setAttribute("reachedStops", reachedStops);
            }
        }

        request.getRequestDispatcher("taixe_dashboard.jsp").forward(request, response);
    }
}

