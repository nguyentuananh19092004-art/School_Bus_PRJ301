package controller;

import dal.BusDAO;
import model.Bus;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "BusCreateServlet", urlPatterns = {"/bus-create"})
public class BusCreateServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("bus_form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        
        String licensePlate = request.getParameter("licensePlate");
        int capacity = Integer.parseInt(request.getParameter("capacity"));
        String status = request.getParameter("status");

        BusDAO dao = new BusDAO();
        if (dao.checkLicensePlateExist(licensePlate, 0)) {
            request.setAttribute("error", "Biển số xe '" + licensePlate + "' đã tồn tại trong hệ thống!");
            request.setAttribute("bus", new Bus(0, licensePlate, capacity, status));
            request.getRequestDispatcher("bus_form.jsp").forward(request, response);
            return;
        }

        Bus b = new Bus(0, licensePlate, capacity, status);
        dao.insertBus(b);

        response.sendRedirect("bus-list");
    }
}
