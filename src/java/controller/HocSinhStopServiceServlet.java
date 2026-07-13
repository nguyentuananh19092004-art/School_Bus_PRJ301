package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "HocSinhStopServiceServlet", urlPatterns = {"/hocsinh-stop-service"})
public class HocSinhStopServiceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        String maHocSinh = request.getParameter("id");
        if (maHocSinh != null && !maHocSinh.isEmpty()) {
            HocSinhDAO dao = new HocSinhDAO();
            dao.stopService(maHocSinh);
            response.sendRedirect("hocsinh-list?msg=stopped");
        } else {
            response.sendRedirect("hocsinh-list?msg=error");
        }
    }
}
