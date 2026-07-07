<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.UserLeave"%>
<%@page import="model.User"%>
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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hòm thư Admin - Xét duyệt nghỉ phép</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .table-custom { background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .table-custom thead { background-color: #343a40; color: white; }
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
                <a href="doi_mat_khau.jsp" class="btn btn-sm btn-outline-warning me-2"><i class="bi bi-key"></i> Đổi mật khẩu</a>
                <a href="dang_nhap.jsp" class="btn btn-sm btn-outline-light"><i class="bi bi-box-arrow-right"></i> Đăng xuất</a>
            </div>
        </div>
    </nav>

    <div class="container mt-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold"><i class="bi bi-envelope-open-heart text-danger me-2"></i>Hòm thư - Đơn xin nghỉ phép</h2>
            <a href="AdminDashboardServlet" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Về Dashboard</a>
        </div>

        <% String msg = request.getParameter("msg");
           if ("approved".equals(msg)) { %>
            <div class="alert alert-success"><i class="bi bi-check-circle me-2"></i>Đã duyệt đơn nghỉ phép.</div>
        <% } else if ("rejected".equals(msg)) { %>
            <div class="alert alert-warning"><i class="bi bi-exclamation-triangle me-2"></i>Đã từ chối đơn nghỉ phép.</div>
        <% } %>

        <div class="table-custom">
            <table class="table table-hover mb-0 align-middle">
                <thead>
                    <tr>
                        <th class="py-3 px-4">Ngày xin nghỉ</th>
                        <th>Người làm đơn</th>
                        <th>Vai trò</th>
                        <th>Lý do</th>
                        <th>Ngày gửi đơn</th>
                        <th class="text-end px-4">Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        List<UserLeave> leaves = (List<UserLeave>) request.getAttribute("pendingLeaves");
                        java.util.Map<Integer, List<User>> replacementsMap = (java.util.Map<Integer, List<User>>) request.getAttribute("replacementsMap");
                        java.util.Map<Integer, Boolean> hasSchedulesMap = (java.util.Map<Integer, Boolean>) request.getAttribute("hasSchedulesMap");
                        
                        if (leaves != null && !leaves.isEmpty()) {
                            for (UserLeave ul : leaves) {
                    %>
                    <tr>
                        <td class="px-4 fw-bold text-primary"><%= ul.getLeaveDate() %></td>
                        <td class="fw-bold"><%= ul.getFullName() %></td>
                        <td>
                            <span class="badge bg-secondary">
                                <% 
                                    String r = ul.getRole();
                                    if ("taixe".equalsIgnoreCase(r) || "DRIVER".equalsIgnoreCase(r)) {
                                        out.print("Tài xế");
                                    } else if ("giamthi".equalsIgnoreCase(r) || "MONITOR".equalsIgnoreCase(r)) {
                                        out.print("Giám sát");
                                    } else if ("kythuat".equalsIgnoreCase(r) || "TECHNICIAN".equalsIgnoreCase(r)) {
                                        out.print("Kỹ thuật");
                                    } else {
                                        out.print(r);
                                    }
                                %>
                            </span>
                        </td>
                        <td><%= ul.getReason() %></td>
                        <td><%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ul.getCreatedAt()) %></td>
                        <td class="text-end px-4">
                            <% 
                                Boolean hasSch = (hasSchedulesMap != null) ? hasSchedulesMap.get(ul.getLeaveID()) : false;
                                List<User> avail = (replacementsMap != null) ? replacementsMap.get(ul.getLeaveID()) : null;
                                if (hasSch != null && hasSch) {
                            %>
                            <form action="admin-inbox" method="POST" class="d-inline-flex align-items-center gap-1">
                                <input type="hidden" name="leaveID" value="<%= ul.getLeaveID() %>">
                                <input type="hidden" name="action" value="approve">
                                <select name="replacementUserID" class="form-select form-select-sm w-auto" required>
                                    <option value="" disabled selected>-- Chọn thay thế --</option>
                                    <% if (avail != null) {
                                        for (User u : avail) { %>
                                            <option value="<%= u.getUserID() %>"><%= u.getFullName() %></option>
                                    <%  }
                                    } %>
                                </select>
                                <button type="submit" class="btn btn-sm btn-success text-nowrap" onclick="return confirm('Duyệt nghỉ phép và bàn giao lịch làm việc cho nhân sự thay thế?');"><i class="bi bi-check2"></i> Duyệt & Thay</button>
                            </form>
                            <% } else { %>
                            <form action="admin-inbox" method="POST" class="d-inline">
                                <input type="hidden" name="leaveID" value="<%= ul.getLeaveID() %>">
                                <input type="hidden" name="action" value="approve">
                                <button type="submit" class="btn btn-sm btn-success me-1" onclick="return confirm('Duyệt cho nhân sự này nghỉ? (Không có lịch làm việc ngày này)');"><i class="bi bi-check2"></i> Duyệt</button>
                            </form>
                            <% } %>
                            <form action="admin-inbox" method="POST" class="d-inline">
                                <input type="hidden" name="leaveID" value="<%= ul.getLeaveID() %>">
                                <input type="hidden" name="action" value="reject">
                                <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('Từ chối đơn xin nghỉ này?');"><i class="bi bi-x-lg"></i> Từ chối</button>
                            </form>
                        </td>
                    </tr>
                    <%      }
                        } else {
                    %>
                    <tr>
                        <td colspan="6" class="text-center py-4 text-muted"><i class="bi bi-inbox fs-4 d-block mb-2"></i>Không có đơn xin nghỉ phép nào đang chờ duyệt.</td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
