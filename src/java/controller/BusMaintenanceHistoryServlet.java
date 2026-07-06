package controller;

import dal.BusDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.BusMaintenance;

@WebServlet(name = "BusMaintenanceHistoryServlet", urlPatterns = {"/BusMaintenanceHistoryServlet"})
public class BusMaintenanceHistoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int busId = Integer.parseInt(request.getParameter("id"));
        BusDAO busDao = new BusDAO();
        request.setAttribute("bus", busDao.getBusById(busId));

        List<BusMaintenance> maintenances = new ArrayList<>();
        String sql = "SELECT * FROM BusMaintenances WHERE BusID = ? ORDER BY MaintenanceDate DESC, MaintenanceID DESC";
        try (Connection conn = new dal.ConnectionProvider().getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, busId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    maintenances.add(new BusMaintenance(rs.getInt("MaintenanceID"), rs.getInt("BusID"), rs.getDate("MaintenanceDate"), rs.getString("Description"), rs.getTimestamp("CreatedAt")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("maintenances", maintenances);
        request.getRequestDispatcher("/bus_maintenance_history.jsp").forward(request, response);
    }
}
