package controller;

import dal.StopDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Stop;

@WebServlet(name = "StopAddServlet", urlPatterns = {"/add-stop"})
public class StopAddServlet extends HttpServlet {

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
            String stopName = request.getParameter("stopName");
            String address = request.getParameter("address");
            double lat = Double.parseDouble(request.getParameter("latitude"));
            double lng = Double.parseDouble(request.getParameter("longitude"));
            String estimatedTime = request.getParameter("estimatedTime");
            String returnTime = request.getParameter("returnTime");

            StopDAO stopDAO = new StopDAO();
            
            // Create stop
            Stop newStop = new Stop(0, stopName, address, lat, lng);
            int stopID = stopDAO.insertStop(newStop);

            if (stopID > 0) {
                // Determine order. Usually add before the last stop (the school).
                // Or just append it. Let's just append it for simplicity.
                int maxOrder = stopDAO.getMaxStopOrder(routeID);
                stopDAO.addStopToRoute(routeID, stopID, maxOrder + 1, estimatedTime, returnTime);
            }

            response.sendRedirect("route-management?routeID=" + routeID);
            
        } catch (Exception e) {
            System.out.println("Error adding stop: " + e.getMessage());
            response.sendRedirect("route-management?error=1");
        }
    }
}
