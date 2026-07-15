<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%-- 
    Trang Dashboard của Giám thị (Monitor).
    Hiển thị danh sách học sinh theo từng điểm dừng (trạm) của chuyến xe hiện tại.
    Cho phép Giám thị điểm danh học sinh, xác nhận đã đến điểm dừng, và gọi phụ huynh.
--%>
<%
    if(session.getAttribute("userRole") == null || !"giamthi".equals(session.getAttribute("userRole"))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    Schedule schedule = (Schedule) request.getAttribute("activeSchedule");
    Bus bus = (Bus) request.getAttribute("bus");
    User driver = (User) request.getAttribute("driver");
    List<Stop> stops = (List<Stop>) request.getAttribute("stops");
    Map<Integer, List<HocSinh>> studentsByStop = (Map<Integer, List<HocSinh>>) request.getAttribute("studentsByStop");
    List<Integer> reachedStops = (List<Integer>) request.getAttribute("reachedStops");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giám Thị Dashboard - School Bus</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #1cc88a 0%, #13855c 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .dashboard-card { border: none; border-radius: 15px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); transition: transform 0.3s ease; }
        .dashboard-card:hover { transform: translateY(-5px); }
        .stop-item { border-left: 3px solid #1cc88a; padding-left: 15px; margin-bottom: 20px; position: relative; }
        .stop-item::before { content: ''; position: absolute; left: -9px; top: 0; width: 15px; height: 15px; border-radius: 50%; background: #1cc88a; }
        .stop-reached { border-left-color: #858796; opacity: 0.7; }
        .stop-reached::before { background: #858796; }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container">
            <a class="navbar-brand fw-bold" href="monitor-dashboard">
                <i class="bi bi-person-badge-fill me-2"></i>Giám Thị Panel
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Xin chào, <b><%= session.getAttribute("username") %></b></span>
                <a href="doi_mat_khau.jsp" class="btn btn-sm btn-outline-warning me-2"><i class="bi bi-key"></i> Đổi mật khẩu</a>
                <a href="dang_nhap.jsp" class="btn btn-sm btn-outline-light"><i class="bi bi-box-arrow-right"></i> Đăng xuất</a>
            </div>
        </div>
    </nav>

    <div class="container my-5">
        <h2 class="fw-bold mb-4">Nhiệm vụ hôm nay</h2>
        
        <% if (schedule != null) { %>
        <div class="row g-4">
            <div class="col-md-12">
                <div class="card dashboard-card border-start border-success border-5">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <div>
                                <h5 class="fw-bold text-success">Chuyến xe đang giám sát</h5>
                                <p class="text-muted mb-1"><i class="bi bi-bus-front me-2"></i> Xe <%= bus != null ? bus.getLicensePlate() : "" %> (Chuyến: <%= schedule.getDirection().equals("TO_SCHOOL") ? "Đến trường" : "Về nhà" %>)</p>
                                <p class="text-muted mb-1"><i class="bi bi-person-badge me-2"></i> Tài xế: <b><%= driver != null ? driver.getFullName() : "Chưa có" %></b></p>
                                <p class="text-muted mb-0"><i class="bi bi-geo-alt me-2"></i> ID Tuyến: <%= schedule.getRouteID() %></p>
                            </div>
                            <span class="badge <%= schedule.getStatus().equals("PENDING") ? "bg-warning text-dark" : "bg-primary" %> fs-6">
                                <%= schedule.getStatus() %>
                            </span>
                        </div>
                        <hr>
                        <h6 class="fw-bold mb-3">Lộ trình và Danh sách điểm đón:</h6>
                        <% if (stops != null && !stops.isEmpty()) { 
                            boolean isReturn = "TO_HOME".equals(schedule.getDirection()) || "Về nhà".equals(schedule.getDirection());
                            List<String> attendedStudents = (List<String>) request.getAttribute("attendedStudents");
                            boolean isActiveStopFound = false;
                            for (int i = 0; i < stops.size(); i++) { 
                                Stop s = stops.get(i);
                                boolean isSchoolStop = ("TO_SCHOOL".equals(schedule.getDirection()) && i == stops.size() - 1) || 
                                                       ("TO_HOME".equals(schedule.getDirection()) && i == 0);
                                String actionText = isSchoolStop ? (isReturn ? "Học sinh lên xe:" : "Học sinh xuống xe:") : (isReturn ? "Học sinh xuống xe:" : "Học sinh lên xe:");
                                String buttonText = isSchoolStop ? (isReturn ? "Đã Lên" : "Đã Xuống") : (isReturn ? "Đã Xuống" : "Đã Lên");
                                boolean isReached = reachedStops != null && reachedStops.contains(s.getStopID());
                                boolean isActiveStop = false;
                                if (!isReached && !isActiveStopFound) {
                                    isActiveStop = true;
                                    isActiveStopFound = true;
                                }
                                List<HocSinh> hsList = studentsByStop.get(s.getStopID());
                                boolean allAttended = true;
                                java.util.List<String> checkedAtStops = (java.util.List<String>) request.getAttribute("checkedAtStops");
                                if (hsList != null && !hsList.isEmpty()) {
                                    for (HocSinh hs : hsList) {
                                        boolean isChecked = isSchoolStop ? 
                                            (attendedStudents != null && attendedStudents.contains(hs.getMaHocSinh())) : 
                                            (checkedAtStops != null && checkedAtStops.contains(hs.getMaHocSinh()));
                                        if (!"Nghỉ".equals(hs.getTrangThai()) && !isChecked) {
                                            allAttended = false;
                                            break;
                                        }
                                    }
                                }
                        %>
                        <div class="stop-item <%= isReached ? "stop-reached" : "" %>">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <h6 class="fw-bold mb-0 text-primary"><%= s.getStopName() %> <small class="text-muted">(<%= s.getStopID() == 0 ? "--" : (isReturn ? s.getReturnTime() : s.getEstimatedTime()) %>)</small></h6>
                                <% if (!"IN_PROGRESS".equals(schedule.getStatus())) { %>
                                    <span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split"></i> Chờ tài xế bắt đầu vào lộ trình</span>
                                <% } else if (!isReached && isActiveStop) { 
                                       boolean hasIncident = schedule.getIncidentStatus() != null && !"NORMAL".equals(schedule.getIncidentStatus()) && !"DRIVER_SWITCHED".equals(schedule.getIncidentStatus());
                                       if (hasIncident) {
                                %>
                                    <span class="badge bg-danger"><i class="bi bi-tools"></i> Xe hỏng, chờ đổi xe...</span>
                                <%     } else { %>
                                <form action="monitor-action" method="POST" class="m-0">
                                    <input type="hidden" name="action" value="reach_stop">
                                    <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                                    <input type="hidden" name="stopID" value="<%= s.getStopID() %>">
                                    <input type="hidden" name="stopName" value="<%= s.getStopName() %>">
                                    <input type="hidden" name="direction" value="<%= schedule.getDirection() %>">
                                    <input type="hidden" name="isSchoolStop" value="<%= isSchoolStop %>">
                                    <input type="hidden" name="assignedStudents" value="<% if (hsList != null) for(HocSinh hs : hsList) if (!"Nghỉ".equals(hs.getTrangThai())) out.print(hs.getMaHocSinh() + ","); %>">
                                    <button type="submit" class="btn btn-sm btn-success" <%= (isSchoolStop || allAttended) ? "" : "disabled title='Vui lòng điểm danh tất cả học sinh trước khi qua điểm'" %>
                                        <% if (isSchoolStop) { %>
                                            onclick="return confirm('<%= isReturn ? "Xác nhận tất cả học sinh lên xe?" : "Xác nhận tất cả học sinh xuống xe?" %>');"
                                        <% } else { %>
                                            onclick="return confirm('Hoàn tất điểm đón này?');"
                                        <% } %>
                                    >
                                        <i class="bi bi-check2-circle"></i> Hoàn tất
                                    </button>
                                </form>
                                <%     } 
                                   } else if (!isReached) { %>
                                    <span class="badge bg-secondary">Chờ đến điểm</span>
                                <% } else { %>
                                    <span class="badge bg-secondary"><i class="bi bi-check-all"></i> Đã đi qua</span>
                                <% } %>
                            </div>
                            <p class="text-muted small mb-2"><i class="bi bi-geo me-1"></i> <%= s.getAddress() %></p>
                            
                            <!-- Danh sách học sinh tại điểm này -->
                            <div class="bg-light p-3 rounded">
                                <strong><i class="bi bi-people me-1"></i> <%= actionText %></strong>
                                <% if (hsList != null && !hsList.isEmpty()) { %>
                                   <ul class="list-unstyled mt-2 mb-0">
                                   <% for (HocSinh hs : hsList) { %>
                                       <li class="d-flex justify-content-between align-items-center border-bottom pb-2 mb-2">
                                           <span><%= hs.getTenHocSinh() %> (<%= hs.getLop() %>)</span>
                                           <div>
                                               <% 
                                                  boolean isChecked = isSchoolStop ? 
                                                      (attendedStudents != null && attendedStudents.contains(hs.getMaHocSinh())) : 
                                                      (checkedAtStops != null && checkedAtStops.contains(hs.getMaHocSinh()));
                                               %>
                                               <% if ("Nghỉ".equals(hs.getTrangThai())) { %>
                                                   <span class="badge bg-danger">Báo nghỉ</span>
                                               <% } else if (isChecked) { %>
                                                   <span class="badge bg-success"><i class="bi bi-check-circle"></i> Đã điểm danh</span>
                                               <% } else if (isSchoolStop) { %>
                                                    <% if (isReached) { %>
                                                        <span class="badge bg-success"><i class="bi bi-check-circle"></i> <%= isReturn ? "Đã lên xe" : "Đã xuống xe" %></span>
                                                    <% } else { %>
                                                        <span class="badge bg-secondary text-light">Chờ hoàn tất</span>
                                                    <% } %>
                                               <% } else { %>
                                                    <% if (!"IN_PROGRESS".equals(schedule.getStatus())) { %>
                                                        <span class="badge bg-warning text-dark border"><i class="bi bi-hourglass-top"></i> Xe chưa vào lộ trình</span>
                                                    <% } else if (!isReached && isActiveStop) { 
                                                           boolean hasIncident = schedule.getIncidentStatus() != null && !"NORMAL".equals(schedule.getIncidentStatus()) && !"DRIVER_SWITCHED".equals(schedule.getIncidentStatus());
                                                           if (hasIncident) {
                                                    %>
                                                        <span class="badge bg-danger"><i class="bi bi-tools"></i> Chờ đổi xe</span>
                                                    <%     } else { %>
                                                    <form action="monitor-action" method="POST" class="d-inline">
                                                        <input type="hidden" name="action" value="notify_parent">
                                                        <input type="hidden" name="hocSinhTK" value="<%= hs.getTenTK() %>">
                                                        <input type="hidden" name="stopName" value="<%= s.getStopName() %>">
                                                        <input type="hidden" name="direction" value="<%= schedule.getDirection() %>">
                                                        <input type="hidden" name="stopID" value="<%= s.getStopID() %>">
                                                        <button type="submit" class="btn btn-sm btn-outline-primary"><i class="bi bi-bell"></i> Gọi Phụ Huynh</button>
                                                    </form>
                                                    <form action="monitor-action" method="POST" class="d-inline ms-1">
                                                        <input type="hidden" name="action" value="mark_attendance">
                                                        <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                                                        <input type="hidden" name="stopID" value="<%= s.getStopID() %>">
                                                        <input type="hidden" name="maHocSinh" value="<%= hs.getMaHocSinh() %>">
                                                        <input type="hidden" name="direction" value="<%= schedule.getDirection() %>">
                                                        <button type="submit" name="isAbsent" value="false" class="btn btn-sm btn-outline-success"><i class="bi bi-person-check"></i> <%= buttonText %></button>
                                                    </form>
                                                    <%     }
                                                       } else { %>
                                                        <span class="badge bg-light text-dark border">Chưa đến lượt</span>
                                                    <% } %>
                                               <% } %>
                                           </div>
                                       </li>
                                   <% } %>
                                   </ul>
                                <% } else if (s.getStopID() != 0) { %>
                                   <span class="text-muted small ms-2">Không có học sinh đăng ký tại điểm này.</span>
                                <% } %>
                            </div>
                        </div>
                        <%  } 
                           } else { %>
                            <p class="text-muted">Chưa có dữ liệu điểm đón cho tuyến này.</p>
                        <% } %>
                        
                        <% 
                            boolean allStopsReached = true;
                            if (stops == null || stops.isEmpty() || reachedStops == null) {
                                allStopsReached = false;
                            } else {
                                for (Stop st : stops) {
                                    if (!reachedStops.contains(st.getStopID())) {
                                        allStopsReached = false;
                                        break;
                                    }
                                }
                            }
                            if (allStopsReached && reachedStops != null && !reachedStops.contains(-1)) { 
                        %>
                            <hr>
                            <div class="text-center mt-4 mb-2">
                                <h5 class="text-success fw-bold mb-3"><i class="bi bi-check-circle-fill me-2"></i>Đã qua tất cả các điểm</h5>
                                <%
                                    boolean hasIncident = schedule.getIncidentStatus() != null && !"NORMAL".equals(schedule.getIncidentStatus()) && !"DRIVER_SWITCHED".equals(schedule.getIncidentStatus());
                                    if (hasIncident) {
                                %>
                                    <button type="button" class="btn btn-secondary btn-lg px-5 fw-bold" disabled title="Xe đang báo hỏng, không thể hoàn tất"><i class="bi bi-tools me-2"></i> Chờ đổi xe</button>
                                <%  } else { %>
                                <form action="monitor-action" method="POST">
                                    <input type="hidden" name="action" value="complete_trip">
                                    <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                                    <input type="hidden" name="direction" value="<%= schedule.getDirection() %>">
                                    <button type="submit" class="btn btn-primary btn-lg px-5 fw-bold" onclick="return confirm('<%= "TO_HOME".equals(schedule.getDirection()) ? "Xác nhận tất cả học sinh xuống xe?" : "Xác nhận hoàn tất chuyến đi?" %>');">
                                        <i class="bi bi-flag-fill me-2"></i>Hoàn Tất Chuyến Đi
                                    </button>
                                </form>
                                <%  } %>
                            </div>
                        <% } else if (reachedStops != null && reachedStops.contains(-1)) { %>
                            <hr>
                            <div class="alert alert-success text-center mb-0">
                                <h5 class="fw-bold mb-0"><i class="bi bi-patch-check-fill me-2"></i>Bạn đã xác nhận hoàn thành nhiệm vụ!</h5>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </div>
        <% } else { %>
            <div class="alert alert-info">
                <h5 class="fw-bold"><i class="bi bi-info-circle me-2"></i>Không có chuyến xe nào</h5>
                <p class="mb-0">Bạn hiện không được phân công giám sát chuyến xe nào cho ngày hôm nay, hoặc các chuyến đã hoàn thành.</p>
            </div>
        <% } %>

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
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
