package controller;

import dal.BusDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import model.Bus;

@WebServlet(name = "BusUpdateServlet", urlPatterns = {"/BusUpdateServlet"})
public class BusUpdateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        BusDAO dao = new BusDAO();
        Bus bus = dao.getBusById(id);
        request.setAttribute("bus", bus);
        request.getRequestDispatcher("/bus_form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int id = Integer.parseInt(request.getParameter("busID"));
        String licensePlate = request.getParameter("licensePlate");
        String capacityStr = request.getParameter("capacity");
        String status = request.getParameter("status");
        String error = null;

        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            error = "Vui lòng nhập biển số xe.";
        } else if (capacityStr == null || capacityStr.trim().isEmpty()) {
            error = "Vui lòng nhập sức chứa.";
        } else {
            try {
                int capacity = Integer.parseInt(capacityStr);
                if (capacity != 7 && capacity != 9) {
                    error = "Sức chứa chỉ được phép là 7 hoặc 9.";
                } else {
                    BusDAO dao = new BusDAO();
                    if (dao.checkLicensePlateExist(licensePlate.trim(), id)) {
                        error = "Biển số xe đã tồn tại.";
                    } else {
                        Bus bus = new Bus(id, licensePlate.trim(), capacity, status);
                        if (dao.updateBus(bus)) {
                            response.sendRedirect("BusListServlet");
                            return;
                        } else {
                            error = "Không thể cập nhật xe.";
                        }
                    }
                }
            } catch (NumberFormatException e) {
                error = "Sức chứa phải là số.";
            }
        }

        BusDAO dao = new BusDAO();
        request.setAttribute("bus", dao.getBusById(id));
        request.setAttribute("error", error);
        request.getRequestDispatcher("/bus_form.jsp").forward(request, response);
    }
}
