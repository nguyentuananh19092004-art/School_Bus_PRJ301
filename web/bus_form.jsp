<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Biểu mẫu Xe Bus.
    Dùng chung cho chức năng Thêm mới và Chỉnh sửa thông tin xe bus (Biển số, sức chứa, trạng thái).
--%>
<%@page import="model.Bus"%>
<%
    Bus bus = (Bus) request.getAttribute("bus");
    boolean isEdit = (bus != null && bus.getBusID() != 0);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= isEdit ? "Cập nhật Xe Bus" : "Thêm mới Xe Bus" %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow-sm">
                    <div class="card-header bg-warning text-dark">
                        <h4 class="mb-0 fw-bold">
                            <i class="bi <%= isEdit ? "bi-pencil-square" : "bi-plus-circle" %> me-2"></i>
                            <%= isEdit ? "Cập nhật thông tin Xe Bus" : "Thêm mới Xe Bus" %>
                        </h4>
                    </div>
                    <div class="card-body">
                        <% String error = (String) request.getAttribute("error");
                           if (error != null) { %>
                            <div class="alert alert-danger fw-bold"><i class="bi bi-exclamation-triangle-fill me-2"></i><%= error %></div>
                        <% } %>
                        <form action="<%= isEdit ? "bus-update" : "bus-create" %>" method="POST">
                            <% if (isEdit) { %>
                                <input type="hidden" name="busID" value="<%= bus.getBusID() %>">
                            <% } %>
                            
                            <div class="mb-3">
                                <label for="licensePlate" class="form-label fw-bold">Biển số xe</label>
                                <input type="text" class="form-control" id="licensePlate" name="licensePlate" 
                                       value="<%= isEdit ? bus.getLicensePlate() : "" %>" required>
                            </div>
                            
                            <div class="mb-3">
                                <label for="capacity" class="form-label fw-bold">Sức chứa (Ghế)</label>
                                <select class="form-select" id="capacity" name="capacity" required>
                                    <option value="7" <%= (isEdit && bus.getCapacity() == 7) ? "selected" : "" %>>7 chỗ</option>
                                    <option value="9" <%= (isEdit && bus.getCapacity() == 9) ? "selected" : "" %>>9 chỗ</option>
                                </select>
                            </div>
                            
                            <div class="mb-4">
                                <label for="status" class="form-label fw-bold">Trạng thái</label>
                                <select class="form-select" id="status" name="status" required>
                                    <option value="Sẵn sàng" <%= (isEdit && "Sẵn sàng".equalsIgnoreCase(bus.getStatus())) ? "selected" : "" %>>Sẵn sàng</option>
                                    <option value="Hoạt động" <%= (isEdit && "Hoạt động".equalsIgnoreCase(bus.getStatus())) ? "selected" : "" %>>Hoạt động</option>
                                    <option value="Bảo dưỡng/Sửa chữa" <%= (isEdit && "Bảo dưỡng/Sửa chữa".equalsIgnoreCase(bus.getStatus())) ? "selected" : "" %>>Sửa chữa/Bảo dưỡng</option>
                                </select>
                            </div>
                            
                            <div class="d-flex justify-content-between">
                                <a href="bus-list" class="btn btn-secondary"><i class="bi bi-x-circle"></i> Hủy</a>
                                <button type="submit" class="btn btn-success"><i class="bi bi-save"></i> <%= isEdit ? "Cập nhật" : "Thêm mới" %></button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
