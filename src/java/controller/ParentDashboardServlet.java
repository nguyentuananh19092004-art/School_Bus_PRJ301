package controller;

import dal.HocSinhDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.HocSinh;

@WebServlet(name = "ParentDashboardServlet", urlPatterns = {"/parent-dashboard"})
public class ParentDashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        // Giả định đối tượng user đã lưu vào session sau khi Đăng Nhập thành công
        String username = (String) session.getAttribute("username"); 
        
        if (username == null) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }

        HocSinhDAO dao = new HocSinhDAO();
        List<HocSinh> myChildren = dao.getHocSinhByParent(username);
        request.setAttribute("myChildren", myChildren);
        request.getRequestDispatcher("phuhuynh_dashboard.jsp").forward(request, response);
    }
}