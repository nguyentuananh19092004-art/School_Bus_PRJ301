package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HocSinh;

@WebServlet(name = "HocSinhUpdateServlet", urlPatterns = {"/hocsinh-edit"})
public class HocSinhUpdateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String maHocSinh = request.getParameter("id");
        HocSinhDAO dao = new HocSinhDAO();
        HocSinh hs = dao.getHocSinhByMa(maHocSinh);
        request.setAttribute("hs", hs);
        request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String maHocSinh = request.getParameter("maHocSinh");
        String tenHocSinh = request.getParameter("tenHocSinh");
        String lopStr = request.getParameter("lop");
        String tenTK = request.getParameter("tenTK");
        String matKhau = request.getParameter("matKhau");
        String trangThai = request.getParameter("trangThai");
        String email = request.getParameter("email");

        int lop = 1;
        try {
            lop = Integer.parseInt(lopStr);
        } catch (NumberFormatException e) {
        }

        HocSinhDAO dao = new HocSinhDAO();
        HocSinh existingByTk = dao.getHocSinhByTenTK(tenTK);
        if (existingByTk != null && !existingByTk.getMaHocSinh().equals(maHocSinh)) {
            request.setAttribute("error", "Tên tài khoản đã tồn tại ở học sinh khác!");
            HocSinh hsError = dao.getHocSinhByMa(maHocSinh);
            request.setAttribute("hs", hsError);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        if (dao.checkEmailExist(email, maHocSinh)) {
            request.setAttribute("error", "Email '" + email + "' đã được sử dụng bởi một tài khoản khác!");
            HocSinh hsError = dao.getHocSinhByMa(maHocSinh);
            request.setAttribute("hs", hsError);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        String allowedDomain = getServletContext().getInitParameter("ALLOWED_EMAIL_DOMAIN");
        if (!util.EmailUtil.isValidDomain(email, allowedDomain)) {
            request.setAttribute("error", "Email bắt buộc phải có tên miền thuộc danh sách: " + allowedDomain);
            HocSinh hsError = dao.getHocSinhByMa(maHocSinh);
            request.setAttribute("hs", hsError);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        HocSinh oldHs = dao.getHocSinhByMa(maHocSinh);
        Integer defaultStopID = oldHs != null ? oldHs.getDefaultStopID() : null;
        Integer defaultRouteID = oldHs != null ? oldHs.getDefaultRouteID() : null;

        HocSinh hs = new HocSinh(maHocSinh, tenHocSinh, lop, tenTK, matKhau, defaultStopID, defaultRouteID, trangThai, email);
        dao.updateHocSinh(hs);

        response.sendRedirect("hocsinh-list");
    }
}
