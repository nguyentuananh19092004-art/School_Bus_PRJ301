package controller;

import dal.StopDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import model.Stop;

@WebServlet(name = "StopAddServlet", urlPatterns = {"/StopAddServlet"})
public class StopAddServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String stopName = request.getParameter("stopName");
        String address = request.getParameter("address");
        String latitude = request.getParameter("latitude");
        String longitude = request.getParameter("longitude");

        StopDAO dao = new StopDAO();
        if (dao.checkStopNameExist(stopName)) {
            request.setAttribute("error", "Tên điểm đón đã tồn tại.");
        } else if (dao.checkStopLocationExist(new BigDecimal(latitude), new BigDecimal(longitude))) {
            request.setAttribute("error", "Điểm đón tại tọa độ này đã tồn tại.");
        } else {
            Stop stop = new Stop();
            stop.setStopName(stopName);
            stop.setAddress(address);
            stop.setLatitude(latitude);
            stop.setLongitude(longitude);
            dao.insertStop(stop);
        }

        response.sendRedirect("RouteManagementServlet");
    }
}
