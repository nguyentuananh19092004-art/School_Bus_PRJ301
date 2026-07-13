package controller;

import dal.HocSinhDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import model.HocSinh;

@WebServlet(name = "HocSinhListServlet", urlPatterns = {"/hocsinh-list"})
public class HocSinhListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HocSinhDAO dao = new HocSinhDAO();
        List<HocSinh> list = dao.getAllHocSinh();
        request.setAttribute("listHS", list);
        request.getRequestDispatcher("hocsinh_list.jsp").forward(request, response);
    }
}