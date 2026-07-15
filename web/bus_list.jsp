<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Quản lý danh sách Xe Bus.
    Hiển thị thông tin toàn bộ các xe trong hệ thống, cung cấp chức năng Thêm, Sửa, Xóa và xem Lịch sử bảo dưỡng.
--%>
<%@page import="java.util.List"%>
<%@page import="model.Bus"%>
<%@page import="model.BusMaintenance"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Danh sách Xe Bus</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold"><i class="bi bi-bus-front text-warning me-2"></i>Danh sách Xe Bus</h2>
            <div class="d-flex align-items-center">
                <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="bus-list" method="get" class="d-flex me-3 mb-0">
                    <label for="dateFilter" class="me-2 fw-bold mb-0">Xem trạng thái ngày:</label>
                    <input type="date" id="dateFilter" name="date" class="form-control form-control-sm me-2" style="cursor: pointer;" 
                           value="<%= request.getAttribute("selectedDate") != null ? request.getAttribute("selectedDate") : java.time.LocalDate.now().toString() %>"
                           onclick="this.showPicker()"
                           onchange="this.form.submit()">
                </form>
                <div>
                    <a href="bus-create" class="btn btn-success me-2"><i class="bi bi-plus-circle"></i> Thêm Xe Bus</a>
                    <a href="AdminDashboardServlet" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Về Dashboard</a>
                </div>
            </div>
        </div>
        
        <div class="card shadow-sm">
            <div class="card-body p-0">
                <%-- Bảng dữ liệu hiển thị thông tin --%>n<table class="table table-hover table-striped mb-0 text-center align-middle">
                    <thead class="table-dark">
                        <tr>
                            <th>STT</th>
                            <th>Biển số xe</th>
                            <th>Sức chứa (Ghế)</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Bus> busList = (List<Bus>) request.getAttribute("busList");
                            if (busList != null && !busList.isEmpty()) {
                                int stt = 1;
                                dal.BusDAO bDao = new dal.BusDAO();
                                for (Bus b : busList) {
                                    boolean hasFutureSchedule = bDao.hasFutureSchedule(b.getBusID());
                        %>
                            <tr>
                                <td><%= stt++ %></td>
                                <td class="fw-bold"><%= b.getLicensePlate() %></td>
                                <td><%= b.getCapacity() %></td>
                                <td>
                                    <% if ("Sẵn sàng".equalsIgnoreCase(b.getStatus())) { %>
                                        <span class="badge bg-primary">Sẵn sàng</span>
                                    <% } else if ("Hoạt động".equalsIgnoreCase(b.getStatus())) { %>
                                        <span class="badge bg-success">Hoạt động</span>
                                    <% } else if ("Bảo dưỡng/Sửa chữa".equalsIgnoreCase(b.getStatus())) { %>
                                        <span class="badge bg-danger">Bảo dưỡng/Sửa chữa</span>
                                    <% } else { %>
                                        <span class="badge bg-info"><%= b.getStatus() != null ? b.getStatus() : "Sẵn sàng" %></span>
                                    <% } %>
                                </td>
                                <td>
                                    <% model.User loggedInUser = (model.User) request.getSession().getAttribute("user"); %>
                                    <% String selDate = request.getAttribute("selectedDate") != null ? (String)request.getAttribute("selectedDate") : java.time.LocalDate.now().toString(); %>
                                    <% if ("Sẵn sàng".equalsIgnoreCase(b.getStatus())) { %>
                                        <a href="daily-status?action=report_maint&type=bus&id=<%= b.getBusID() %>&date=<%= selDate %>" class="btn btn-sm btn-warning" onclick="return confirm('Báo bảo dưỡng xe này trong ngày <%= selDate %>?');"><i class="bi bi-tools"></i> Báo bảo dưỡng</a>
                                    <% } else if ("Bảo dưỡng/Sửa chữa".equalsIgnoreCase(b.getStatus())) { %>
                                        <% if (loggedInUser != null && "TECHNICIAN".equalsIgnoreCase(loggedInUser.getRole())) { %>
                                            <a href="daily-status?action=cancel_maint&type=bus&id=<%= b.getBusID() %>&date=<%= selDate %>" class="btn btn-sm btn-secondary" onclick="return confirm('Hủy bảo dưỡng xe này trong ngày <%= selDate %>?');"><i class="bi bi-arrow-counterclockwise"></i> Hủy bảo dưỡng</a>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-secondary" disabled title="Chỉ Kỹ thuật viên mới có quyền hủy bảo dưỡng"><i class="bi bi-arrow-counterclockwise"></i> Hủy bảo dưỡng</button>
                                        <% } %>
                                    <% } %>
                                    <a href="bus-maintenance-history?id=<%= b.getBusID() %>" class="btn btn-sm btn-info text-white"><i class="bi bi-clock-history"></i> Lịch sử</a>
                                    <a href="bus-update?id=<%= b.getBusID() %>" class="btn btn-sm btn-primary"><i class="bi bi-pencil"></i> Sửa gốc</a>
                                    <% if (hasFutureSchedule || "Hoạt động".equalsIgnoreCase(b.getStatus())) { %>
                                        <button class="btn btn-sm btn-secondary" disabled title="Không thể xóa vì xe có lịch hoạt động trong tương lai"><i class="bi bi-trash"></i> Xóa</button>
                                    <% } else { %>
                                        <a href="bus-delete?id=<%= b.getBusID() %>" class="btn btn-sm btn-danger" onclick="return confirm('Bạn có chắc chắn muốn xóa xe này? (Các lịch phân ca cũ vẫn sẽ được giữ lại)');"><i class="bi bi-trash"></i> Xóa</a>
                                    <% } %>
                                </td>
                            </tr>
                        <%
                                }
                            } else {
                        %>
                            <tr>
                                <td colspan="5" class="text-center text-muted py-3">Không có xe bus nào trong hệ thống.</td>
                            </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
