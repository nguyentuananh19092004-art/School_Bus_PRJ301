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
 * Servlet quản lý chức năng cập nhật thông tin Học sinh.
 * Cung cấp form chỉnh sửa và xử lý dữ liệu để lưu các thay đổi vào cơ sở dữ liệu.
 */
@WebServlet(name = "HocSinhUpdateServlet", urlPatterns = {"/hocsinh-edit"})
public class HocSinhUpdateServlet extends HttpServlet {
    /**
     * Xử lý yêu cầu GET để hiển thị form cập nhật với thông tin hiện tại của Học sinh.
     * 
     * @param request đối tượng HttpServletRequest chứa yêu cầu của client (Mã học sinh)
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
        String maHocSinh = request.getParameter("id");
        // Khởi tạo đối tượng DAO để tương tác CSDL
        HocSinhDAO dao = new HocSinhDAO();
        HocSinh hs = dao.getHocSinhByMa(maHocSinh);
        request.setAttribute("hs", hs);
        // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
    }

    /**
     * Xử lý yêu cầu POST để cập nhật thông tin Học sinh.
     * Kiểm tra tính hợp lệ của dữ liệu (trùng tên tài khoản, email với người khác).
     * 
     * @param request đối tượng HttpServletRequest chứa dữ liệu form cần cập nhật
     * @param response đối tượng HttpServletResponse dùng để gửi phản hồi
     * @throws ServletException nếu có lỗi xảy ra trong quá trình xử lý servlet
     * @throws IOException nếu có lỗi I/O xảy ra
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                // Xử lý luồng dữ liệu HTTP
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

        // Khởi tạo đối tượng DAO để tương tác CSDL
        HocSinhDAO dao = new HocSinhDAO();
        HocSinh existingByTk = dao.getHocSinhByTenTK(tenTK);
        if (existingByTk != null && !existingByTk.getMaHocSinh().equals(maHocSinh)) {
            request.setAttribute("error", "Tên tài khoản đã tồn tại ở học sinh khác!");
            HocSinh hsError = dao.getHocSinhByMa(maHocSinh);
            request.setAttribute("hs", hsError);
            // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        if (dao.checkEmailExist(email, maHocSinh, tenTK)) {
            request.setAttribute("error", "Email '" + email + "' đã được sử dụng bởi một tài khoản khác!");
            HocSinh hsError = dao.getHocSinhByMa(maHocSinh);
            request.setAttribute("hs", hsError);
            // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        if (dao.checkPhoneExist(phone, maHocSinh, tenTK)) {
            request.setAttribute("error", "Số điện thoại '" + phone + "' đã được người khác sử dụng!");
            HocSinh hsError = dao.getHocSinhByMa(maHocSinh);
            request.setAttribute("hs", hsError);
            // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        String allowedDomain = getServletContext().getInitParameter("ALLOWED_EMAIL_DOMAIN");
        if (!util.EmailUtil.isValidDomain(email, allowedDomain)) {
            request.setAttribute("error", "Email bắt buộc phải có tên miền thuộc danh sách: " + allowedDomain);
            HocSinh hsError = dao.getHocSinhByMa(maHocSinh);
            request.setAttribute("hs", hsError);
            // Trả kết quả về cho View (JSP) hiển thị
        request.getRequestDispatcher("hocsinh_form.jsp").forward(request, response);
            return;
        }

        HocSinh oldHs = dao.getHocSinhByMa(maHocSinh);
        Integer defaultStopID = oldHs != null ? oldHs.getDefaultStopID() : null;
        Integer defaultRouteID = oldHs != null ? oldHs.getDefaultRouteID() : null;

        HocSinh hs = new HocSinh(maHocSinh, tenHocSinh, lop, tenTK, matKhau, defaultStopID, defaultRouteID, trangThai, email, phone);
        dao.updateHocSinh(hs);

        // Chuyển hướng (Redirect) người dùng đến trang khác
        response.sendRedirect("hocsinh-list");
    }
}
