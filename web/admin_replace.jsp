<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Bố trí nhân sự thay thế.
    Admin dùng trang này để chọn và thay thế tài xế hoặc giám thị khác cho các ca làm việc bị trống do nhân sự nghỉ phép.
--%>
<%@page import="java.util.List"%>
<%@page import="model.User"%>
<%@page import="model.Schedule"%>
<%
    if(session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Phân công thay thế</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .card { border: none; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container">
            <a class="navbar-brand fw-bold" href="AdminDashboardServlet">
                <i class="bi bi-shield-lock-fill me-2"></i>Admin Panel
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Xin chào, <b><%= session.getAttribute("username") %></b></span>
            </div>
        </div>
    </nav>

    <div class="container mt-5">
        <div class="mb-4">
            <h2 class="fw-bold"><i class="bi bi-person-lines-fill text-warning me-2"></i>Phân công nhân sự thay thế</h2>
            <% User leaveUser = (User) request.getAttribute("leaveUser"); 
               String roleDisplay = "";
               String r = leaveUser.getRole();
               if ("taixe".equalsIgnoreCase(r) || "DRIVER".equalsIgnoreCase(r)) {
                   roleDisplay = "Tài xế";
               } else if ("giamthi".equalsIgnoreCase(r) || "MONITOR".equalsIgnoreCase(r)) {
                   roleDisplay = "Giám thị";
               } else if ("kythuat".equalsIgnoreCase(r) || "TECHNICIAN".equalsIgnoreCase(r)) {
                   roleDisplay = "Kỹ thuật";
               } else {
                   roleDisplay = r;
               }
            %>
            <p class="text-muted">Nhân viên <b><%= leaveUser.getFullName() %></b> (<%= roleDisplay %>) nghỉ phép vào ngày <b><%= request.getAttribute("leaveDate") %></b>. Dưới đây là lịch làm việc cần phân công người thay thế.</p>
         </div>
 
         <div class="card p-4">
             <form action="admin-replace" method="POST">
                 <input type="hidden" name="role" value="<%= leaveUser.getRole() %>">
                 <input type="hidden" name="leaveDate" value="<%= request.getAttribute("leaveDate") %>">
                 <%
                     List<Schedule> schedulesToReplace = (List<Schedule>) request.getAttribute("schedulesToReplace");
                     List<model.TechnicianSchedule> techSchedulesToReplace = (List<model.TechnicianSchedule>) request.getAttribute("techSchedulesToReplace");
                     List<User> availableReplacements = (List<User>) request.getAttribute("availableReplacements");
                     
                     boolean isTech = "kythuat".equalsIgnoreCase(r) || "TECHNICIAN".equalsIgnoreCase(r);
                     
                     if (isTech) {
                 %>
                 <table class="table table-bordered table-striped align-middle">
                     <thead class="table-dark">
                         <tr>
                             <th>ID Ca</th>
                             <th>Ngày trực</th>
                             <th>Trạng thái</th>
                             <th>Kỹ thuật viên thay thế (Sẵn sàng)</th>
                         </tr>
                     </thead>
                     <tbody>
                         <%
                             if (techSchedulesToReplace != null) {
                                 for (model.TechnicianSchedule ts : techSchedulesToReplace) {
                         %>
                         <tr>
                             <td class="fw-bold text-primary">#<%= ts.getTechScheduleID() %></td>
                             <td><%= ts.getDate() %></td>
                             <td>
                                 <%
                                     String statusText = "Chờ trực";
                                     if ("IN_PROGRESS".equals(ts.getStatus())) statusText = "Đang trực";
                                     else if ("COMPLETED".equals(ts.getStatus())) statusText = "Hoàn thành";
                                 %>
                                 <%= statusText %>
                             </td>
                             <td>
                                 <input type="hidden" name="techScheduleID" value="<%= ts.getTechScheduleID() %>">
                                 <select class="form-select" name="replacement_tech_<%= ts.getTechScheduleID() %>" required>
                                     <option value="">-- Chọn kỹ thuật thay thế --</option>
                                     <% if (availableReplacements != null) {
                                         for (User u : availableReplacements) { %>
                                             <option value="<%= u.getUserID() %>"><%= u.getFullName() %> (<%= u.getPhone() %>)</option>
                                     <%  }
                                        } %>
                                 </select>
                             </td>
                         </tr>
                         <%
                                 }
                             }
                         %>
                     </tbody>
                 </table>
                 <% } else { %>
                 <table class="table table-bordered table-striped align-middle">
                     <thead class="table-dark">
                         <tr>
                             <th>ID Chuyến</th>
                             <th>Hướng đi</th>
                             <th>ID Tuyến</th>
                             <th>Nhân sự thay thế (Sẵn sàng)</th>
                         </tr>
                     </thead>
                     <tbody>
                         <%
                             if (schedulesToReplace != null) {
                                 for (Schedule s : schedulesToReplace) {
                         %>
                         <tr>
                             <td class="fw-bold text-primary"><%= s.getScheduleID() %></td>
                             <td><%= s.getDirection().equals("TO_SCHOOL") ? "Đến trường" : "Về nhà" %></td>
                             <td>Tuyến <%= s.getRouteID() %></td>
                             <td>
                                 <input type="hidden" name="scheduleID" value="<%= s.getScheduleID() %>">
                                 <select class="form-select" name="replacement_<%= s.getScheduleID() %>" required>
                                     <option value="">-- Chọn người thay thế --</option>
                                     <% if (availableReplacements != null) {
                                         for (User u : availableReplacements) { %>
                                             <option value="<%= u.getUserID() %>"><%= u.getFullName() %> (<%= u.getPhone() %>)</option>
                                     <%  }
                                        } %>
                                 </select>
                             </td>
                         </tr>
                         <%
                                 }
                             }
                         %>
                     </tbody>
                 </table>
                 <% } %>
                 <div class="text-end mt-3">
                     <a href="admin-inbox" class="btn btn-secondary me-2">Bỏ qua / Xử lý sau</a>
                     <button type="submit" class="btn btn-success"><i class="bi bi-check-circle"></i> Cập nhật phân công</button>
                 </div>
            </form>
        </div>
    </div>
</body>
</html>
