<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.*"%>
<%@page import="java.util.List"%>
<%
    if(session.getAttribute("userRole") == null || !"phuhuynh".equals(session.getAttribute("userRole"))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    HocSinh student = (HocSinh) request.getAttribute("student");
    List<StopRouteOption> stopRouteOptions = (List<StopRouteOption>) request.getAttribute("stopRouteOptions");
    Stop currentStop = (Stop) request.getAttribute("currentStop");
    Schedule activeSchedule = (Schedule) request.getAttribute("activeSchedule");
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    Boolean hasBoardedToday = (Boolean) request.getAttribute("hasBoardedToday");
    if (hasBoardedToday == null) hasBoardedToday = false;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phụ Huynh Dashboard - School Bus</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #f6c23e 0%, #dda20a 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .dashboard-card { border: none; border-radius: 15px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); transition: transform 0.3s ease; }
        .dashboard-card:hover { transform: translateY(-5px); }
        .timeline-container { display: flex; justify-content: space-between; align-items: flex-start; position: relative; margin: 30px 0; overflow-x: auto; padding-bottom: 20px;}
        .timeline-line { position: absolute; top: 20px; left: 0; right: 0; height: 3px; background-color: #343a40; z-index: 1; }
        .timeline-item { position: relative; z-index: 2; text-align: center; flex: 1; min-width: 80px;}
        .timeline-circle { width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 15px; border: 2px solid #343a40; }
        .timeline-circle.bg-danger { background-color: #dc3545; }
        .timeline-circle.bg-success { background-color: #92d050; }
        .timeline-circle.bg-secondary { background-color: #d9d9d9; }
        .timeline-label { font-size: 0.85rem; font-weight: 600; border: 2px solid #343a40; padding: 5px; background: white; margin: 0 auto; width: 90%; word-break: break-word; line-height: 1.2; box-shadow: 2px 2px 0px rgba(0,0,0,0.1); }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container">
            <a class="navbar-brand fw-bold" href="parent-dashboard">
                <i class="bi bi-people-fill me-2"></i>Phụ Huynh Panel
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Xin chào, <b><%= session.getAttribute("username") %></b></span>
                <a href="doi_mat_khau.jsp" class="btn btn-sm btn-outline-warning me-2"><i class="bi bi-key"></i> Đổi mật khẩu</a>
                <a href="dang_nhap.jsp" class="btn btn-sm btn-outline-light"><i class="bi bi-box-arrow-right"></i> Đăng xuất</a>
            </div>
        </div>
    </nav>

    <div class="container my-5">
        <% if ("stopped".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-warning alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <i class="bi bi-info-circle-fill me-2"></i><strong>Thông báo:</strong> Dịch vụ đón học sinh bằng xe bus đã được ngưng. Trạng thái học sinh đã chuyển về "Ngưng hoạt động".
            </div>
        <% } %>
        <% if ("stop_pending".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-warning alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <i class="bi bi-info-circle-fill me-2"></i><strong>Thông báo:</strong> Đã ghi nhận yêu cầu hủy dịch vụ. Việc hủy sẽ có hiệu lực từ ngày mai.
            </div>
        <% } %>
        <% if ("leave_success".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-success alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <i class="bi bi-check-circle-fill me-2"></i><strong>Thành công:</strong> Đã ghi nhận yêu cầu xin nghỉ phép của học sinh.
            </div>
        <% } %>
        <% if ("duplicate_leave".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-danger alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <i class="bi bi-exclamation-triangle-fill me-2"></i><strong>Lỗi:</strong> Học sinh đã được báo nghỉ vào ngày này rồi!
            </div>
        <% } else if ("no_active_bus".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-danger alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <i class="bi bi-exclamation-triangle-fill me-2"></i><strong>Lỗi:</strong> Không thể báo nghỉ vì bạn chưa có chuyến xe nào hoạt động trong hôm nay!
            </div>
        <% } else if ("limit_exceeded".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-danger alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Bạn đã thực hiện thay đổi dịch vụ trong hôm nay. Mỗi ngày chỉ được phép thay đổi 1 lần!
            </div>
        <% } else if ("change_pending".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-success alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <i class="bi bi-check-circle-fill me-2"></i><strong>Thành công:</strong> Yêu cầu cập nhật điểm đón đã được ghi nhận và sẽ có hiệu lực từ ngày mai.
            </div>
        <% } %>
        <% if (student != null) { %>
        <h2 class="fw-bold mb-4">Thông tin của học sinh: <span class="text-primary"><%= student.getTenHocSinh() %> (Lớp <%= student.getLop() %>)</span></h2>
        
        <div class="row g-4 mb-4">
            <!-- Tình trạng học sinh -->
            <div class="col-md-6">
                <div class="card dashboard-card h-100 border-start border-warning border-5">
                    <div class="card-body">
                        <h5 class="fw-bold text-warning mb-3">Tình trạng học hôm nay</h5>
                        <% if ("Nghỉ".equals(student.getTrangThai())) { %>
                            <div class="alert alert-danger">
                                <i class="bi bi-info-circle-fill me-2"></i> Gia đình đã báo cho học sinh nghỉ ngày hôm nay.
                            </div>
                        <% } else if ("Ngưng hoạt động".equals(student.getTrangThai()) || (student.getDefaultStopID() == null && student.getPendingStopID() == null)) { %>
                            <div class="alert alert-secondary">
                                <i class="bi bi-exclamation-circle-fill me-2 text-warning"></i> Trạng thái: <strong>Chưa kích hoạt</strong>. Vui lòng thiết lập Điểm đón và Khung giờ ở bên dưới để đăng ký sử dụng dịch vụ xe Bus.
                            </div>
                        <% } else { %>
                            <div class="alert alert-success">
                                <i class="bi bi-check-circle-fill me-2"></i> Trạng thái: Đang hoạt động. 
                                <% if (currentStop != null) { %>
                                    <br><span class="small"><i class="bi bi-geo-alt-fill me-1 ms-3 mt-1"></i> Điểm đón hiện tại: <strong><%= currentStop.getStopName() %></strong></span>
                                <% } else if (student.getPendingStopID() != null) { 
                                       Stop pendingStop = (Stop) request.getAttribute("pendingStop");
                                %>
                                    <br><span class="small"><i class="bi bi-geo-alt-fill me-1 ms-3 mt-1"></i> Điểm đón đã chọn: <strong><%= pendingStop != null ? pendingStop.getStopName() : "" %></strong> (Có hiệu lực từ ngày mai)</span>
                                <% } %>
                            </div>
                            
                            <hr>
                            
                            <% if (currentStop == null) { %>
                                <div class="alert alert-secondary small py-2 mb-3">
                                    <i class="bi bi-info-circle-fill me-1"></i> Xe bus của bạn sẽ bắt đầu chạy từ ngày mai, do đó bạn không cần báo nghỉ cho ngày hôm nay.
                                </div>
                            <% } else if (hasBoardedToday) { %>
                                <div class="alert alert-info small py-2 mb-3">
                                    <i class="bi bi-info-circle-fill me-1"></i> Học sinh đã lên xe hôm nay. Không thể báo vắng mặt.
                                </div>
                                <button type="button" class="btn btn-sm btn-secondary" disabled><i class="bi bi-x-circle me-1"></i> Báo vắng mặt hôm nay</button>
                            <% } else { %>
                                <form action="parent-action" method="POST" class="mb-3">
                                    <input type="hidden" name="action" value="report_absent">
                                    <p class="text-muted small mb-1">Nếu con nghỉ học hôm nay, vui lòng thông báo:</p>
                                    <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('Xác nhận báo cho học sinh nghỉ học ngày hôm nay?');"><i class="bi bi-x-circle me-1"></i> Báo vắng mặt hôm nay</button>
                                </form>
                            <% } %>
                            
                            <hr>
                            
                                <form action="parent-action" method="POST" class="mt-2">
                                    <input type="hidden" name="action" value="report_leave">
                                    <p class="text-muted small mb-1">Xin nghỉ phép ngày khác:</p>
                                    <div class="input-group input-group-sm mb-2">
                                        <input type="date" class="form-control" name="leaveDate" required min="<%= new java.sql.Date(System.currentTimeMillis() + 86400000L) %>">
                                        <button type="submit" class="btn btn-warning"><i class="bi bi-calendar-x"></i> Xin nghỉ</button>
                                    </div>
                                </form>
                            <% 
                                java.util.List<java.sql.Date> upcomingLeaves = (java.util.List<java.sql.Date>) request.getAttribute("upcomingLeaves");
                                if (upcomingLeaves != null && !upcomingLeaves.isEmpty()) { 
                            %>
                                <hr>
                                <p class="text-muted small fw-bold mb-1"><i class="bi bi-calendar2-check"></i> Các ngày đã xin nghỉ sắp tới:</p>
                                <ul class="list-group list-group-sm">
                                <% for (java.sql.Date d : upcomingLeaves) { %>
                                    <li class="list-group-item list-group-item-light py-1 text-danger small"><i class="bi bi-circle-fill" style="font-size: 0.5rem; vertical-align: middle;"></i> <%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(d) %></li>
                                <% } %>
                                </ul>
                            <% } %>
                        <% } %>
                    </div>
                </div>
            </div>
            
            <!-- Trạng thái xe -->
            <div class="col-md-6">
                <div class="card dashboard-card h-100 border-start border-success border-5">
                    <div class="card-body">
                        <h5 class="fw-bold text-success mb-3">Trạng thái xe đến điểm đón</h5>
                        <% if ("Nghỉ".equals(student.getTrangThai())) { %>
                            <p class="text-muted">Học sinh đã báo nghỉ, hệ thống không theo dõi xe cho ngày hôm nay.</p>
                        <% } else if (currentStop == null) { %>
                            <% if (student.getPendingStopID() != null) { 
                                Stop pendingStop = (Stop) request.getAttribute("pendingStop");
                            %>
                                <div class="alert alert-info"><i class="bi bi-info-circle-fill me-2"></i> Điểm đón <strong><%= pendingStop != null ? pendingStop.getStopName() : "" %></strong> của bạn sẽ bắt đầu có hiệu lực từ ngày mai. Hôm nay chưa có chuyến xe nào đi qua.</div>
                            <% } else { %>
                                <div class="alert alert-warning"><i class="bi bi-exclamation-triangle-fill me-2"></i> Chưa cấu hình điểm đón.</div>
                            <% } %>
                        <% } else { %>
                            <p class="mb-2"><i class="bi bi-geo-alt-fill text-danger me-2"></i> <strong>Điểm đón:</strong> <%= currentStop.getStopName() %></p>
                            <% if (activeSchedule != null) { 
                                List<Stop> routeStops = (List<Stop>) request.getAttribute("routeStops");
                                List<Integer> reachedStops = (List<Integer>) request.getAttribute("reachedStops");
                            %>
                                <% if (activeSchedule.getBus() != null) { %>
                                <div class="alert alert-info mt-3">
                                    <i class="bi bi-bus-front-fill me-2"></i> Xe biển số <strong><%= activeSchedule.getBus().getLicensePlate() %></strong> (Tuyến <%= activeSchedule.getRouteID() %>) đang di chuyển. Vui lòng chú ý điện thoại.
                                </div>
                                <% } else { %>
                                <div class="alert alert-info mt-3">
                                    <i class="bi bi-bus-front-fill me-2"></i> Xe tuyến <strong><%= activeSchedule.getRouteID() %></strong> đang di chuyển. Vui lòng chú ý điện thoại.
                                </div>
                                <% } %>

                                <% if (routeStops != null && !routeStops.isEmpty()) { %>
                                <div class="mt-4">
                                    <h6 class="fw-bold text-dark mb-3"><i class="bi bi-signpost-split me-2"></i>Tiến độ lộ trình</h6>
                                    <div class="timeline-container">
                                        <div class="timeline-line"></div>
                                        <% 
                                           boolean activeFound = false;
                                           for (Stop rs : routeStops) { 
                                               boolean isReached = reachedStops != null && reachedStops.contains(rs.getStopID());
                                               String circleClass = "bg-secondary"; // Xám (chưa tới)
                                               if (isReached) {
                                                   circleClass = "bg-danger"; // Đỏ (đã qua)
                                               } else if (!activeFound) {
                                                   circleClass = "bg-success"; // Xanh (sắp tới/đang ở)
                                                   activeFound = true;
                                               }
                                        %>
                                        <div class="timeline-item">
                                            <div class="timeline-circle <%= circleClass %>"></div>
                                            <div class="timeline-label"><%= rs.getStopName() %></div>
                                        </div>
                                        <% } %>
                                    </div>
                                </div>
                                <% } %>
                            <% } else { %>
                                <div class="alert alert-secondary mt-3">
                                    <i class="bi bi-clock me-2"></i> Hiện không có chuyến xe nào hoạt động đi qua điểm đón này.
                                </div>
                            <% } %>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
        
        <div class="row g-4">
            <!-- Cài đặt điểm đón -->
            <div class="col-md-6">
                <div class="card dashboard-card h-100 border-start border-primary border-5">
                    <div class="card-body">
                        <h5 class="fw-bold text-primary mb-3">Thiết lập điểm đón cố định</h5>
                        <% if (student.getEffectiveDate() != null) {
                            if (student.getPendingStopID() == null) {
                        %>
                            <div class="alert alert-warning py-2">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i> <strong>Chờ xử lý:</strong> Bạn đã yêu cầu <strong>hủy dịch vụ</strong> xe bus. Dịch vụ sẽ chính thức ngưng từ ngày mai (<%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(student.getEffectiveDate()) %>).
                            </div>
                        <%    } else {
                                Stop pendingStop = (Stop) request.getAttribute("pendingStop");
                        %>
                            <div class="alert alert-info py-2">
                                <i class="bi bi-info-circle-fill me-2"></i> <strong>Chờ xử lý:</strong> Bạn đã yêu cầu chuyển sang điểm đón <strong><%= pendingStop != null ? pendingStop.getStopName() : "" %></strong>. Thay đổi sẽ có hiệu lực từ ngày mai (<%= new java.text.SimpleDateFormat("dd/MM/yyyy").format(student.getEffectiveDate()) %>).
                            </div>
                        <%    } %>
                            <div class="alert alert-danger py-2">
                                <i class="bi bi-lock-fill me-2"></i> Bạn đã thực hiện thay đổi dịch vụ trong hôm nay. Mỗi ngày chỉ được phép thay đổi 1 lần.
                            </div>
                        <% } %>
                        <form action="parent-action" method="POST" id="formChangeStop">
                            <input type="hidden" name="action" value="change_stop">
                            <div class="mb-3">
                                <label for="stopRoute" class="form-label">Chọn Điểm đón & Khung giờ (Tuyến):</label>
                                <select class="form-select" name="stopRoute" id="stopRoute" <%= student.getPendingStopID() != null ? "disabled" : "required" %>>
                                    <option value="">-- Chọn điểm đón và giờ --</option>
                                    <% if (stopRouteOptions != null) {
                                        for (StopRouteOption sro : stopRouteOptions) { 
                                            String val = sro.getStopID() + "_" + sro.getRouteID();
                                            boolean isSelected = (student.getDefaultStopID() != null && student.getDefaultRouteID() != null && 
                                                                  student.getDefaultStopID() == sro.getStopID() && 
                                                                  student.getDefaultRouteID() == sro.getRouteID());
                                    %>
                                            <option value="<%= val %>" <%= isSelected ? "selected" : "" %>>
                                                <%= sro.getStopName() %> (Đón: <%= sro.getEstimatedTime().toString().substring(0,5) %>, Trả: <%= sro.getReturnTime() != null ? sro.getReturnTime().toString().substring(0,5) : "--" %>)
                                            </option>
                                    <%  } 
                                       } %>
                                </select>
                            </div>
                            
                            <div id="stopMap" style="height: 350px; border-radius: 8px; margin-bottom: 15px; border: 1px solid #dee2e6; z-index: 1;"></div>
                            <small class="text-muted d-block mb-3"><i class="bi bi-info-circle me-1"></i>Bạn có thể click vào các điểm trên bản đồ để chọn nhanh điểm đón.</small>
                            
                            <button type="submit" class="btn btn-primary btn-sm" <%= student.getEffectiveDate() != null ? "disabled" : "" %> onclick="return confirm('LƯU Ý: Bạn chỉ được phép đổi điểm đón 1 LẦN DUY NHẤT trong ngày.\n\nThay đổi sẽ có hiệu lực từ ngày mai. Bạn có chắc chắn muốn lưu điểm đón này không?');"><i class="bi bi-save"></i> Cập nhật điểm đón</button>
                        </form>
                        
                        <% if ("Sử dụng".equals(student.getTrangThai()) || "Nghỉ".equals(student.getTrangThai())) { %>
                        <hr>
                        <h6 class="fw-bold text-danger mb-3">Hủy dịch vụ</h6>
                        <form action="parent-action" method="POST" class="d-inline">
                            <input type="hidden" name="action" value="stop_service">
                            <button type="submit" class="btn btn-outline-danger btn-sm" <%= student.getEffectiveDate() != null ? "disabled" : "" %> onclick="return confirm('LƯU Ý: Bạn chỉ được phép thay đổi dịch vụ 1 LẦN DUY NHẤT trong ngày.\n\nBạn có chắc chắn muốn ngưng dịch vụ đón học sinh bằng xe bus?\n(Dịch vụ sẽ ngừng từ ngày mai đồng thời trạng thái chuyển về ngưng hoạt động)');">
                                <i class="bi bi-x-circle me-1"></i> Ngưng dịch vụ xe bus
                            </button>
                        </form>
                        <% } %>
                    </div>
                </div>
            </div>
            
            <!-- Hộp thư thông báo -->
            <div class="col-md-6">
                <div class="card dashboard-card h-100 border-start border-info border-5">
                    <div class="card-body">
                        <h5 class="fw-bold text-info mb-3"><i class="bi bi-bell-fill me-2"></i>Hộp thư thông báo</h5>
                        <% if (notifications != null && !notifications.isEmpty()) { %>
                            <div class="list-group" style="max-height: 300px; overflow-y: auto;">
                                <% for (Notification n : notifications) { %>
                                    <div class="list-group-item list-group-item-action <%= !n.isRead() ? "list-group-item-light fw-bold border-info" : "" %>">
                                        <div class="d-flex w-100 justify-content-between">
                                            <p class="mb-1"><%= n.getMessage() %></p>
                                            <small class="text-muted"><%= n.getCreatedAt() %></small>
                                        </div>
                                        <% if (!n.isRead()) { %>
                                        <form action="parent-action" method="POST" class="text-end mt-1">
                                            <input type="hidden" name="action" value="mark_read">
                                            <input type="hidden" name="notifID" value="<%= n.getNotifID() %>">
                                            <button type="submit" class="btn btn-sm btn-link text-decoration-none p-0">Đánh dấu đã đọc</button>
                                        </form>
                                        <% } %>
                                    </div>
                                <% } %>
                            </div>
                        <% } else { %>
                            <p class="text-muted">Chưa có thông báo nào từ nhà trường.</p>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
        <% } else { %>
            <div class="alert alert-danger">Lỗi tải thông tin học sinh.</div>
        <% } %>
    </div>

    <!-- Leaflet JS -->
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <script>
        document.addEventListener("DOMContentLoaded", function() {
            var map = L.map('stopMap').setView([21.028511, 105.804817], 11); // Default Hanoi
            
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: '© OpenStreetMap'
            }).addTo(map);

            window.blueIcon = new L.Icon({
              iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
              shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
              iconSize: [25, 41],
              iconAnchor: [12, 41],
              popupAnchor: [1, -34],
              shadowSize: [41, 41]
            });

            window.redIcon = new L.Icon({
              iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
              shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
              iconSize: [25, 41],
              iconAnchor: [12, 41],
              popupAnchor: [1, -34],
              shadowSize: [41, 41]
            });

            window.markersMap = {};

            var stops = [
                <% if (stopRouteOptions != null) {
                    for (int i = 0; i < stopRouteOptions.size(); i++) {
                        model.StopRouteOption sro = stopRouteOptions.get(i);
                        String val = sro.getStopID() + "_" + sro.getRouteID();
                        String estTime = sro.getEstimatedTime() != null ? sro.getEstimatedTime().toString().substring(0,5) : "--";
                        String retTime = sro.getReturnTime() != null ? sro.getReturnTime().toString().substring(0,5) : "--";
                %>
                {
                    lat: <%= sro.getLatitude() %>,
                    lng: <%= sro.getLongitude() %>,
                    val: '<%= val %>',
                    name: '<%= sro.getStopName().replace("'", "\\'") %>',
                    desc: 'Đón: <%= estTime %>, Trả: <%= retTime %>'
                }<%= (i < stopRouteOptions.size() - 1) ? "," : "" %>
                <%  }
                   } %>
            ];

            var bounds = [];
            stops.forEach(function(stop) {
                if (stop.lat && stop.lng) {
                    var marker = L.marker([stop.lat, stop.lng], {icon: window.blueIcon}).addTo(map);
                    window.markersMap[stop.val] = marker;
                    bounds.push([stop.lat, stop.lng]);
                    
                    var popupContent = '<div class="text-center"><b>' + stop.name + '</b><br/>' + stop.desc + 
                                       '<br/><button type="button" class="btn btn-sm btn-primary mt-2 w-100" onclick="selectStop(\'' + stop.val + '\')">Chọn điểm này</button></div>';
                    marker.bindPopup(popupContent);
                    
                    // On click marker, select the option
                    marker.on('click', function() {
                        selectStop(stop.val);
                    });
                }
            });

            if (bounds.length > 0) {
                map.fitBounds(bounds, {padding: [20, 20]});
            }

            // Bind change event to select dropdown
            document.getElementById('stopRoute').addEventListener('change', updateMarkersColor);
            
            // Initial call to set color for already selected stop
            updateMarkersColor();
        });

        function updateMarkersColor() {
            var selectedVal = document.getElementById('stopRoute').value;
            for (var key in window.markersMap) {
                if (key === selectedVal) {
                    window.markersMap[key].setIcon(window.redIcon);
                } else {
                    window.markersMap[key].setIcon(window.blueIcon);
                }
            }
        }

        function selectStop(val) {
            var select = document.getElementById('stopRoute');
            select.value = val;
            updateMarkersColor();
            
            // Visual feedback
            select.classList.add('is-valid');
            setTimeout(function() {
                select.classList.remove('is-valid');
            }, 1500);
        }
    </script>
</body>
</html>
