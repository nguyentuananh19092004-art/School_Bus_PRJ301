<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Bus"%>
<%@page import="model.BusMaintenance"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử bảo dưỡng</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <%
            Bus bus = (Bus) request.getAttribute("bus");
            List<BusMaintenance> history = (List<BusMaintenance>) request.getAttribute("history");
            if (bus != null) {
        %>
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold"><i class="bi bi-clock-history text-info me-2"></i>Lịch sử bảo dưỡng - <%= bus.getLicensePlate() %></h2>
            <div>
                <a href="bus-list" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Quay lại Danh sách</a>
            </div>
        </div>
        
        <div class="card shadow-sm">
            <div class="card-body p-0">
                <table class="table table-hover table-striped mb-0 text-center align-middle">
                    <thead class="table-info">
                        <tr>
                            <th>STT</th>
                            <th>Ngày bảo dưỡng</th>
                            <th>Thời gian báo</th>
                            <th>Nội dung / Lý do hỏng</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (history != null && !history.isEmpty()) {
                                int stt = 1;
                                for (BusMaintenance hm : history) {
                        %>
                            <tr>
                                <td><%= stt++ %></td>
                                <td class="fw-bold"><%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(hm.getMaintenanceDate()) %></td>
                                <td><%= hm.getCreatedAt() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(hm.getCreatedAt()) : "" %></td>
                                <td><%= hm.getDescription() != null ? hm.getDescription() : "Không có nội dung" %></td>
                            </tr>
                        <%      }
                            } else {
                        %>
                            <tr>
                                <td colspan="4" class="text-center text-muted py-4">Chưa có lịch sử bảo dưỡng nào cho xe này.</td>
                            </tr>
                        <%  } %>
                    </tbody>
                </table>
            </div>
        </div>
        <% } else { %>
            <div class="alert alert-danger">Không tìm thấy thông tin xe!</div>
            <a href="bus-list" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Quay lại</a>
        <% } %>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
