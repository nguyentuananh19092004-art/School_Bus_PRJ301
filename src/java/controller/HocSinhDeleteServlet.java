package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "HocSinhDeleteServlet", urlPatterns = {"/hocsinh-delete"})
public class HocSinhDeleteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String maHocSinh = request.getParameter("id");
        HocSinhDAO dao = new HocSinhDAO();
        dao.deleteHocSinh(maHocSinh);
        response.sendRedirect("hocsinh-list");
    }
}
