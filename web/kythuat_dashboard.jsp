<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%-- 
    Trang Dashboard của Kỹ thuật viên.
    Cung cấp giao diện để nhân viên kỹ thuật thực hiện:
    - Bắt đầu/Kết thúc ca trực.
    - Xử lý các sự cố xe trên đường (Điều xe dự phòng, đến nơi, bàn giao xe mới, chuyển xe cũ về xưởng).
    - Danh sách các xe đang trong trạng thái bảo dưỡng và nút xác nhận hoàn tất bảo dưỡng.
--%>
<%
    // Kiểm tra session người dùng, chặn truy cập trái phép
    if(session.getAttribute("userRole") == null || !"kythuat".equals(session.getAttribute("userRole"))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    List<Schedule> incidentSchedules = (List<Schedule>) request.getAttribute("incidentSchedules");
    List<Schedule> affectedSchedules = (List<Schedule>) request.getAttribute("affectedSchedules");
    List<Bus> maintenanceBuses = (List<Bus>) request.getAttribute("maintenanceBuses");
    Map<Integer, Bus> busMap = (Map<Integer, Bus>) request.getAttribute("busMap");
    Map<Integer, Integer> studentCountMap = (Map<Integer, Integer>) request.getAttribute("studentCountMap");
    List<TechnicianSchedule> mySchedules = (List<TechnicianSchedule>) request.getAttribute("mySchedules");
    List<Bus> availableBuses = (List<Bus>) request.getAttribute("availableBuses");
    
    // Find today's schedule
    java.time.LocalDate today = java.time.LocalDate.now();
    TechnicianSchedule todaySchedule = null;
    int currentUserID = (int) session.getAttribute("userID");
    if (mySchedules != null) {
        for (TechnicianSchedule ts : mySchedules) {
            if (ts.getDate().toLocalDate().equals(today)) {
                todaySchedule = ts;
                break;
            }
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kỹ Thuật Dashboard - School Bus</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #434343 0%, #000000 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .dashboard-card { border: none; border-radius: 15px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); transition: transform 0.3s ease; }
        .dashboard-card:hover { transform: translateY(-5px); }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container">
            <a class="navbar-brand fw-bold" href="technician-dashboard">
                <i class="bi bi-tools me-2"></i>Kỹ Thuật Panel
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Xin chào, <b><%= session.getAttribute("username") %></b></span>
                <a href="doi_mat_khau.jsp" class="btn btn-sm btn-outline-warning me-2"><i class="bi bi-key"></i> Đổi mật khẩu</a>
                <a href="dang_nhap.jsp" class="btn btn-sm btn-outline-light"><i class="bi bi-box-arrow-right"></i> Đăng xuất</a>
            </div>
        </div>
    </nav>

    <div class="container my-5">
        <h2 class="fw-bold mb-4">Điều phối xe tăng cường & Bảo dưỡng</h2>
        
        <% if ("early_end".equals(request.getParameter("msg"))) { %>
            <div class="alert alert-danger alert-dismissible fade show border-start border-danger border-5">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> <strong>Cảnh báo:</strong> Kỹ thuật viên không thể kết thúc ca trực trước 18:00! Vui lòng hoàn tất sau 18:00.
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        <% } %>
        
        <div class="row g-4">
            
            <%
                Boolean isTodayLeaveApproved = (Boolean) request.getAttribute("isTodayLeaveApproved");
                if (isTodayLeaveApproved == null) isTodayLeaveApproved = false;
                if (isTodayLeaveApproved) {
            %>
            <div class="col-12 mb-2">
                <div class="alert alert-info border-start border-info border-5 py-3 d-flex align-items-center mb-0">
                    <i class="bi bi-info-circle-fill text-info fs-3 me-3"></i>
                    <div>
                        <h6 class="fw-bold mb-0 text-dark">Thông báo: Hôm nay bạn được nghỉ phép (Đã duyệt đơn nghỉ phép).</h6>
                        <p class="text-muted mb-0 small">Lịch làm việc ngày hôm nay của bạn đã được bàn giao cho người khác.</p>
                    </div>
                </div>
            </div>
            <% } %>

            <div class="col-md-12 mb-2">
                <div class="card dashboard-card border-start border-primary border-5">
                    <div class="card-body d-flex justify-content-between align-items-center">
                        <div>
                            <h5 class="fw-bold text-primary mb-1"><i class="bi bi-clock-history me-2"></i>Ca làm việc hôm nay (<%= today.toString() %>)</h5>
                            <% if (todaySchedule == null) { %>
                                <p class="text-muted mb-0">Hôm nay bạn không có lịch trực xưởng.</p>
                            <% } else { 
                                String statusBadge = "bg-warning text-dark";
                                String statusText = "Chưa bắt đầu";
                                if ("IN_PROGRESS".equals(todaySchedule.getStatus())) {
                                    statusBadge = "bg-primary";
                                    statusText = "Đang trong ca";
                                } else if ("COMPLETED".equals(todaySchedule.getStatus())) {
                                    statusBadge = "bg-success";
                                    statusText = "Đã hoàn tất ca";
                                }
                            %>
                                <p class="mb-0">Trạng thái ca: <span class="badge <%= statusBadge %>"><%= statusText %></span></p>
                            <% } %>
                        </div>
                        <div>
                            <% if (todaySchedule != null) { 
                                if ("PENDING".equals(todaySchedule.getStatus())) { %>
                                    <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="technician-action" method="POST" class="m-0">
                                        <input type="hidden" name="action" value="start_shift">
                                        <input type="hidden" name="scheduleID" value="<%= todaySchedule.getTechScheduleID() %>">
                                        <button type="submit" class="btn btn-primary fw-bold px-4 py-2"><i class="bi bi-play-circle-fill me-2"></i> Bắt đầu vào ca</button>
                                    </form>
                            <%  } else if ("IN_PROGRESS".equals(todaySchedule.getStatus())) { 
                                    java.time.LocalTime currentTime = java.time.LocalTime.now();
                                    boolean canEndShift = !currentTime.isBefore(java.time.LocalTime.of(18, 0));
                                    if (canEndShift) {
                            %>
                                    <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="technician-action" method="POST" class="m-0">
                                        <input type="hidden" name="action" value="end_shift">
                                        <input type="hidden" name="scheduleID" value="<%= todaySchedule.getTechScheduleID() %>">
                                        <button type="submit" class="btn btn-success fw-bold px-4 py-2"><i class="bi bi-check-circle-fill me-2"></i> Hoàn tất ca làm</button>
                                    </form>
                            <%      } else { %>
                                    <button type="button" class="btn btn-secondary fw-bold px-4 py-2" disabled title="Bạn chỉ được kết thúc ca làm việc sau 18:00"><i class="bi bi-clock-fill me-2"></i> Hoàn tất ca làm (Sau 18:00)</button>
                            <%      }
                                } 
                               } %>
                        </div>
                    </div>
                </div>
            </div>

            <% if (todaySchedule != null && "IN_PROGRESS".equals(todaySchedule.getStatus())) { %>
            <div class="col-md-7">
                <div class="card dashboard-card border-start border-danger border-5 h-100">
                    <div class="card-body">
                        <h5 class="fw-bold text-danger mb-3"><i class="bi bi-exclamation-triangle-fill me-2"></i>Sự cố trên đường (Cần hỗ trợ)</h5>
                        <% if (incidentSchedules != null && !incidentSchedules.isEmpty()) { %>
                            <div class="list-group">
                            <% for (Schedule s : incidentSchedules) { 
                                String isStatus = s.getIncidentStatus();
                                int brokenBusID = "DRIVER_SWITCHED".equals(isStatus) ? s.getReplacementBusID() : s.getBusID();
                                Bus b = busMap.get(brokenBusID);
                                int studentCount = studentCountMap != null && studentCountMap.containsKey(s.getScheduleID()) ? studentCountMap.get(s.getScheduleID()) : 0;
                                

                            %>
                                <div class="list-group-item list-group-item-action flex-column align-items-start border-danger mb-2 rounded">
                                    <div class="d-flex w-100 justify-content-between">
                                        <h6 class="mb-1 fw-bold">Tuyến ID: <%= s.getRouteID() %> - Lỗi xe <%= b != null ? b.getLicensePlate() : "" %> <span class="text-secondary">(Học sinh: <%= studentCount %>)</span></h6>
                                        <small class="text-danger fw-bold"><%= isStatus.equals("INCIDENT") ? "KHẨN CẤP" : "ĐANG XỬ LÝ" %></small>
                                    </div>
                                    <p class="mb-1">
                                        <% if ("INCIDENT".equals(isStatus)) { %>
                                            Tài xế báo sự cố. Vui lòng chọn xe dự phòng và bấm Điều xe.
                                        <% } else if ("DISPATCHED".equals(isStatus)) { %>
                                            Đã điều xe dự phòng (ID: <%= s.getReplacementBusID() %>). Đang di chuyển...
                                        <% } else if ("ARRIVED".equals(isStatus)) { %>
                                            Đã đến nơi. Hãy bàn giao xe dự phòng cho tài xế.
                                        <% } else if ("HANDED_OVER".equals(isStatus)) { %>
                                            Đã bàn giao xe cho tài xế. Đang chờ tài xế xác nhận Đổi xe...
                                        <% } else if ("DRIVER_SWITCHED".equals(isStatus)) { %>
                                            Tài xế đã nhận xe dự phòng và tiếp tục hành trình. Vui lòng chuyển xe hỏng về xưởng bảo dưỡng.
                                        <% } %>
                                    </p>
                                    
                                    <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="technician-action" method="POST" class="mt-2 text-end">
                                        <input type="hidden" name="scheduleID" value="<%= s.getScheduleID() %>">
                                        <input type="hidden" name="brokenBusID" value="<%= brokenBusID %>">
                                        
                                        <% if ("INCIDENT".equals(isStatus)) { 
                                            int bestFitBusID = -1;
                                            int minDiff = Integer.MAX_VALUE;
                                            int maxCapacity = -1;
                                            int maxCapacityBusID = -1;
                                            if (availableBuses != null && b != null) {
                                                for (Bus ab : availableBuses) {
                                                    if (ab.getCapacity() >= b.getCapacity()) {
                                                        if ((ab.getCapacity() - b.getCapacity()) < minDiff) {
                                                            minDiff = ab.getCapacity() - b.getCapacity();
                                                            bestFitBusID = ab.getBusID();
                                                        }
                                                        if (ab.getCapacity() > maxCapacity) {
                                                            maxCapacity = ab.getCapacity();
                                                            maxCapacityBusID = ab.getBusID();
                                                        }
                                                    }
                                                }
                                            }
                                            if (bestFitBusID == -1) {
                                                bestFitBusID = maxCapacityBusID;
                                            }
                                        %>
                                            <input type="hidden" name="action" value="dispatch_bus">
                                            <div class="d-inline-flex gap-2 align-items-center">
                                                <select name="replacementBusID" class="form-select form-select-sm" required style="width: auto;">
                                                    <option value="" disabled <%= bestFitBusID == -1 ? "selected" : "" %>>-- Chọn xe dự phòng --</option>
                                                    <% if(availableBuses != null && b != null) { 
                                                           for(Bus ab : availableBuses) { 
                                                               if (ab.getCapacity() >= b.getCapacity()) {
                                                    %>
                                                        <option value="<%= ab.getBusID() %>" <%= ab.getBusID() == bestFitBusID ? "selected" : "" %>><%= ab.getLicensePlate() %> (<%= ab.getCapacity() %> chỗ)</option>
                                                    <%         }
                                                           } 
                                                       } %>
                                                </select>
                                                <button type="submit" class="btn btn-sm btn-danger"><i class="bi bi-truck me-1"></i> Điều xe</button>
                                            </div>
                                        <% } else { %>
                                            <% if (s.getHandlingTechID() > 0 && s.getHandlingTechID() != currentUserID) { %>
                                                <button type="button" class="btn btn-sm btn-secondary" disabled>
                                                    <i class="bi bi-lock-fill me-1"></i> Đang được KTV khác xử lý
                                                </button>
                                            <% } else { %>
                                                <% if ("DISPATCHED".equals(isStatus)) { %>
                                                    <input type="hidden" name="action" value="arrive_incident">
                                                    <button type="submit" class="btn btn-sm btn-warning text-dark"><i class="bi bi-geo-alt-fill me-1"></i> Đã đến nơi xe gặp sự cố</button>
                                                <% } else if ("ARRIVED".equals(isStatus)) { %>
                                                    <input type="hidden" name="action" value="handover_bus">
                                                    <button type="submit" class="btn btn-sm btn-primary"><i class="bi bi-key-fill me-1"></i> Bàn giao xe cho lái xe</button>
                                                <% } else if ("HANDED_OVER".equals(isStatus)) { %>
                                                    <!-- Chờ tài xế xác nhận đổi xe -->
                                                    <button type="button" class="btn btn-sm btn-secondary" disabled><i class="bi bi-clock-history me-1"></i> Chờ tài xế xác nhận đổi xe...</button>
                                                <% } else if ("DRIVER_SWITCHED".equals(isStatus)) { %>
                                                    <!-- Chuyển xe cũ vào bảo dưỡng và hoàn tất -->
                                                    <button type="submit" name="action" value="mark_maintenance" class="btn btn-sm btn-secondary"><i class="bi bi-tools me-1"></i> Chuyển xe hỏng về xưởng</button>
                                                <% } %>
                                            <% } %>
                                        <% } %>
                                    </form>
                                </div>
                            <% } %>
                            </div>
                        <% } else { %>
                            <div class="alert alert-success">
                                <i class="bi bi-check-circle-fill me-2"></i> Không có sự cố nào trên đường lúc này.
                            </div>
                        <% } %>
                    </div>
                </div>


            </div>

            <div class="col-md-5">
                <div class="card dashboard-card border-start border-warning border-5 h-100">
                    <div class="card-body">
                        <h5 class="fw-bold text-warning mb-3"><i class="bi bi-tools me-2"></i>Danh sách xe cần bảo dưỡng</h5>
                        <% if (maintenanceBuses != null && !maintenanceBuses.isEmpty()) { %>
                            <ul class="list-group list-group-flush">
                            <% for (Bus b : maintenanceBuses) { %>
                                <li class="list-group-item d-flex justify-content-between align-items-center">
                                    <div>
                                        <strong>Xe: <%= b.getLicensePlate() %></strong><br>
                                        <small class="text-muted"><%= b.getCapacity() %> chỗ</small>
                                        <% if (b.getDescription() != null && !b.getDescription().isEmpty()) { %>
                                             <div class="text-danger small mt-1"><i class="bi bi-exclamation-circle-fill"></i> <%= b.getDescription() %></div>
                                        <% } %>
                                    </div>
                                    <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="technician-action" method="POST">
                                        <input type="hidden" name="action" value="finish_maintenance">
                                        <input type="hidden" name="busID" value="<%= b.getBusID() %>">
                                        <button type="submit" class="btn btn-sm btn-outline-success"><i class="bi bi-check-lg"></i> Hoàn thành</button>
                                    </form>
                                </li>
                            <% } %>
                            </ul>
                        <% } else { %>
                            <p class="text-muted">Không có xe nào đang ở xưởng bảo dưỡng.</p>
                        <% } %>
                    </div>
                </div>
            </div>
            <% } else if (todaySchedule != null && "PENDING".equals(todaySchedule.getStatus())) { %>
            <div class="col-12">
                <div class="alert alert-warning text-center py-4 mb-0 border-warning border-start border-5">
                    <i class="bi bi-info-circle-fill text-warning fs-1 d-block mb-2"></i>
                    <h5 class="fw-bold text-dark">Vui lòng "Bắt đầu vào ca" để xem và xử lý công việc.</h5>
                </div>
            </div>
            <% } %>
        </div>
        <div class="row mt-5">
            <div class="col-12">
                <div class="card dashboard-card">
                    <div class="card-header bg-white border-0 pt-4 pb-0">
                        <h5 class="fw-bold"><i class="bi bi-grid-fill me-2 text-primary"></i>Chức năng</h5>
                    </div>
                    <div class="card-body pt-4 pb-4">
                        <a href="employee-schedule" class="btn btn-outline-primary btn-lg me-3 mb-3 px-4">
                            <i class="bi bi-calendar3 me-2"></i> Lịch làm việc
                        </a>
                        <a href="employee-inbox" class="btn btn-outline-danger btn-lg mb-3 px-4 position-relative">
                            <i class="bi bi-envelope-paper me-2"></i> Hòm thư & Nghỉ phép
                            <% 
                                Long unreadCount = (Long) request.getAttribute("unreadCount");
                                if (unreadCount != null && unreadCount > 0) { 
                            %>
                                <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                                    <%= unreadCount %>
                                </span>
                            <% } %>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </div>
</body>
</html>
