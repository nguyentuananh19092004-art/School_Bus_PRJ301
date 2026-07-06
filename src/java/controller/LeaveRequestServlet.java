package controller;

import dal.UserDAO;
import dal.NotificationDAO;
import java.io.IOException;
import java.sql.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LeaveRequestServlet", urlPatterns = {"/leave-request"})
public class LeaveRequestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        Integer userID = (Integer) session.getAttribute("userID");
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("userRole");
        
        if (userID == null) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String dateStr = request.getParameter("leaveDate");
        String reason = request.getParameter("reason");
        
        try {
            Date date = Date.valueOf(dateStr);
            
            // Check deadline: Before 21:00 (9 PM) of the previous day
            java.time.LocalDate requestDate = date.toLocalDate();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime deadline = requestDate.minusDays(1).atTime(21, 0);

            if (now.isAfter(deadline)) {
                response.sendRedirect("employee-inbox?msg=leave_timeout");
                return;
            }

            UserDAO userDAO = new UserDAO();
            
            // Employee requests leave -> PENDING
            boolean success = userDAO.insertUserLeave(userID, date, reason, "PENDING");
            
            String redirectUrl = "employee-inbox";
            
            if (success) {
                // Optional: Send notification to Admin that there is a new request
                // NotificationDAO nDao = new NotificationDAO();
                // nDao.insertNotification("admin", "Có đơn xin nghỉ phép mới từ " + username + " cho ngày " + dateStr);
                response.sendRedirect(redirectUrl + "?msg=leave_success");
            } else {
                response.sendRedirect(redirectUrl + "?msg=leave_error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("employee-inbox?msg=leave_error");
        }
    }
}
