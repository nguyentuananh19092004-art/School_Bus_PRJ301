package controller;

import dal.BusDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "BusDeleteServlet", urlPatterns = {"/bus-delete"})
public class BusDeleteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idRaw = request.getParameter("id");
        try {
            int id = Integer.parseInt(idRaw);
            BusDAO dao = new BusDAO();
            dao.deleteBus(id);
        } catch (NumberFormatException e) {
            System.out.println(e);
        }
        response.sendRedirect("bus-list");
    }
}
