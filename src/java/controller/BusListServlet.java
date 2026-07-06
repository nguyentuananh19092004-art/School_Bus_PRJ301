package controller;

import dal.BusDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.Bus;

@WebServlet(name = "BusListServlet", urlPatterns = {"/BusListServlet"})
public class BusListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BusDAO dao = new BusDAO();
        List<Bus> buses = dao.getAllBus();
        request.setAttribute("buses", buses);
        request.getRequestDispatcher("/bus_list.jsp").forward(request, response);
    }
}
