package controller;

import dal.HocSinhDAO;
import dal.StudentLeaveDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ParentActionServlet", urlPatterns = {"/parent-action"})
public class ParentActionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String maHS = request.getParameter("maHocSinh");
        HocSinhDAO hsDao = new HocSinhDAO();

        if ("activate".equals(action)) {
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            int stopID = Integer.parseInt(request.getParameter("stopID"));
            hsDao.activateService(maHS, stopID, routeID);
        } 
        else if ("leave".equals(action)) {
            String leaveDate = request.getParameter("leaveDate");
            StudentLeaveDAO leaveDao = new StudentLeaveDAO();
            leaveDao.insertLeave(maHS, leaveDate);
        } 
        else if ("stopService".equals(action)) {
            // Nghiệp vụ: Cập nhật dừng dịch vụ
            hsDao.stopService(maHS);
        }

        response.sendRedirect("parent-dashboard");
    }
}