package controller;

import dal.BusDAO;
import model.Bus;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.sql.Date;
import java.time.LocalDate;

@WebServlet(name = "BusListServlet", urlPatterns = {"/bus-list"})
public class BusListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String dateParam = request.getParameter("date");
        Date selectedDate;
        if (dateParam != null && !dateParam.isEmpty()) {
            selectedDate = Date.valueOf(dateParam);
        } else {
            selectedDate = Date.valueOf(LocalDate.now());
        }

        BusDAO dao = new BusDAO();
        List<Bus> list = dao.getBusesByDate(selectedDate);
        
        request.setAttribute("busList", list);
        request.setAttribute("selectedDate", selectedDate.toString());
        request.getRequestDispatcher("bus_list.jsp").forward(request, response);
    }
}
