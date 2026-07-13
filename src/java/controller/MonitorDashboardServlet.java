package controller;

import dal.ScheduleDAO;
import dal.StopDAO;
import dal.UserDAO;
import dal.HocSinhDAO;
import dal.ScheduleProgressDAO;
import dal.BusDAO;
import model.Schedule;
import model.Stop;
import model.User;
import model.HocSinh;
import model.ScheduleProgress;
import dal.AttendanceDAO;
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

@WebServlet(name = "MonitorDashboardServlet", urlPatterns = {"/monitor-dashboard"})
public class MonitorDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"giamthi".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String username = (String) session.getAttribute("username");
        UserDAO userDAO = new UserDAO();
        User monitor = userDAO.getUserByUsername(username);

        dal.NotificationDAO notifDAO = new dal.NotificationDAO();
        java.util.List<model.Notification> notifs = notifDAO.getNotificationsByUsername(username);
        long unreadCount = notifs.stream().filter(n -> !n.isRead()).count();
        request.setAttribute("unreadCount", unreadCount);

        if (monitor != null) {
            ScheduleDAO scheduleDAO = new ScheduleDAO();
            Schedule activeSchedule = scheduleDAO.getActiveScheduleByMonitor(monitor.getUserID());

            if (activeSchedule != null) {
                request.setAttribute("activeSchedule", activeSchedule);
                
                request.setAttribute("driver", userDAO.getUserById(activeSchedule.getDriverID()));
                
                BusDAO busDAO = new BusDAO();
                request.setAttribute("bus", busDAO.getBusById(activeSchedule.getBusID()));

                StopDAO stopDAO = new StopDAO();
                List<Stop> stops = stopDAO.getStopsByRoute(activeSchedule.getRouteID());
                
                if ("TO_HOME".equals(activeSchedule.getDirection())) {
                    java.util.Collections.reverse(stops);
                }
                
                request.setAttribute("stops", stops);

                HocSinhDAO hsDAO = new HocSinhDAO();
                List<Schedule> allSchedules = scheduleDAO.getAllSchedules();
                List<Schedule> relatedSchedules = allSchedules.stream()
                        .filter(s -> s.getRouteID() == activeSchedule.getRouteID() &&
                                     s.getDate().toString().equals(activeSchedule.getDate().toString()) &&
                                     s.getDirection().equals(activeSchedule.getDirection()) &&
                                     !"CANCELLED".equals(s.getStatus()))
                        .sorted((s1, s2) -> Integer.compare(s1.getScheduleID(), s2.getScheduleID()))
                        .collect(java.util.stream.Collectors.toList());

                dal.StudentLeaveDAO leaveDAO = new dal.StudentLeaveDAO();
                java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                
                List<HocSinh> allRouteStudents = new java.util.ArrayList<>();
                for (Stop s : stops) {
                    List<HocSinh> hsList = hsDAO.getHocSinhByStopID(s.getStopID());
                    for (HocSinh hs : hsList) {
                        if ("Sử dụng".equals(hs.getTrangThai()) || "Nghỉ".equals(hs.getTrangThai())) {
                            if (leaveDAO.isStudentOnLeave(hs.getMaHocSinh(), today)) {
                                hs.setTrangThai("Nghỉ");
                            }
                            allRouteStudents.add(hs);
                        }
                    }
                }
                allRouteStudents.sort((h1, h2) -> h1.getMaHocSinh().compareTo(h2.getMaHocSinh()));

                List<HocSinh> assignedStudents = new java.util.ArrayList<>();
                int currentIndex = 0;
                for (Schedule s : relatedSchedules) {
                    model.Bus b = busDAO.getBusById(s.getBusID());
                    int capacity = b != null ? Math.max(0, b.getCapacity() - 2) : 0;
                    
                    if (s.getScheduleID() == activeSchedule.getScheduleID()) {
                        int endIndex = Math.min(currentIndex + capacity, allRouteStudents.size());
                        if (currentIndex < allRouteStudents.size()) {
                            assignedStudents = new java.util.ArrayList<>(allRouteStudents.subList(currentIndex, endIndex));
                        }
                        break;
                    }
                    currentIndex += capacity;
                }

                AttendanceDAO attendanceDAO = new AttendanceDAO();
                List<String> attendedStudents = attendanceDAO.getAttendedStudents(activeSchedule.getScheduleID());
                request.setAttribute("attendedStudents", attendedStudents);

                int schoolStopID = -1;
                if (!stops.isEmpty()) {
                    schoolStopID = ("TO_HOME".equals(activeSchedule.getDirection())) ? stops.get(0).getStopID() : stops.get(stops.size() - 1).getStopID();
                }

                List<String> checkedAtStops = attendanceDAO.getStudentsCheckedAtNonSchoolStops(activeSchedule.getScheduleID(), schoolStopID);
                request.setAttribute("checkedAtStops", checkedAtStops);

                Map<Integer, List<HocSinh>> studentsByStop = new HashMap<>();
                for (int i = 0; i < stops.size(); i++) {
                    Stop s = stops.get(i);
                    boolean isSchoolStop = ("TO_SCHOOL".equals(activeSchedule.getDirection()) && i == stops.size() - 1) || 
                                           ("TO_HOME".equals(activeSchedule.getDirection()) && i == 0);
                    if (isSchoolStop) {
                        if ("TO_SCHOOL".equals(activeSchedule.getDirection())) {
                            // Alighting stop: only show students who boarded
                            List<HocSinh> boardedStudents = assignedStudents.stream()
                                    .filter(hs -> attendedStudents.contains(hs.getMaHocSinh()))
                                    .collect(java.util.stream.Collectors.toList());
                            studentsByStop.put(s.getStopID(), boardedStudents);
                        } else {
                            // Boarding stop: show all assigned students
                            studentsByStop.put(s.getStopID(), assignedStudents);
                        }
                    } else {
                        List<HocSinh> stopStudents = assignedStudents.stream()
                                .filter(hs -> hs.getDefaultStopID() == s.getStopID())
                                .collect(java.util.stream.Collectors.toList());
                        if ("TO_HOME".equals(activeSchedule.getDirection())) {
                            // Alighting stop: only show students who boarded at school
                            stopStudents = stopStudents.stream()
                                    .filter(hs -> attendedStudents.contains(hs.getMaHocSinh()))
                                    .collect(java.util.stream.Collectors.toList());
                        }
                        studentsByStop.put(s.getStopID(), stopStudents);
                    }
                }
                request.setAttribute("studentsByStop", studentsByStop);

                ScheduleProgressDAO progressDAO = new ScheduleProgressDAO();
                List<ScheduleProgress> progresses = progressDAO.getProgressBySchedule(activeSchedule.getScheduleID());
                List<Integer> reachedStops = progresses.stream().map(ScheduleProgress::getStopID).collect(java.util.stream.Collectors.toList());
                request.setAttribute("reachedStops", reachedStops);
            }
        }

        request.getRequestDispatcher("giamthi_dashboard.jsp").forward(request, response);
    }
}
