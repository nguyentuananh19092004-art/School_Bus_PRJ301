package controller;

import dal.ScheduleDAO;
import dal.BusDAO;
import dal.UserDAO;
import model.Schedule;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "EmployeeScheduleServlet", urlPatterns = {"/employee-schedule"})
public class EmployeeScheduleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("userRole");
        if (role == null || (!"taixe".equals(role) && !"giamthi".equals(role) && !"kythuat".equals(role))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        Integer userID = (Integer) session.getAttribute("userID");
        String dateStr = request.getParameter("date");
        Date targetDate;
        
        if (dateStr != null && !dateStr.isEmpty()) {
            targetDate = Date.valueOf(dateStr);
        } else {
            targetDate = new Date(System.currentTimeMillis());
            dateStr = targetDate.toString();
        }

        ScheduleDAO scheduleDAO = new ScheduleDAO();
        List<Schedule> schedules = scheduleDAO.getSchedulesByUserAndDate(userID, role, targetDate);
        
        List<model.TechnicianSchedule> techSchedules = null;
        if ("kythuat".equals(role)) {
            techSchedules = scheduleDAO.getTechnicianSchedulesByUserAndDate(userID, targetDate);
        }

        // Fetch additional info if needed
        BusDAO busDAO = new BusDAO();
        UserDAO userDAO = new UserDAO();
        
        request.setAttribute("selectedDate", dateStr);
        request.setAttribute("schedules", schedules);
        request.setAttribute("techSchedules", techSchedules);
        request.setAttribute("busDAO", busDAO);
        request.setAttribute("userDAO", userDAO);

        request.getRequestDispatcher("employee_schedule.jsp").forward(request, response);
    }
}
