package controller;

import dal.ScheduleDAO;
import java.io.IOException;
import java.sql.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "TechnicianScheduleServlet", urlPatterns = {"/tech-schedule"})
public class TechnicianScheduleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        ScheduleDAO dao = new ScheduleDAO();

        if ("delete".equals(action)) {
            String idRaw = request.getParameter("id");
            String selectedDate = request.getParameter("selectedDate");
            try {
                int id = Integer.parseInt(idRaw);
                boolean success = dao.deleteTechnicianSchedule(id);
                if (success) {
                    response.sendRedirect("ScheduleServlet?selectedDate=" + selectedDate + "&msg=tech_deleted");
                } else {
                    response.sendRedirect("ScheduleServlet?selectedDate=" + selectedDate + "&msg=tech_error");
                }
            } catch (Exception e) {
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
                response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_past_date");
                return;
            }

            dal.UserDAO userDAO = new dal.UserDAO();
            if (userDAO.isLeaveApproved(technicianID, date)) {
                response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_on_leave");
                return;
            }

            boolean success = dao.insertTechnicianSchedule(technicianID, date);
            if (success) {
                response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_success");
            } else {
                response.sendRedirect("ScheduleServlet?selectedDate=" + date + "&msg=tech_conflict");
            }
        } catch (Exception e) {
            response.sendRedirect("ScheduleServlet?msg=tech_error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
