package controller;

import dal.HocSinhDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.HocSinh;

/**
 * Servlet quản lý chức năng thêm mới Học sinh vào hệ thống.
 * Cung cấp form đăng ký và xử lý dữ liệu được gửi lên từ form.
 */
@WebServlet(name = "HocSinhCreateServlet", urlPatterns = {"/hocsinh-add"})
public class HocSinhCreateServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để hiển thị form thêm mới Học sinh.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST để thêm mới Học sinh.
     * Kiểm tra tính hợp lệ của dữ liệu (trùng mã học sinh, tên tài khoản, email).
     * Nếu có lỗi, quay lại form với thông báo lỗi. Nếu thành công, lưu vào cơ sở dữ liệu và chuyển hướng.
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
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
        String phone = request.getParameter("phone");

        int lop = 1;
        try {
            lop = Integer.parseInt(lopStr);
        } catch (NumberFormatException e) {
        }

        HocSinhDAO dao = new HocSinhDAO();
        HocSinh hs = new HocSinh(maHocSinh, tenHocSinh, lop, tenTK, matKhau, null, null, trangThai, email, phone);

        // 1. Kiểm tra xem Mã học sinh đã tồn tại chưa
        if (dao.getHocSinhByMa(maHocSinh) != null) {
            request.setAttribute("error", "Mã học sinh đã tồn tại!");
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }
        // 2. Kiểm tra xem Tên tài khoản đã tồn tại chưa
        if (dao.getHocSinhByTenTK(tenTK) != null) {
            request.setAttribute("error", "Tên tài khoản đã tồn tại!");
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        // 3. Kiểm tra xem Email đã tồn tại trong hệ thống (Học sinh hoặc User) chưa
        if (dao.checkEmailExist(email, null, tenTK)) {
            request.setAttribute("error", "Email '" + email + "' đã được sử dụng bởi một tài khoản khác!");
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        // 4. Kiểm tra xem Số điện thoại đã tồn tại trong hệ thống chưa
        if (dao.checkPhoneExist(phone, null, tenTK)) {
            request.setAttribute("error", "Số điện thoại '" + phone + "' đã được người khác sử dụng!");
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        // 4. Kiểm tra xem Email có thuộc tên miền (domain) hợp lệ của trường hay không
        String allowedDomain = getServletContext().getInitParameter("ALLOWED_EMAIL_DOMAIN");
        if (!util.EmailUtil.isValidDomain(email, allowedDomain)) {
            request.setAttribute("error", "Email bắt buộc phải có tên miền thuộc danh sách: " + allowedDomain);
            request.setAttribute("hs", hs);
            request.setAttribute("isCreate", true);
            request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        // 5. Nếu dữ liệu hợp lệ, thêm học sinh vào CSDL
        dao.insertHocSinh(hs);

        response.sendRedirect("hocsinh-list");
    }
}
