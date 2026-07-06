package controller;

import dal.RouteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import model.StopRouteOption;

@WebServlet(name = "RouteStopUpdateServlet", urlPatterns = {"/RouteStopUpdateServlet"})
public class RouteStopUpdateServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int routeId = Integer.parseInt(request.getParameter("routeID"));
        String[] stopIds = request.getParameterValues("stopID");
        String[] orders = request.getParameterValues("stopOrder");
        String[] estimatedTimes = request.getParameterValues("estimatedTime");
        String[] returnTimes = request.getParameterValues("returnTime");

        List<StopRouteOption> list = new ArrayList<>();
        if (stopIds != null) {
            for (int i = 0; i < stopIds.length; i++) {
                StopRouteOption option = new StopRouteOption();
                option.setRouteID(routeId);
                option.setStopID(Integer.parseInt(stopIds[i]));
                option.setStopOrder(Integer.parseInt(orders[i]));
                option.setEstimatedTime(Time.valueOf(estimatedTimes[i]));
                option.setReturnTime(returnTimes[i] == null || returnTimes[i].isEmpty() ? null : Time.valueOf(returnTimes[i]));
                list.add(option);
            }
        }

        if (isValidOrder(list)) {
            RouteDAO routeDao = new RouteDAO();
            routeDao.updateRouteStops(routeId, list);
        }

        response.sendRedirect("RouteManagementServlet?routeId=" + routeId);
    }

    private boolean isValidOrder(List<StopRouteOption> list) {
        if (list == null || list.isEmpty()) {
            return true;
        }
        int[] orders = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            orders[i] = list.get(i).getStopOrder();
        }
        for (int i = 0; i < orders.length; i++) {
            for (int j = i + 1; j < orders.length; j++) {
                if (orders[i] == orders[j]) {
                    return false;
                }
            }
        }
        for (int i = 0; i < orders.length; i++) {
            if (orders[i] < 1) {
                return false;
            }
        }
        return true;
    }
}
