package controller;

import dal.ScheduleProgressDAO;
import dal.NotificationDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "MonitorActionServlet", urlPatterns = {"/monitor-action"})
public class MonitorActionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"giamthi".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("reach_stop".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            int stopID = Integer.parseInt(request.getParameter("stopID"));
            String stopName = request.getParameter("stopName");
            String direction = request.getParameter("direction");
            ScheduleProgressDAO dao = new ScheduleProgressDAO();
            dao.insertProgress(scheduleID, stopID);
            
            boolean isSchoolStop = Boolean.parseBoolean(request.getParameter("isSchoolStop"));
            String assignedStudents = request.getParameter("assignedStudents");
            
            if (isSchoolStop) {
                if ("TO_HOME".equals(direction) && assignedStudents != null && !assignedStudents.isEmpty()) {
                    // Bulk check-in for afternoon (from school)
                    String[] maHocSinhArray = assignedStudents.split(",");
                    dal.AttendanceDAO attDAO = new dal.AttendanceDAO();
                    dal.HocSinhDAO hsDAO = new dal.HocSinhDAO();
                    NotificationDAO notifDAO = new NotificationDAO();
                    for (String ma : maHocSinhArray) {
                        if (!ma.trim().isEmpty()) {
                            attDAO.insertAttendance(scheduleID, ma.trim(), stopID, false, direction);
                            model.HocSinh hs = hsDAO.getHocSinhByMa(ma.trim());
                            if (hs != null) {
                                notifDAO.insertNotification(hs.getTenTK(), "Học sinh " + hs.getTenHocSinh() + " đã lên xe.");
                            }
                        }
                    }
                } else if ("TO_SCHOOL".equals(direction)) {
                    // Bulk arrived notification for morning (at school)
                    dal.AttendanceDAO attDAO = new dal.AttendanceDAO();
                    java.util.List<String> attendedList = attDAO.getAttendedStudents(scheduleID);
                    dal.HocSinhDAO hsDAO = new dal.HocSinhDAO();
                    NotificationDAO notifDAO = new NotificationDAO();
                    for (String ma : attendedList) {
                        model.HocSinh hs = hsDAO.getHocSinhByMa(ma);
                        if (hs != null) {
                            notifDAO.insertNotification(hs.getTenTK(), "Học sinh " + hs.getTenHocSinh() + " đã đến trường an toàn.");
                        }
                    }
                }
            } else if (stopName != null) {
                dal.HocSinhDAO hsDAO = new dal.HocSinhDAO();
                java.util.List<model.HocSinh> hsList = hsDAO.getHocSinhByStopID(stopID);
                NotificationDAO notifDAO = new NotificationDAO();
                String type = "TO_HOME".equals(direction) ? "điểm trả" : "điểm đón";
                for (model.HocSinh hs : hsList) {
                    notifDAO.insertNotification(hs.getTenTK(), "Xe đã đi qua " + type + " " + stopName + ".");
                }
            }
        } else if ("notify_parent".equals(action)) {
            String hocSinhTK = request.getParameter("hocSinhTK");
            String stopName = request.getParameter("stopName");
            String direction = request.getParameter("direction");
            int stopID = 0;
            try {
                stopID = Integer.parseInt(request.getParameter("stopID"));
            } catch (Exception e) {}
            String message = "";
            if ("TO_HOME".equals(direction)) {
                if (stopID == 0) {
                    message = "Xe buýt đã gần đến Trường học để đón học sinh về nhà.";
                } else {
                    message = "Xe buýt đã gần đến điểm trả: " + stopName + ". Phụ huynh vui lòng chuẩn bị đón học sinh!";
                }
            } else {
                if (stopID == 0) {
                    message = "Xe buýt đã gần đến Trường học. Học sinh chuẩn bị xuống xe.";
                } else {
                    message = "Xe buýt đã gần đến điểm đón: " + stopName + ". Phụ huynh vui lòng chuẩn bị cho học sinh ra điểm đón!";
                }
            }
            NotificationDAO dao = new NotificationDAO();
            dao.insertNotification(hocSinhTK, message); // The Username in Notification table matches the HocSinh.TenTK which is the Parent's account.
        } else if ("mark_attendance".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            int stopID = Integer.parseInt(request.getParameter("stopID"));
            String maHocSinh = request.getParameter("maHocSinh");
            boolean isAbsent = Boolean.parseBoolean(request.getParameter("isAbsent"));
            String direction = request.getParameter("direction");
            
            dal.AttendanceDAO dao = new dal.AttendanceDAO();
            dao.insertAttendance(scheduleID, maHocSinh, stopID, isAbsent, direction);
            
            if (!isAbsent) {
                dal.HocSinhDAO hsDAO = new dal.HocSinhDAO();
                model.HocSinh hs = hsDAO.getHocSinhByMa(maHocSinh);
                if (hs != null) {
                    boolean isBoarding = ("TO_SCHOOL".equals(direction) && stopID != 0) || ("TO_HOME".equals(direction) && stopID == 0);
                    String message = isBoarding ? 
                        "Học sinh " + hs.getTenHocSinh() + " đã lên xe." : 
                        "Học sinh " + hs.getTenHocSinh() + " đã xuống xe an toàn.";
                    NotificationDAO notifDAO = new NotificationDAO();
                    notifDAO.insertNotification(hs.getTenTK(), message);
                }
            }
        } else if ("complete_trip".equals(action)) {
            int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
            String direction = request.getParameter("direction");
            ScheduleProgressDAO dao = new ScheduleProgressDAO();
            dao.insertProgress(scheduleID, -1); // -1 marks that the monitor has completed the trip
            
            if ("TO_SCHOOL".equals(direction)) {
                // Notifications are now handled in reach_stop for the school stop.
            }
        }

        response.sendRedirect("monitor-dashboard");
    }
}
