package controller;

import dal.BusDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "BusDeleteServlet", urlPatterns = {"/BusDeleteServlet"})
public class BusDeleteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        BusDAO dao = new BusDAO();
        if (dao.canDeleteBus(id)) {
            dao.deleteBus(id);
        } else {
            dao.changeStatus(id, "Bảo dưỡng/Sửa chữa");
        }
        response.sendRedirect("BusListServlet");
    }
}
