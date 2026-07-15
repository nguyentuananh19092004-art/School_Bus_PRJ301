<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Dashboard của Admin.
    Hiển thị tổng quan hệ thống (số lượng học sinh, xe, tuyến, tài khoản).
    Cung cấp các liên kết truy cập nhanh đến các chức năng quản lý, phân ca, và hòm thư duyệt phép.
--%>
<%
    // Kiểm tra đăng nhập: Chặn các truy cập trái phép không có quyền 'admin'
    // Nếu không phải admin, đá văng về trang đăng nhập
    // Kiểm tra session người dùng, chặn truy cập trái phép
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
    <title>Admin Dashboard - School Bus</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f4f6f9;
        }
        .navbar {
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        .dashboard-card {
            border: none;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.05);
            transition: transform 0.3s ease;
        }
        .dashboard-card:hover {
            transform: translateY(-5px);
        }
        .icon-box {
            width: 60px; height: 60px;
            border-radius: 15px;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.8rem; color: white;
        }
        .bg-gradient-primary { background: linear-gradient(135deg, #4e73df 0%, #224abe 100%); }
        .bg-gradient-success { background: linear-gradient(135deg, #1cc88a 0%, #13855c 100%); }
        .bg-gradient-warning { background: linear-gradient(135deg, #f6c23e 0%, #dda20a 100%); }
        .bg-gradient-danger { background: linear-gradient(135deg, #e74a3b 0%, #be2617 100%); }
    </style>
</head>
<body>

    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container">
            <a class="navbar-brand fw-bold" href="#">
                <i class="bi bi-shield-lock-fill me-2"></i>Admin Panel
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Xin chào, <b><%= session.getAttribute("username") %></b></span>
                <a href="doi_mat_khau.jsp" class="btn btn-sm btn-outline-warning me-2"><i class="bi bi-key"></i> Đổi mật khẩu</a>
                <a href="dang_nhap.jsp" class="btn btn-sm btn-outline-light"><i class="bi bi-box-arrow-right"></i> Đăng xuất</a>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <div class="container my-5">
        <h2 class="fw-bold mb-4">Tổng quan hệ thống</h2>
        
        <!-- Section 1: Các thẻ thống kê (Stats Cards) - Lấy số liệu từ DB qua Controller -->
        <div class="row g-4">
            <!-- Card 1 -->
            <div class="col-md-3">
                <div class="card dashboard-card h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted fw-bold mb-1">TỔNG SỐ HỌC SINH</h6>
                                <h3 class="fw-bold mb-0 text-dark"><%= request.getAttribute("totalStudents") != null ? request.getAttribute("totalStudents") : 0 %></h3>
                            </div>
                            <div class="icon-box bg-gradient-primary shadow-sm">
                                <i class="bi bi-people-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Card 2 -->
            <div class="col-md-3">
                <div class="card dashboard-card h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted fw-bold mb-1">SỐ LƯỢNG XE BUS</h6>
                                <h3 class="fw-bold mb-0 text-dark"><%= request.getAttribute("totalBuses") != null ? request.getAttribute("totalBuses") : 0 %></h3>
                            </div>
                            <div class="icon-box bg-gradient-success shadow-sm">
                                <i class="bi bi-bus-front"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Card 3 -->
            <div class="col-md-3">
                <div class="card dashboard-card h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted fw-bold mb-1">TUYẾN ĐƯỜNG</h6>
                                <h3 class="fw-bold mb-0 text-dark"><%= request.getAttribute("totalRoutes") != null ? request.getAttribute("totalRoutes") : 0 %></h3>
                            </div>
                            <div class="icon-box bg-gradient-warning shadow-sm">
                                <i class="bi bi-map-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Card 4 -->
            <div class="col-md-3">
                <div class="card dashboard-card h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="text-muted fw-bold mb-1">TÀI KHOẢN</h6>
                                <h3 class="fw-bold mb-0 text-dark"><%= request.getAttribute("totalUsers") != null ? request.getAttribute("totalUsers") : 0 %></h3>
                            </div>
                            <div class="icon-box bg-gradient-danger shadow-sm">
                                <i class="bi bi-person-badge-fill"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Section 2: Chức năng quản lý và Thông báo -->
        <div class="row mt-5">
            <!-- Bảng liên kết quản lý nhanh -->
            <div class="col-md-7">
                <div class="card dashboard-card h-100">
                    <div class="card-header bg-white border-0 pt-4 pb-0">
                        <h5 class="fw-bold"><i class="bi bi-gear-fill me-2 text-primary"></i>Quản lý nhanh</h5>
                    </div>
                    <div class="card-body">
                        <a href="hocsinh-list" class="btn btn-outline-primary me-2 mb-2"><i class="bi bi-person-lines-fill me-1"></i> Quản lý Học Sinh</a>
                        <a href="ScheduleServlet" class="btn btn-outline-success me-2 mb-2"><i class="bi bi-calendar-check me-1"></i> Phân ca & Lịch trình</a>
                        <a href="bus-list" class="btn btn-outline-warning me-2 mb-2"><i class="bi bi-bus-front me-1"></i> Quản lý Xe Bus</a>
                        <a href="route-management" class="btn btn-outline-primary me-2 mb-2"><i class="bi bi-map-fill me-1"></i> Quản lý Lộ trình</a>
                        <a href="user-list?role=DRIVER" class="btn btn-outline-info me-2 mb-2"><i class="bi bi-person-vcard me-1"></i> Quản lý Lái xe</a>
                        <a href="user-list?role=MONITOR" class="btn btn-outline-dark me-2 mb-2"><i class="bi bi-eye-fill me-1"></i> Quản lý Giám sát</a>
                        <a href="user-list?role=TECHNICIAN" class="btn btn-outline-secondary me-2 mb-2"><i class="bi bi-tools me-1"></i> Quản lý Kỹ thuật</a>
                        <a href="admin-inbox" class="btn btn-danger mb-2 position-relative">
                            <i class="bi bi-envelope-open-heart me-1"></i> Hòm thư Duyệt phép
                            <% if (Boolean.TRUE.equals(request.getAttribute("hasPendingLeaves"))) { %>
                            <span class="position-absolute top-0 start-100 translate-middle p-2 bg-warning border border-light rounded-circle">
                                <span class="visually-hidden">New alerts</span>
                            </span>
                            <% } %>
                        </a>
                    </div>
                </div>
            </div>
            
            <!-- Bảng thông báo khẩn cấp (Sự cố xe, Hủy chuyến) -->
            <div class="col-md-5">
                <div class="card dashboard-card h-100 border-start border-danger border-5">
                    <div class="card-body">
                        <h5 class="fw-bold text-danger mb-3"><i class="bi bi-bell-fill me-2"></i>Thông báo khẩn cấp</h5>
                        <%-- Parse danh sách thông báo được nhúng từ Servlet --%>
                        <% java.util.List<model.Notification> notifications = (java.util.List<model.Notification>) request.getAttribute("notifications"); %>
                        <% if (notifications != null && !notifications.isEmpty()) { %>
                            <div class="list-group" style="max-height: 300px; overflow-y: auto;">
                                <% for (model.Notification n : notifications) { 
                                    String text = n.getMessage();
                                    int schId = -1;
                                    String targetDate = "";
                                    if (text.contains("|DATE:")) {
                                        int start = text.indexOf("|DATE:") + 6;
                                        int end = text.indexOf("|", start);
                                        if (end != -1) {
                                            targetDate = text.substring(start, end);
                                            text = text.substring(0, text.indexOf("|DATE:"));
                                        }
                                    } else if (text.contains("|SCHEDULE_ID:")) {
                                        int start = text.indexOf("|SCHEDULE_ID:") + 13;
                                        int end = text.indexOf("|", start);
                                        if (end != -1) {
                                            schId = Integer.parseInt(text.substring(start, end));
                                            text = text.substring(0, text.indexOf("|SCHEDULE_ID:"));
                                        }
                                    }
                                %>
                                    <div class="list-group-item list-group-item-action <%= !n.isRead() ? "list-group-item-light fw-bold border-danger" : "" %>">
                                        <div class="d-flex w-100 justify-content-between">
                                            <p class="mb-1"><%= text %></p>
                                            <small class="text-muted"><%= new java.text.SimpleDateFormat("dd/MM HH:mm").format(n.getCreatedAt()) %></small>
                                        </div>
                                        <% if (!n.isRead()) { %>
                                        <div class="d-flex justify-content-end mt-1 gap-3">
                                            <% if (!targetDate.isEmpty()) { %>
                                                <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="AdminDashboardServlet" method="POST" class="m-0">
                                                    <input type="hidden" name="action" value="redirect_to_schedule">
                                                    <input type="hidden" name="targetDate" value="<%= targetDate %>">
                                                    <input type="hidden" name="notifID" value="<%= n.getNotifID() %>">
                                                    <button type="submit" class="btn btn-sm btn-danger py-0"><i class="bi bi-calendar-plus"></i> Phân ca lại</button>
                                                </form>
                                            <% } else if (schId != -1) { %>
                                                <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="AdminDashboardServlet" method="POST" class="m-0">
                                                    <input type="hidden" name="action" value="redirect_to_schedule">
                                                    <input type="hidden" name="scheduleID" value="<%= schId %>">
                                                    <input type="hidden" name="notifID" value="<%= n.getNotifID() %>">
                                                    <button type="submit" class="btn btn-sm btn-danger py-0"><i class="bi bi-arrow-repeat"></i> Đổi xe ngay</button>
                                                </form>
                                            <% } %>
                                            <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="AdminDashboardServlet" method="POST" class="m-0">
                                                <input type="hidden" name="action" value="mark_read">
                                                <input type="hidden" name="notifID" value="<%= n.getNotifID() %>">
                                                <button type="submit" class="btn btn-sm btn-link text-decoration-none p-0 text-secondary">Đã đọc</button>
                                            </form>
                                        </div>
                                        <% } %>
                                    </div>
                                <% } %>
                            </div>
                        <% } else { %>
                            <p class="text-muted">Không có thông báo nào.</p>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
    </div>


</body>
</html>
