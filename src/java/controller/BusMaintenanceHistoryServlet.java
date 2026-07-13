package controller;

import dal.BusDAO;
import model.Bus;
import model.BusMaintenance;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "BusMaintenanceHistoryServlet", urlPatterns = {"/bus-maintenance-history"})
public class BusMaintenanceHistoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        try {
            int busID = Integer.parseInt(request.getParameter("id"));
            BusDAO busDAO = new BusDAO();
            Bus bus = busDAO.getBusById(busID);
            
            if (bus != null) {
                List<BusMaintenance> history = busDAO.getBusMaintenances(busID);
                request.setAttribute("bus", bus);
                request.setAttribute("history", history);
                request.getRequestDispatcher("bus_maintenance_history.jsp").forward(request, response);
            } else {
                response.sendRedirect("bus-list");
            }
        } catch (Exception e) {
            response.sendRedirect("bus-list");
        }
    }
}
