package controller;

import dal.BusDAO;
import dal.RouteDAO;
import dal.ScheduleDAO;
import dal.UserDAO;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Bus;
import model.Route;
import model.Schedule;
import model.User;

/**
 * Servlet quản lý chức năng xếp lịch (phân ca) cho tài xế, giám thị và xe bus.
 * Là trung tâm xử lý logic lịch trình, bao gồm kiểm tra xung đột, sức chứa, trạng thái nhân sự (nghỉ phép), và tình trạng xe.
 */
@WebServlet(name = "ScheduleServlet", urlPatterns = {"/ScheduleServlet"})
public class ScheduleServlet extends HttpServlet {

    /**
     * Xử lý yêu cầu GET để hiển thị giao diện quản lý lịch trình theo ngày.
     * Tính toán số lượng học sinh, kiểm tra năng lực đáp ứng, và lọc danh sách nhân sự/xe sẵn sàng.
     * Xử lý cả chức năng xóa lịch trình thông qua tham số `action`.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String action = request.getParameter("action");
        if ("delete".equals(action)) {
            String selectedDateStr = request.getParameter("selectedDate");
            String dateParamStr = (selectedDateStr != null && !selectedDateStr.isEmpty()) ? "&selectedDate=" + selectedDateStr : "";
            try {
                int id = Integer.parseInt(request.getParameter("id"));
                ScheduleDAO sDao = new ScheduleDAO();
                boolean success = sDao.deleteSchedule(id);
                if (success) {
                    response.sendRedirect("ScheduleServlet?msg=deleted" + dateParamStr);
                } else {
                    response.sendRedirect("ScheduleServlet?msg=error" + dateParamStr);
                }
                return;
            } catch (Exception e) {
                response.sendRedirect("ScheduleServlet?msg=error" + dateParamStr);
                return;
            }
        }

        UserDAO userDAO = new UserDAO();
        BusDAO busDAO = new BusDAO();
        RouteDAO routeDAO = new RouteDAO();
        ScheduleDAO scheduleDAO = new ScheduleDAO();

        // Handle selectedDate
        String dateParam = request.getParameter("selectedDate");
        java.time.LocalDate selectedDate;
        if (dateParam != null && !dateParam.isEmpty()) {
            selectedDate = java.time.LocalDate.parse(dateParam);
        } else {
            selectedDate = java.time.LocalDate.now().plusDays(1); // Default to tomorrow
        }
        java.sql.Date sqlSelectedDate = java.sql.Date.valueOf(selectedDate);
        request.setAttribute("selectedDate", selectedDate.toString());

        List<User> drivers = userDAO.getUsersByRole("DRIVER").stream().filter(u -> "Sẵn sàng".equals(u.getStatus()) && !userDAO.isLeaveApproved(u.getUserID(), sqlSelectedDate)).collect(Collectors.toList());
        List<User> monitors = userDAO.getUsersByRole("MONITOR").stream().filter(u -> "Sẵn sàng".equals(u.getStatus()) && !userDAO.isLeaveApproved(u.getUserID(), sqlSelectedDate)).collect(Collectors.toList());
        List<User> technicians = userDAO.getUsersByRole("TECHNICIAN").stream().filter(u -> "Sẵn sàng".equals(u.getStatus()) && !userDAO.isLeaveApproved(u.getUserID(), sqlSelectedDate)).collect(Collectors.toList());
        
        List<Bus> allBuses = busDAO.getBusesByDate(sqlSelectedDate);
        List<User> allUsersInc = userDAO.getAllUsersIncludingDeleted();
        List<User> allDrivers = allUsersInc.stream().filter(u -> "DRIVER".equals(u.getRole())).collect(Collectors.toList());
        List<User> allMonitors = allUsersInc.stream().filter(u -> "MONITOR".equals(u.getRole())).collect(Collectors.toList());
        List<Bus> allHistoricalBuses = busDAO.getAllBusesIncludingDeleted();
        
        request.setAttribute("allBuses", allBuses);
        request.setAttribute("allHistoricalBuses", allHistoricalBuses);
        request.setAttribute("allDrivers", allDrivers);
        request.setAttribute("allMonitors", allMonitors);
        
        List<Bus> buses = allBuses.stream().filter(b -> !"Bảo dưỡng/Sửa chữa".equals(b.getStatus())).collect(Collectors.toList());
        List<Route> routes = routeDAO.getAllRoutes();
        List<Schedule> schedules = scheduleDAO.getAllSchedules();
        
        dal.HocSinhDAO hsDAO = new dal.HocSinhDAO();
        java.util.Map<Integer, Integer> routeStudentCounts = new java.util.HashMap<>();
        List<String> capacityWarnings = new java.util.ArrayList<>();
        
        // Filter schedules to only show the selected date
        schedules = schedules.stream().filter(s -> s.getDate().toString().equals(sqlSelectedDate.toString())).collect(Collectors.toList());
        
        for (Route r : routes) {
            int students = hsDAO.countActiveHocSinhByRoute(r.getRouteID(), sqlSelectedDate);
            routeStudentCounts.put(r.getRouteID(), students);
            
            if (students > 0) {
                int optimal9 = students / 7;
                int remainder = students % 7;
                int optimal7 = 0;
                if (remainder > 0) {
                    if (remainder <= 5) {
                        optimal7 = 1;
                    } else {
                        optimal9++;
                    }
                }
                String suggestion = (optimal9 > 0 ? optimal9 + " xe 9 chỗ " : "") + (optimal7 > 0 ? optimal7 + " xe 7 chỗ" : "");
                
                int assignedSchool = 0;
                int assignedHome = 0;
                for (Schedule s : schedules) {
                    if (s.getRouteID() == r.getRouteID() && s.getDate().toString().equals(sqlSelectedDate.toString())) {
                        Bus b = allBuses.stream().filter(bus -> bus.getBusID() == s.getBusID()).findFirst().orElse(null);
                        if (b != null) {
                            if ("TO_SCHOOL".equals(s.getDirection())) assignedSchool += (b.getCapacity() - 2);
                            if ("TO_HOME".equals(s.getDirection())) assignedHome += (b.getCapacity() - 2);
                        }
                    }
                }
                
                if (assignedSchool < students) {
                    capacityWarnings.add("Tuyến " + r.getRouteName() + " (Đến trường ngày " + selectedDate.toString() + "): Có " + students + " học sinh đăng ký. Đã gán: " + assignedSchool + ". Còn thiếu: " + (students - assignedSchool) + " chỗ. Đề xuất: " + suggestion);
                }
                if (assignedHome < students) {
                    capacityWarnings.add("Tuyến " + r.getRouteName() + " (Về nhà ngày " + selectedDate.toString() + "): Có " + students + " học sinh đăng ký. Đã gán: " + assignedHome + ". Còn thiếu: " + (students - assignedHome) + " chỗ. Đề xuất: " + suggestion);
                }
            }
        }
        request.setAttribute("routeStudentCounts", routeStudentCounts);
        request.setAttribute("capacityWarnings", capacityWarnings);
        
        List<model.TechnicianSchedule> techSchedules = scheduleDAO.getTechnicianSchedules();
        techSchedules = techSchedules.stream().filter(ts -> ts.getDate().toString().equals(sqlSelectedDate.toString())).collect(Collectors.toList());
        
        List<Schedule> activeSchedules = schedules.stream().filter(s -> !"CANCELLED".equals(s.getStatus())).collect(Collectors.toList());
        List<Integer> assignedDriversToSchool = activeSchedules.stream().filter(s -> "TO_SCHOOL".equals(s.getDirection())).map(Schedule::getDriverID).collect(Collectors.toList());
        List<Integer> assignedMonitorsToSchool = activeSchedules.stream().filter(s -> "TO_SCHOOL".equals(s.getDirection())).map(Schedule::getMonitorID).collect(Collectors.toList());
        List<Integer> assignedBusesToSchool = activeSchedules.stream().filter(s -> "TO_SCHOOL".equals(s.getDirection())).map(Schedule::getBusID).collect(Collectors.toList());
        
        List<Integer> assignedDriversToHome = activeSchedules.stream().filter(s -> "TO_HOME".equals(s.getDirection())).map(Schedule::getDriverID).collect(Collectors.toList());
        List<Integer> assignedMonitorsToHome = activeSchedules.stream().filter(s -> "TO_HOME".equals(s.getDirection())).map(Schedule::getMonitorID).collect(Collectors.toList());
        List<Integer> assignedBusesToHome = activeSchedules.stream().filter(s -> "TO_HOME".equals(s.getDirection())).map(Schedule::getBusID).collect(Collectors.toList());
        
        List<User> driversToSchool = drivers.stream().filter(d -> !assignedDriversToSchool.contains(d.getUserID())).collect(Collectors.toList());
        List<User> monitorsToSchool = monitors.stream().filter(m -> !assignedMonitorsToSchool.contains(m.getUserID())).collect(Collectors.toList());
        List<Bus> busesToSchool = buses.stream().filter(b -> !assignedBusesToSchool.contains(b.getBusID())).collect(Collectors.toList());
        
        List<User> driversToHome = drivers.stream().filter(d -> !assignedDriversToHome.contains(d.getUserID())).collect(Collectors.toList());
        List<User> monitorsToHome = monitors.stream().filter(m -> !assignedMonitorsToHome.contains(m.getUserID())).collect(Collectors.toList());
        List<Bus> busesToHome = buses.stream().filter(b -> !assignedBusesToHome.contains(b.getBusID())).collect(Collectors.toList());

        List<Integer> assignedTechs = techSchedules.stream().filter(ts -> !"CANCELLED".equals(ts.getStatus())).map(model.TechnicianSchedule::getTechnicianID).collect(Collectors.toList());
        List<User> availableTechnicians = technicians.stream().filter(t -> !assignedTechs.contains(t.getUserID())).collect(Collectors.toList());

        request.setAttribute("driversToSchool", driversToSchool);
        request.setAttribute("monitorsToSchool", monitorsToSchool);
        request.setAttribute("busesToSchool", busesToSchool);
        request.setAttribute("driversToHome", driversToHome);
        request.setAttribute("monitorsToHome", monitorsToHome);
        request.setAttribute("busesToHome", busesToHome);
        request.setAttribute("availableTechnicians", availableTechnicians);
        
        request.setAttribute("drivers", drivers); // Keep original for view mapping if needed
        request.setAttribute("monitors", monitors); // Keep original for view mapping if needed
        request.setAttribute("technicians", technicians); // Keep original for view mapping if needed
        request.setAttribute("buses", buses); // Keep original for view mapping if needed
        request.setAttribute("routes", routes);
        request.setAttribute("schedules", schedules);
        request.setAttribute("techSchedules", techSchedules);

        request.getRequestDispatcher("schedule_management.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST để thêm mới hoặc thay đổi lịch trình.
     * Thực hiện nhiều tầng kiểm tra nghiệp vụ: thời gian quá hạn, nghỉ phép, xung đột lịch, vượt sức chứa và trạng thái xe.
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form xếp lịch
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        if ("change_bus".equals(action)) {
            try {
                int scheduleID = Integer.parseInt(request.getParameter("scheduleID"));
                int newBusID = Integer.parseInt(request.getParameter("newBusID"));
                String selectedDateStr = request.getParameter("selectedDate");

                ScheduleDAO sDao = new ScheduleDAO();
                boolean success = sDao.updateScheduleBus(scheduleID, newBusID);
                response.sendRedirect("ScheduleServlet?msg=" + (success ? "bus_changed" : "error") + "&selectedDate=" + selectedDateStr);
                return;
            } catch(Exception e) {
                response.sendRedirect("ScheduleServlet?msg=error");
                return;
            }
        }
        
        try {
            Date date = Date.valueOf(request.getParameter("date"));
            String direction = request.getParameter("direction");
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            int busID = Integer.parseInt(request.getParameter("busID"));
            int driverID = Integer.parseInt(request.getParameter("driverID"));
            int monitorID = Integer.parseInt(request.getParameter("monitorID"));

            java.time.LocalDate scheduleDate = date.toLocalDate();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDate today = now.toLocalDate();

            String paramStr = "&selectedDate=" + date.toString() + "&direction=" + direction + "&routeID=" + routeID + "&busID=" + busID + "&driverID=" + driverID + "&monitorID=" + monitorID;

            // 1. Kiểm tra không cho phép tạo lịch trình cho ngày trong quá khứ
            if (scheduleDate.isBefore(today)) {
                response.sendRedirect("ScheduleServlet?msg=past_date" + paramStr);
                return;
            }

            // 2. Kiểm tra nếu tạo lịch cho ngày hôm nay, phải đảm bảo chưa quá giờ khởi hành quy định
            if (scheduleDate.isEqual(today)) {
                if ("TO_SCHOOL".equals(direction) && now.getHour() >= 6) {
                    response.sendRedirect("ScheduleServlet?msg=timeout_school" + paramStr);
                    return;
                }
                if ("TO_HOME".equals(direction) && now.getHour() >= 16) {
                    response.sendRedirect("ScheduleServlet?msg=timeout_home" + paramStr);
                    return;
                }
            }

            ScheduleDAO dao = new ScheduleDAO();
            dal.UserDAO userDAO = new dal.UserDAO();
            
            // 3. Kiểm tra xem Tài xế có đang trong thời gian nghỉ phép đã được duyệt hay không
            if (userDAO.isLeaveApproved(driverID, date)) {
                response.sendRedirect("ScheduleServlet?msg=driver_on_leave" + paramStr);
                return;
            }
            // 4. Kiểm tra xem Giám thị có đang trong thời gian nghỉ phép đã được duyệt hay không
            if (userDAO.isLeaveApproved(monitorID, date)) {
                response.sendRedirect("ScheduleServlet?msg=monitor_on_leave" + paramStr);
                return;
            }
            
            // Check Capacity Override
            dal.HocSinhDAO hsDAO = new dal.HocSinhDAO();
            int students = hsDAO.countActiveHocSinhByRoute(routeID, date);
            
            if (students == 0) {
                 response.sendRedirect("ScheduleServlet?msg=no_students" + paramStr);
                 return;
            }
            
            // Calculate current assigned capacity
            java.util.List<Schedule> allSchedules = dao.getAllSchedules();
            dal.BusDAO busDAO = new dal.BusDAO();
            int currentAssignedCapacity = 0;
            for (Schedule s : allSchedules) {
                if (s.getRouteID() == routeID && s.getDate().toString().equals(date.toString()) && s.getDirection().equals(direction) && !"CANCELLED".equals(s.getStatus())) {
                    model.Bus b = busDAO.getBusById(s.getBusID());
                    if (b != null) {
                        currentAssignedCapacity += (b.getCapacity() - 2);
                    }
                }
            }
            
            if (currentAssignedCapacity >= students) {
                 response.sendRedirect("ScheduleServlet?msg=overcapacity" + paramStr);
                 return;
            }

            if (dao.isConflict(date, direction, driverID, monitorID, busID)) {
                response.sendRedirect("ScheduleServlet?msg=conflict" + paramStr);
                return;
            }
            
            // Check if bus is under maintenance
            java.util.List<Bus> busesOnDate = busDAO.getBusesByDate(date);
            boolean isMaintenance = busesOnDate.stream().anyMatch(b -> b.getBusID() == busID && "Bảo dưỡng/Sửa chữa".equals(b.getStatus()));
            if (isMaintenance) {
                response.sendRedirect("ScheduleServlet?msg=invalid" + paramStr);
                return;
            }

            Schedule s = new Schedule(0, date, direction, routeID, busID, driverID, monitorID, "PENDING", "NORMAL");
            boolean success = dao.insertSchedule(s);

            if (success) {
                response.sendRedirect("ScheduleServlet?msg=success&selectedDate=" + date.toString());
            } else {
                response.sendRedirect("ScheduleServlet?msg=error" + paramStr);
            }
        } catch (Exception e) {
            response.sendRedirect("ScheduleServlet?msg=invalid");
        }
    }
}
