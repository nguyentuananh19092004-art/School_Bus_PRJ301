package controller;

import dal.RouteDAO;
import dal.StopDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.Route;
import model.Stop;
import model.StopRouteOption;

@WebServlet(name = "RouteManagementServlet", urlPatterns = {"/RouteManagementServlet"})
public class RouteManagementServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RouteDAO routeDao = new RouteDAO();
        StopDAO stopDao = new StopDAO();
        List<Route> routes = routeDao.getAllRoute();
        List<Stop> allStops = stopDao.getAllStop();
        request.setAttribute("routes", routes);
        request.setAttribute("allStops", allStops);
        String routeId = request.getParameter("routeId");
        if (routeId != null && !routeId.isEmpty()) {
            int id = Integer.parseInt(routeId);
            request.setAttribute("selectedRoute", routeDao.getRouteById(id));
            request.setAttribute("routeStops", routeDao.getStopsByRoute(id));
        }
        request.getRequestDispatcher("/route_management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        RouteDAO routeDao = new RouteDAO();
        if ("create".equals(action)) {
            String routeCode = request.getParameter("routeCode");
            String routeName = request.getParameter("routeName");
            String description = request.getParameter("description");
            if (routeDao.checkRouteCodeExist(routeCode)) {
                request.setAttribute("error", "Mã tuyến đã tồn tại.");
            } else {
                Route route = new Route();
                route.setRouteCode(routeCode);
                route.setRouteName(routeName);
                route.setDescription(description);
                routeDao.insertRoute(route);
            }
        } else if ("update".equals(action)) {
            int routeId = Integer.parseInt(request.getParameter("routeID"));
            String routeCode = request.getParameter("routeCode");
            String routeName = request.getParameter("routeName");
            String description = request.getParameter("description");
            if (routeDao.checkRouteCodeExist(routeCode, routeId)) {
                request.setAttribute("error", "Mã tuyến đã tồn tại.");
            } else {
                Route route = new Route(routeId, routeCode, routeName, description);
                routeDao.updateRoute(route);
            }
        } else if ("delete".equals(action)) {
            int routeId = Integer.parseInt(request.getParameter("routeID"));
            routeDao.deleteRoute(routeId);
        }
        response.sendRedirect("RouteManagementServlet");
    }
}
