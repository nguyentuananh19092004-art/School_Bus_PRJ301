<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Notification"%>
<%@page import="java.util.List"%>
<%
    if(session.getAttribute("userRole") == null || (!"taixe".equals(session.getAttribute("userRole")) && !"giamthi".equals(session.getAttribute("userRole")) && !"kythuat".equals(session.getAttribute("userRole")))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    String role = (String) session.getAttribute("userRole");
    String dashboardLink = "taixe".equals(role) ? "driver-dashboard" : 
                          ("giamthi".equals(role) ? "monitor-dashboard" : "technician-dashboard");
    String titleRole = "taixe".equals(role) ? "Tài Xế" : 
                      ("giamthi".equals(role) ? "Giám Thị" : "Kỹ Thuật");
    String prefillDate = request.getParameter("prefillDate") != null ? request.getParameter("prefillDate") : "";
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hòm Thư & Nghỉ Phép - <%= titleRole %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .dashboard-card { border: none; border-radius: 15px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); }
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
            <h2 class="fw-bold"><i class="bi bi-envelope-paper text-primary me-2"></i>Hòm thư cá nhân & Xin nghỉ phép</h2>
            <a href="<%= dashboardLink %>" class="btn btn-outline-secondary"><i class="bi bi-arrow-left"></i> Về Dashboard</a>
        </div>

        <div class="row g-4">
            <!-- Form xin nghỉ phép -->
            <div class="col-md-5">
                <div class="card dashboard-card h-100 border-start border-warning border-4">
                    <div class="card-header bg-white border-0 pt-4 pb-0">
                        <h5 class="fw-bold"><i class="bi bi-calendar-x text-warning me-2"></i>Tạo đơn xin nghỉ phép</h5>
                        <p class="text-muted small">Gửi đơn cho Admin để chờ phê duyệt.</p>
                    </div>
                    <div class="card-body">
                        <% String msg = request.getParameter("msg");
                           if ("leave_success".equals(msg)) { %>
                            <div class="alert alert-success py-2"><i class="bi bi-check-circle me-1"></i> Gửi đơn thành công. Xin vui lòng chờ duyệt.</div>
                        <% } else if ("leave_error".equals(msg)) { %>
                            <div class="alert alert-danger py-2"><i class="bi bi-exclamation-triangle me-1"></i> Có lỗi xảy ra khi gửi đơn.</div>
                        <% } else if ("leave_timeout".equals(msg)) { %>
                            <div class="alert alert-danger py-2"><i class="bi bi-x-circle me-1"></i> Đã quá hạn! Bạn chỉ được gửi đơn trước 21:00 (9h tối) ngày hôm trước.</div>
                        <% } 
                           java.time.LocalDateTime now = java.time.LocalDateTime.now();
                           java.time.LocalDate minDate = now.toLocalDate().plusDays(1);
                           if (now.getHour() >= 21) {
                               minDate = minDate.plusDays(1);
                           }
                        %>
                        <form action="leave-request" method="POST">
                            <div class="mb-3">
                                <label class="form-label fw-bold">Ngày xin nghỉ</label>
                                <input type="date" name="leaveDate" class="form-control" value="<%= prefillDate %>" min="<%= minDate.toString() %>" required style="cursor: pointer;" onclick="this.showPicker()">
                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-bold">Lý do nghỉ</label>
                                <textarea name="reason" class="form-control" rows="4" required placeholder="Ví dụ: Bận việc gia đình, ốm đau..."></textarea>
                            </div>
                            <button type="submit" class="btn btn-warning w-100 fw-bold py-2"><i class="bi bi-send me-1"></i> Gửi đơn cho Admin</button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- Hòm thư thông báo -->
            <div class="col-md-7">
                <div class="card dashboard-card h-100 border-start border-primary border-4">
                    <div class="card-header bg-white border-0 pt-4 pb-0 d-flex justify-content-between align-items-center">
                        <h5 class="fw-bold mb-0"><i class="bi bi-bell text-primary me-2"></i>Hộp thư đến</h5>
                    </div>
                    <div class="card-body p-0 mt-3">
                        <ul class="list-group list-group-flush">
                            <%
                                List<Notification> notifs = (List<Notification>) request.getAttribute("notifications");
                                if (notifs != null && !notifs.isEmpty()) {
                                    for (Notification n : notifs) {
                            %>
                            <li class="list-group-item px-4 py-3 <%= n.isRead() ? "bg-light text-muted" : "fw-bold" %>">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="text-primary"><i class="bi bi-info-circle-fill me-1"></i> Thông báo từ Hệ thống</span>
                                    <small class="text-secondary"><%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(n.getCreatedAt()) %></small>
                                </div>
                                <div class="fs-6"><%= n.getMessage() %></div>
                            </li>
                            <%      }
                                } else {
                            %>
                            <li class="list-group-item text-center text-muted py-5 border-0">
                                <i class="bi bi-inbox fs-1 d-block mb-3 text-secondary opacity-50"></i> 
                                <h5>Hòm thư trống</h5>
                                <p>Bạn chưa có thông báo nào từ hệ thống.</p>
                            </li>
                            <%  } %>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
