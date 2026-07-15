<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Schedule"%>
<%@page import="dal.BusDAO"%>
<%@page import="dal.UserDAO"%>
<%@page import="java.util.List"%>
<%-- 
    Trang Xem Lịch làm việc cá nhân.
    Dành cho Tài xế, Giám thị hoặc Kỹ thuật viên để xem lịch phân công nhiệm vụ của cá nhân trong một tuần.
--%>
<%
    // Kiểm tra session người dùng, chặn truy cập trái phép
    if(session.getAttribute("userRole") == null || (!"taixe".equals(session.getAttribute("userRole")) && !"giamthi".equals(session.getAttribute("userRole")) && !"kythuat".equals(session.getAttribute("userRole")))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    String role = (String) session.getAttribute("userRole");
    String dashboardLink = "taixe".equals(role) ? "driver-dashboard" : 
                          ("giamthi".equals(role) ? "monitor-dashboard" : "technician-dashboard");
    String titleRole = "taixe".equals(role) ? "Tài Xế" : 
                      ("giamthi".equals(role) ? "Giám Thị" : "Kỹ Thuật");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch Làm Việc - <%= titleRole %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .dashboard-card { border: none; border-radius: 15px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
        .btn-custom { transition: transform 0.2s; }
        .btn-custom:hover { transform: translateY(-2px); }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container">
            <a class="navbar-brand fw-bold" href="<%= dashboardLink %>">
                <i class="bi <%= "taixe".equals(role) ? "bi-car-front-fill" : ("giamthi".equals(role) ? "bi-person-badge-fill" : "bi-tools") %> me-2"></i><%= titleRole %> Panel
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Xin chào, <b><%= session.getAttribute("username") %></b></span>
                <a href="doi_mat_khau.jsp" class="btn btn-sm btn-outline-warning me-2"><i class="bi bi-key"></i> Đổi mật khẩu</a>
                <a href="dang_nhap.jsp" class="btn btn-sm btn-outline-light"><i class="bi bi-box-arrow-right"></i> Đăng xuất</a>
            </div>
        </div>
    </nav>

    <div class="container my-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2 class="fw-bold"><i class="bi bi-calendar3 text-primary me-2"></i>Tra cứu Lịch làm việc</h2>
            <a href="<%= dashboardLink %>" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Về Dashboard</a>
        </div>

        <div class="card dashboard-card mb-4">
            <div class="card-body bg-light rounded">
                <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="employee-schedule" method="GET" class="d-flex align-items-end">
                    <div class="me-3">
                        <label class="form-label fw-bold">Chọn ngày muốn xem:</label>
                        <input type="date" name="date" class="form-control" value="<%= request.getAttribute("selectedDate") %>" required style="cursor: pointer;" onclick="this.showPicker()">
                    </div>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-search me-1"></i> Tra cứu</button>
                    
                    <div class="ms-auto">
                        <a href="employee-inbox?prefillDate=<%= request.getAttribute("selectedDate") %>" class="btn btn-warning fw-bold btn-custom">
                            <i class="bi bi-calendar-x"></i> Xin nghỉ phép ngày này
                        </a>
                    </div>
                </form>
            </div>
        </div>

        <h5 class="fw-bold mb-3">Lịch trình ngày: <span class="text-primary"><%= request.getAttribute("selectedDate") %></span></h5>
        
        <div class="row g-4">
            <%
                List<Schedule> schedules = (List<Schedule>) request.getAttribute("schedules");
                List<model.TechnicianSchedule> techSchedules = (List<model.TechnicianSchedule>) request.getAttribute("techSchedules");
                BusDAO busDAO = (BusDAO) request.getAttribute("busDAO");
                UserDAO userDAO = (UserDAO) request.getAttribute("userDAO");
                
                boolean hasAnySchedule = false;
                
                if ("kythuat".equals(role)) {
                    if (techSchedules != null && !techSchedules.isEmpty()) {
                        hasAnySchedule = true;
                        for (model.TechnicianSchedule ts : techSchedules) {
                            String statusBadge = "bg-warning text-dark";
                            String statusText = "Chờ xử lý";
                            if ("IN_PROGRESS".equals(ts.getStatus())) {
                                statusBadge = "bg-primary";
                                statusText = "Đang thực hiện";
                            } else if ("COMPLETED".equals(ts.getStatus())) {
                                statusBadge = "bg-success";
                                statusText = "Hoàn tất";
                            }
            %>
            <div class="col-md-6">
                <div class="card dashboard-card h-100 border-start border-warning border-4">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="fw-bold mb-0 text-warning text-dark"><i class="bi bi-tools me-2"></i>Ca Kỹ thuật</h5>
                            <span class="badge <%= statusBadge %>"><%= statusText %></span>
                        </div>
                        <p class="mb-1"><i class="bi bi-person-workspace text-muted me-2"></i> Người xử lý: <b><%= ts.getTechnicianName() %></b></p>
                        <p class="mb-0"><i class="bi bi-clock-history text-muted me-2"></i> Ngày tạo: <b><%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts.getCreatedAt()) %></b></p>
                    </div>
                </div>
            </div>
            <%          }
                    }
                } else {
                    if (schedules != null && !schedules.isEmpty()) {
                        hasAnySchedule = true;
                        for (Schedule s : schedules) {
                            String busPlate = busDAO.getBusById(s.getBusID()) != null ? busDAO.getBusById(s.getBusID()).getLicensePlate() : "";
                            String monitorName = userDAO.getUserById(s.getMonitorID()) != null ? userDAO.getUserById(s.getMonitorID()).getFullName() : "";
                            String driverName = userDAO.getUserById(s.getDriverID()) != null ? userDAO.getUserById(s.getDriverID()).getFullName() : "";
            %>
            <div class="col-md-6">
                <div class="card dashboard-card h-100 border-start border-primary border-4">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5 class="fw-bold mb-0 text-primary">Tuyến ID: <%= s.getRouteID() %></h5>
                            <span class="badge bg-<%= s.getStatus().equals("PENDING") ? "warning text-dark" : s.getStatus().equals("COMPLETED") ? "success" : "info" %>"><%= s.getStatus() %></span>
                        </div>
                        <p class="mb-1"><i class="bi bi-arrow-right-circle text-muted me-2"></i> Chiều đi: <b><%= s.getDirection().equals("TO_SCHOOL") ? "Đến trường" : "Về nhà" %></b></p>
                        <p class="mb-1"><i class="bi bi-bus-front text-muted me-2"></i> Xe: <b><%= busPlate %></b></p>
                        <% if ("taixe".equals(role)) { %>
                            <p class="mb-0"><i class="bi bi-person-badge text-muted me-2"></i> Giám sát đi cùng: <b><%= monitorName %></b></p>
                        <% } else { %>
                            <p class="mb-0"><i class="bi bi-person-vcard text-muted me-2"></i> Lái xe: <b><%= driverName %></b></p>
                        <% } %>
                    </div>
                </div>
            </div>
            <%
                        }
                    }
                }
                
                if (!hasAnySchedule) {
            %>
            <div class="col-12">
                <div class="alert alert-light border text-center py-5">
                    <i class="bi bi-calendar2-check text-success" style="font-size: 3rem;"></i>
                    <h5 class="mt-3 fw-bold text-muted">Bạn không có ca làm việc nào trong ngày này.</h5>
                </div>
            </div>
            <%  } %>
        </div>
    </div>
</body>
</html>
