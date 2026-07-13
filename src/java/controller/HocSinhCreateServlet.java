package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HocSinh;

@WebServlet(name = "HocSinhCreateServlet", urlPatterns = {"/hocsinh-add"})
public class HocSinhCreateServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        HocSinh hs = new HocSinh(maHocSinh, tenHocSinh, lop, tenTK, matKhau, email, 0, 0, trangThai);

        if (dao.getHocSinhByMa(maHocSinh) != null) {
            request.setAttribute("error", "Mã học sinh đã tồn tại!");
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }
        if (dao.getHocSinhByTenTK(tenTK) != null) {
            request.setAttribute("error", "Tên tài khoản đã tồn tại!");
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        if (dao.checkEmailExist(email, null)) {
            request.setAttribute("error", "Email '" + email + "' đã được sử dụng bởi một tài khoản khác!");
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        String allowedDomain = getServletContext().getInitParameter("ALLOWED_EMAIL_DOMAIN");
        if (!util.EmailUtil.isValidDomain(email, allowedDomain)) {
            request.setAttribute("error", "Email bắt buộc phải có tên miền thuộc danh sách: " + allowedDomain);
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        dao.insertHocSinh(hs);

        response.sendRedirect("hocsinh-list");
    }
}
