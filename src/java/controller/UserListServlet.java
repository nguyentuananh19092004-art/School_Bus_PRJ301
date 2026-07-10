package controller;

import dal.UserDAO;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;

import java.sql.Date;
import java.time.LocalDate;

@WebServlet(name = "UserListServlet", urlPatterns = {"/user-list"})
public class UserListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String role = request.getParameter("role"); // DRIVER or MONITOR
        if (role == null || role.isEmpty()) {
            role = "DRIVER"; // default
        }
        
        String dateParam = request.getParameter("date");
        Date selectedDate;
        if (dateParam != null && !dateParam.isEmpty()) {
            selectedDate = Date.valueOf(dateParam);
        } else {
            selectedDate = Date.valueOf(LocalDate.now());
        }
        
        UserDAO dao = new UserDAO();
        List<User> userList = dao.getUsersByRoleAndDate(role, selectedDate);
        
        request.setAttribute("userList", userList);
        request.setAttribute("role", role);
        request.setAttribute("selectedDate", selectedDate.toString());
        request.getRequestDispatcher("user_list.jsp").forward(request, response);
    }
}
