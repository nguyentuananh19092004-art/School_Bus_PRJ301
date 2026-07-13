package controller;

import dal.StopDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "RouteStopUpdateServlet", urlPatterns = {"/update-route-stop"})
public class RouteStopUpdateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        try {
            int routeID = Integer.parseInt(request.getParameter("routeID"));
            int stopID = Integer.parseInt(request.getParameter("stopID"));
            String estimatedTime = request.getParameter("estimatedTime");
            String returnTime = request.getParameter("returnTime");

            StopDAO stopDAO = new StopDAO();
            stopDAO.updateRouteStopTime(routeID, stopID, estimatedTime, returnTime);

            response.sendRedirect("route-management?routeID=" + routeID);
            
        } catch (Exception e) {
            System.out.println("Error updating stop: " + e.getMessage());
            response.sendRedirect("route-management?error=1");
        }
    }
}
