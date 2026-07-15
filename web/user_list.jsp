<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Quản lý danh sách người dùng (Nhân viên/Tài xế/Giám thị/Kỹ thuật).
    Hiển thị bảng danh sách các tài khoản trong hệ thống và cung cấp các chức năng Thêm, Sửa, Xóa.
--%>
<%@page import="java.util.List"%>
<%@page import="model.User"%>
<%
    List<User> userList = (List<User>) request.getAttribute("userList");
    String role = (String) request.getAttribute("role");
    String title = "DRIVER".equals(role) ? "Quản lý Lái xe" : ("TECHNICIAN".equals(role) ? "Quản lý Kỹ thuật" : "Quản lý Giám sát");
    String icon = "DRIVER".equals(role) ? "bi-person-badge" : ("TECHNICIAN".equals(role) ? "bi-wrench" : "bi-eye");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><%= title %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <style>
        body { background-color: #f8f9fa; }
        .card { border: none; border-radius: 12px; }
        .table th { background-color: #343a40; color: white; }
    </style>
</head>
<body>
    <div class="container mt-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold"><i class="bi <%= icon %> text-primary me-2"></i><%= title %></h2>
            <div class="d-flex align-items-center">
                <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="user-list" method="get" class="d-flex me-3 mb-0">
                    <input type="hidden" name="role" value="<%= role %>">
                    <label for="dateFilter" class="me-2 fw-bold mb-0">Xem trạng thái ngày:</label>
                    <input type="date" id="dateFilter" name="date" class="form-control form-control-sm me-2" style="cursor: pointer;" 
                           value="<%= request.getAttribute("selectedDate") != null ? request.getAttribute("selectedDate") : java.time.LocalDate.now().toString() %>"
                           onclick="this.showPicker()"
                           onchange="this.form.submit()">
                </form>
                <div>
                    <a href="user-create?role=<%= role %>" class="btn btn-success me-2"><i class="bi bi-plus-circle"></i> Thêm mới</a>
                    <a href="AdminDashboardServlet" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Về Dashboard</a>
                </div>
            </div>
        </div>
        
        <div class="card shadow-sm">
            <div class="card-body p-0">
                <%-- Bảng dữ liệu hiển thị thông tin --%>n<table class="table table-hover table-striped mb-0 text-center align-middle">
                    <thead class="table-dark">
                        <tr>
                            <th>ID</th>
                            <th>Tên TK</th>
                            <th>Mật khẩu</th>
                            <th>Họ và tên</th>
                            <th>SĐT</th>
                            <th>Email</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (userList != null && !userList.isEmpty()) {
                                dal.UserDAO uDao = new dal.UserDAO();
                                for (User u : userList) {
                                    boolean hasFutureSchedule = uDao.hasFutureSchedule(u.getUserID());
                        %>
                            <tr>
                                <td><%= u.getUserID() %></td>
                                <td class="fw-bold"><%= u.getUsername() %></td>
                                <td><span class="font-monospace text-muted"><%= u.getPassword() %></span></td>
                                <td><%= u.getFullName() %></td>
                                <td><%= u.getPhone() != null ? u.getPhone() : "" %></td>
                                <td><%= u.getEmail() != null ? u.getEmail() : "" %></td>
                                <td>
                                    <% if ("Sẵn sàng".equalsIgnoreCase(u.getStatus())) { %>
                                        <span class="badge bg-primary">Sẵn sàng</span>
                                    <% } else if ("Hoạt động".equalsIgnoreCase(u.getStatus())) { %>
                                        <span class="badge bg-success">Hoạt động</span>
                                    <% } else if ("Nghỉ".equalsIgnoreCase(u.getStatus())) { %>
                                        <span class="badge bg-secondary">Nghỉ</span>
                                    <% } else { %>
                                        <span class="badge bg-info"><%= u.getStatus() != null ? u.getStatus() : "Sẵn sàng" %></span>
                                    <% } %>
                                </td>
                                <td>
                                    <% String selDate = request.getAttribute("selectedDate") != null ? (String)request.getAttribute("selectedDate") : java.time.LocalDate.now().toString(); %>
                                    <% if ("Sẵn sàng".equalsIgnoreCase(u.getStatus())) { %>
                                        <a href="daily-status?action=report_leave&type=user&id=<%= u.getUserID() %>&role=<%= role %>&date=<%= selDate %>" class="btn btn-sm btn-warning" onclick="return confirm('Báo nghỉ phép cho nhân sự này trong ngày <%= selDate %>?');"><i class="bi bi-calendar-x"></i> Báo nghỉ</a>
                                    <% } else if ("Nghỉ".equalsIgnoreCase(u.getStatus())) { %>
                                        <a href="daily-status?action=cancel_leave&type=user&id=<%= u.getUserID() %>&role=<%= role %>&date=<%= selDate %>" class="btn btn-sm btn-secondary" onclick="return confirm('Hủy báo nghỉ cho nhân sự này trong ngày <%= selDate %>?');"><i class="bi bi-arrow-counterclockwise"></i> Hủy nghỉ</a>
                                    <% } %>
                                    <a href="user-update?id=<%= u.getUserID() %>" class="btn btn-sm btn-primary"><i class="bi bi-pencil"></i> Sửa gốc</a>
                                    <% if (hasFutureSchedule || "Hoạt động".equalsIgnoreCase(u.getStatus())) { %>
                                        <button class="btn btn-sm btn-secondary" disabled title="Không thể xóa vì nhân sự có lịch làm việc trong tương lai"><i class="bi bi-trash"></i> Xóa</button>
                                    <% } else { %>
                                        <a href="user-delete?id=<%= u.getUserID() %>&role=<%= role %>" class="btn btn-sm btn-danger" onclick="return confirm('Bạn có chắc chắn muốn xóa nhân sự này? (Các lịch phân ca cũ vẫn sẽ được giữ lại)');"><i class="bi bi-trash"></i> Xóa</a>
                                    <% } %>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="8" class="text-center text-muted py-3">Không có dữ liệu trong hệ thống.</td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>
