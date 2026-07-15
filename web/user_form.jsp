<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Biểu mẫu Người dùng.
    Dùng chung cho cả chức năng Thêm mới và Chỉnh sửa thông tin nhân viên (Tài xế/Giám thị/Kỹ thuật).
--%>
<%@page import="model.User"%>
<%
    User u = (User) request.getAttribute("userObj");
    String role = (String) request.getAttribute("role");
    boolean isEdit = (u != null && u.getUserID() != 0);
    String titleRole = "DRIVER".equals(role) ? "Lái xe" : ("TECHNICIAN".equals(role) ? "Kỹ thuật" : "Giám sát");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><%= isEdit ? "Cập nhật" : "Thêm mới" %> <%= titleRole %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <style>
        body { background-color: #f4f6f9; }
        .card { border: none; border-radius: 12px; }
    </style>
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white">
                        <h4 class="mb-0 fw-bold">
                            <i class="bi <%= isEdit ? "bi-pencil-square" : "bi-plus-circle" %> me-2"></i>
                            <%= isEdit ? "Cập nhật " + titleRole : "Thêm mới " + titleRole %>
                        </h4>
                    </div>
                    <div class="card-body">
                        <% String error = (String) request.getAttribute("error");
                           if (error != null) { %>
                            <div class="alert alert-danger fw-bold"><i class="bi bi-exclamation-triangle-fill me-2"></i><%= error %></div>
                        <% } %>
                        <form action="<%= isEdit ? "user-update" : "user-create" %>" method="POST">
                            <% if (isEdit) { %>
                                <input type="hidden" name="userID" value="<%= u.getUserID() %>">
                            <% } %>
                            <input type="hidden" name="role" value="<%= role %>">
                            
                            <div class="mb-3">
                                <label for="username" class="form-label fw-bold">Tên đăng nhập</label>
                                <input type="text" class="form-control" id="username" name="username" value="<%= isEdit ? u.getUsername() : "" %>" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="password" class="form-label fw-bold">Mật khẩu</label>
                                <input type="text" class="form-control" id="password" name="password" value="<%= isEdit ? u.getPassword() : "" %>" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="fullName" class="form-label fw-bold">Họ và tên</label>
                                <input type="text" class="form-control" id="fullName" name="fullName" value="<%= isEdit ? u.getFullName() : "" %>" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="phone" class="form-label fw-bold">Số điện thoại</label>
                                <input type="text" class="form-control" id="phone" name="phone" value="<%= isEdit && u.getPhone() != null ? u.getPhone() : "" %>">
                            </div>
                            
                            <div class="mb-3">
                                <label for="email" class="form-label fw-bold">Email (Tuỳ chọn)</label>
                                <input type="email" class="form-control" id="email" name="email" value="<%= isEdit && u.getEmail() != null ? u.getEmail() : "" %>">
                            </div>
                            
                            <div class="mb-4">
                                <label for="status" class="form-label fw-bold">Trạng thái</label>
                                <select class="form-select" id="status" name="status" required>
                                    <option value="Sẵn sàng" <%= (isEdit && "Sẵn sàng".equalsIgnoreCase(u.getStatus())) ? "selected" : "" %>>Sẵn sàng</option>
                                    <option value="Nghỉ" <%= (isEdit && "Nghỉ".equalsIgnoreCase(u.getStatus())) ? "selected" : "" %>>Nghỉ</option>
                                    <option value="Hoạt động" <%= (isEdit && "Hoạt động".equalsIgnoreCase(u.getStatus())) ? "selected" : "" %>>Hoạt động</option>
                                </select>
                            </div>
                            
                            <div class="d-flex justify-content-between">
                                <a href="user-list?role=<%= role %>" class="btn btn-secondary"><i class="bi bi-arrow-left"></i> Quay lại</a>
                                <button type="submit" class="btn btn-primary"><i class="bi bi-save"></i> <%= isEdit ? "Cập nhật" : "Lưu" %></button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
