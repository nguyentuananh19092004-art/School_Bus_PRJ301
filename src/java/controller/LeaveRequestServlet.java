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

/**
 * Servlet xử lý chức năng nộp đơn xin nghỉ phép của nhân viên.
 */
@WebServlet(name = "LeaveRequestServlet", urlPatterns = {"/leave-request"})
public class LeaveRequestServlet extends HttpServlet {

    /**
     * Xử lý dữ liệu đơn xin phép. Kiểm tra điều kiện nộp đơn (phải nộp trước 21:00 ngày hôm trước)
     * và lưu đơn xin nghỉ vào hệ thống với trạng thái chờ duyệt (PENDING).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        Integer userID = (Integer) session.getAttribute("userID");
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("userRole");
        
        if (userID == null) {
            // Chuyển hướng (Redirect) người dùng đến trang khác
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
                // Chuyển hướng (Redirect) người dùng đến trang khác
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
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect(redirectUrl + "?msg=leave_success");
            } else {
                // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect(redirectUrl + "?msg=leave_error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("employee-inbox?msg=leave_error");
        }
    }
}
