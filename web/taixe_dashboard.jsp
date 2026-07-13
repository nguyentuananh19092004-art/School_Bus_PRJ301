<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.*"%>
<%
    if(session.getAttribute("userRole") == null || !"taixe".equals(session.getAttribute("userRole"))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    Schedule schedule = (Schedule) request.getAttribute("activeSchedule");
    Bus bus = (Bus) request.getAttribute("bus");
    User monitor = (User) request.getAttribute("monitor");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tài Xế Dashboard - School Bus</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Inter', sans-serif; background-color: #f4f6f9; }
        .navbar { background: linear-gradient(135deg, #e74a3b 0%, #be2617 100%); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .dashboard-card { border: none; border-radius: 15px; box-shadow: 0 5px 20px rgba(0,0,0,0.05); transition: transform 0.3s ease; }
        .dashboard-card:hover { transform: translateY(-5px); }
    </style>
</head>
<body>

    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container">
            <a class="navbar-brand fw-bold" href="driver-dashboard">
                <i class="bi bi-car-front-fill me-2"></i>Tài Xế Panel
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Xin chào, <b><%= session.getAttribute("username") %></b></span>
                <a href="doi_mat_khau.jsp" class="btn btn-sm btn-outline-warning me-2"><i class="bi bi-key"></i> Đổi mật khẩu</a>
                <a href="dang_nhap.jsp" class="btn btn-sm btn-outline-light"><i class="bi bi-box-arrow-right"></i> Đăng xuất</a>
            </div>
        </div>
    </nav>

    <div class="container my-5">
        <% if (session.getAttribute("errorMessage") != null) { %>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i><%= session.getAttribute("errorMessage") %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <% 
            session.removeAttribute("errorMessage");
        } %>
        <h2 class="fw-bold mb-4">Lịch trình lái xe</h2>
        
        <% if (schedule != null) { %>
        <div class="row g-4">
            <div class="col-md-12">
                <div class="card dashboard-card border-start border-danger border-5">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h5 class="fw-bold text-danger">Tuyến ID: <%= schedule.getRouteID() %> (<%= schedule.getDirection().equals("TO_SCHOOL") ? "Đến trường" : "Về nhà" %>)</h5>
                                <p class="text-muted mb-1"><i class="bi bi-bus-front me-2"></i> Xe: <b><%= bus != null ? bus.getLicensePlate() : "" %></b></p>
                                <p class="text-muted mb-1"><i class="bi bi-person-badge me-2"></i> Giám sát: <b><%= monitor != null ? monitor.getFullName() : "Trống" %></b></p>
                                <p class="text-muted mb-0"><i class="bi bi-info-circle me-2"></i> Trạng thái chuyến: <span class="badge bg-secondary"><%= schedule.getStatus() %></span></p>
                            </div>
                            <div class="text-end">
                                <% if ("PENDING".equals(schedule.getStatus())) { 
                                    if (bus != null && "Bảo dưỡng/Sửa chữa".equals(bus.getStatus())) {
                                %>
                                    <button type="button" class="btn btn-secondary btn-lg mb-2" disabled title="Xe đã đưa về xưởng">
                                        <i class="bi bi-tools me-2"></i> Xe cần bảo dưỡng. Chờ Admin đổi xe...
                                    </button>
                                <%  } else { 
                                        boolean hasActiveIncident = schedule.getIncidentStatus() != null && !"NORMAL".equals(schedule.getIncidentStatus()) && !"DRIVER_SWITCHED".equals(schedule.getIncidentStatus());
                                        if (hasActiveIncident) {
                                %>
                                    <button type="button" class="btn btn-secondary btn-lg mb-2" disabled title="Đang chờ đổi xe do sự cố"><i class="bi bi-car-front-fill me-2"></i> Hoàn tất kiểm tra xe & Di chuyển (Chờ đổi xe)</button>
                                <%      } else { %>
                                <form action="driver-action" method="POST" class="d-inline">
                                    <input type="hidden" name="action" value="start_moving">
                                    <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                                    <input type="hidden" name="busID" value="<%= schedule.getBusID() %>">
                                    <button type="submit" class="btn btn-primary btn-lg mb-2"><i class="bi bi-car-front-fill me-2"></i> Hoàn tất kiểm tra xe & Di chuyển</button>
                                </form>
                                <%      } 
                                    } 
                                   } else if ("PREPARING".equals(schedule.getStatus())) { 
                                    if (bus != null && "Bảo dưỡng/Sửa chữa".equals(bus.getStatus())) {
                                %>
                                    <button type="button" class="btn btn-secondary btn-lg mb-2" disabled title="Xe đã đưa về xưởng">
                                        <i class="bi bi-tools me-2"></i> Xe đang bảo dưỡng. Chờ Kỹ thuật đổi xe...
                                    </button>
                                <%  } else {
                                    Stop firstStop = (Stop) request.getAttribute("firstStop");
                                    boolean canStart = true;
                                    String timeStr = null;
                                    if (firstStop != null) {
                                        java.sql.Time stopTime = schedule.getDirection().equals("TO_SCHOOL") ? firstStop.getEstimatedTime() : firstStop.getReturnTime();
                                          if (schedule.getDirection().equals("Về nhà") || schedule.getDirection().equals("TO_HOME")) {
                                              timeStr = "16:30:00";
                                              try {
                                                  java.time.LocalTime firstTime = java.time.LocalTime.of(16, 30);
                                                  if (java.time.LocalTime.now().isBefore(firstTime)) {
                                                      canStart = false;
                                                  }
                                              } catch (Exception e) {}
                                          } else if (stopTime != null) {
                                              timeStr = stopTime.toString();
                                              try {
                                                  java.time.LocalTime firstTime = stopTime.toLocalTime();
                                                  if (java.time.LocalTime.now().isBefore(firstTime)) {
                                                      canStart = false;
                                                  }
                                              } catch (Exception e) {}
                                          }
                                    }
                                %>
                                    <% if (canStart) { 
                                           boolean hasActiveIncident = schedule.getIncidentStatus() != null && !"NORMAL".equals(schedule.getIncidentStatus()) && !"DRIVER_SWITCHED".equals(schedule.getIncidentStatus());
                                           if (hasActiveIncident) {
                                    %>
                                        <button type="button" class="btn btn-secondary btn-lg mb-2" disabled title="Đang chờ đổi xe do sự cố"><i class="bi bi-play-circle-fill me-2"></i> Bắt đầu vào lộ trình (Chờ đổi xe)</button>
                                    <%     } else { %>
                                    <form action="driver-action" method="POST" class="d-inline">
                                        <input type="hidden" name="action" value="start_trip">
                                        <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                                        <input type="hidden" name="busID" value="<%= schedule.getBusID() %>">
                                        <button type="submit" class="btn btn-danger btn-lg mb-2"><i class="bi bi-play-circle-fill me-2"></i> Bắt đầu vào lộ trình</button>
                                    </form>
                                    <%     } 
                                       } else { %>
                                        <button type="button" class="btn btn-secondary btn-lg mb-2" disabled title="Chưa đến giờ đón tại điểm đầu tiên"><i class="bi bi-clock-history me-2"></i> Chờ đến giờ (<%= timeStr %>)</button>
                                    <% } %>
                                  <% } 
                                   } else if ("IN_PROGRESS".equals(schedule.getStatus())) { 
                                      java.util.List<Integer> reachedStops = (java.util.List<Integer>) request.getAttribute("reachedStops");
                                      boolean monitorConfirmed = reachedStops != null && reachedStops.contains(-1);
                                      boolean hasIncident = schedule.getIncidentStatus() != null && !"NORMAL".equals(schedule.getIncidentStatus()) && !"DRIVER_SWITCHED".equals(schedule.getIncidentStatus());
                                  %>
                                      <span class="badge bg-success fs-5 mb-2 me-2"><i class="bi bi-truck"></i> Đang chạy</span>
                                      
                                      <% if (hasIncident) { %>
                                          <button type="button" class="btn btn-secondary btn-lg mb-2" disabled title="Bạn cần nhận xe dự phòng trước khi kết thúc chuyến">
                                              <i class="bi bi-exclamation-triangle-fill me-2"></i>Kết thúc chuyến đi
                                          </button>
                                       <% } else if (!monitorConfirmed) { %>
                                           <button type="button" class="btn btn-secondary btn-lg mb-2" disabled title="Đang chờ Giám thị hoàn tất chuyến đi">
                                               <i class="bi bi-hourglass-split me-2"></i>Kết thúc chuyến đi
                                           </button>
                                      <% } else { %>
                                          <button type="button" class="btn btn-success btn-lg mb-2" data-bs-toggle="modal" data-bs-target="#completeTripModal">
                                              <i class="bi bi-check-circle-fill me-2"></i>Kết thúc chuyến đi
                                          </button>
                                      <% } %>
                                  <% } %>
                                <% if ("NORMAL".equals(schedule.getIncidentStatus()) || "DRIVER_SWITCHED".equals(schedule.getIncidentStatus()) || schedule.getIncidentStatus() == null) { %>
                                <form action="driver-action" method="POST" class="mt-2">
                                    <input type="hidden" name="action" value="report_incident">
                                    <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                                    <input type="hidden" name="busID" value="<%= schedule.getBusID() %>">
                                    <button type="submit" class="btn btn-outline-warning" onclick="return confirm('Xác nhận báo hỏng xe để yêu cầu kỹ thuật viên hỗ trợ?');"><i class="bi bi-exclamation-triangle"></i> Báo hỏng xe</button>
                                </form>
                                <% } else { 
                                    String iStatus = schedule.getIncidentStatus();
                                    if ("INCIDENT".equals(iStatus)) {
                                %>
                                    <div class="mt-2 text-danger fw-bold"><i class="bi bi-exclamation-triangle-fill"></i> Đang chờ kỹ thuật đến...</div>
                                <%  } else if ("DISPATCHED".equals(iStatus)) { %>
                                    <div class="mt-2 text-warning fw-bold"><i class="bi bi-truck"></i> Kỹ thuật đang mang xe dự phòng đến...</div>
                                <%  } else if ("ARRIVED".equals(iStatus)) { %>
                                    <div class="mt-2 text-primary fw-bold"><i class="bi bi-geo-alt-fill"></i> Kỹ thuật đã đến nơi, chuẩn bị bàn giao...</div>
                                <%  } else if ("HANDED_OVER".equals(iStatus) || "TECH_RESOLVED".equals(iStatus)) { %>
                                    <form action="driver-action" method="POST" class="mt-2">
                                        <input type="hidden" name="action" value="switch_bus">
                                        <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                                        <input type="hidden" name="newBusID" value="<%= schedule.getReplacementBusID() %>">
                                        <input type="hidden" name="oldBusID" value="<%= schedule.getBusID() %>">
                                        <button type="submit" class="btn btn-success fw-bold pulse-button" onclick="return confirm('Xác nhận đã nhận xe dự phòng và tiếp tục hành trình?');"><i class="bi bi-arrow-repeat"></i> Đổi xe và Tiếp tục hành trình</button>
                                    </form>
                                <%  } 
                                   } %>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <% } else { %>
            <div class="alert alert-info">
                <h5 class="fw-bold"><i class="bi bi-info-circle me-2"></i>Không có chuyến xe nào</h5>
                <p class="mb-0">Bạn hiện không được phân công lái chuyến xe nào cho ngày hôm nay, hoặc chuyến đã hoàn thành.</p>
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
    
    <% if (schedule != null && "IN_PROGRESS".equals(schedule.getStatus())) { %>
    <!-- Modal Kết thúc chuyến đi -->
    <div class="modal fade" id="completeTripModal" tabindex="-1" aria-labelledby="completeTripModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="driver-action" method="POST">
                    <div class="modal-header bg-success text-white">
                        <h5 class="modal-title fw-bold" id="completeTripModalLabel"><i class="bi bi-check-circle-fill me-2"></i>Xác nhận kết thúc chuyến</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p>Vui lòng đánh giá tình trạng xe <b><%= bus != null ? bus.getLicensePlate() : "" %></b> sau chuyến đi:</p>
                        <input type="hidden" name="action" value="complete_trip">
                        <input type="hidden" name="scheduleID" value="<%= schedule.getScheduleID() %>">
                        <input type="hidden" name="busID" value="<%= schedule.getBusID() %>">
                        
                        <div class="form-check mb-3">
                            <input class="form-check-input" type="radio" name="busCondition" id="conditionOK" value="OK" checked>
                            <label class="form-check-label text-success fw-bold" for="conditionOK">
                                <i class="bi bi-check-circle me-1"></i> Xe bình thường (Sẵn sàng chạy tiếp)
                            </label>
                        </div>
                        <div class="form-check mb-3">
                            <input class="form-check-input" type="radio" name="busCondition" id="conditionBad" value="MAINTENANCE">
                            <label class="form-check-label text-danger fw-bold" for="conditionBad">
                                <i class="bi bi-tools me-1"></i> Xe có vấn đề (Cần bảo dưỡng/Sửa chữa)
                            </label>
                        </div>
                        
                        <div class="mb-3" id="noteContainer" style="display: none;">
                            <label class="form-label text-muted small">Ghi chú thêm (Bắt buộc):</label>
                            <textarea name="note" id="noteInput" class="form-control" rows="2" placeholder="Ví dụ: Xe kêu to, điều hòa yếu..."></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-success fw-bold">Xác nhận hoàn tất</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <% } %>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            var conditionOK = document.getElementById('conditionOK');
            var conditionBad = document.getElementById('conditionBad');
            var noteContainer = document.getElementById('noteContainer');
            var noteInput = document.getElementById('noteInput');

            function toggleNote() {
                if (conditionBad && conditionBad.checked) {
                    noteContainer.style.display = 'block';
                    noteInput.setAttribute('required', 'required');
                } else {
                    noteContainer.style.display = 'none';
                    noteInput.removeAttribute('required');
                }
            }

            if (conditionOK && conditionBad) {
                conditionOK.addEventListener('change', toggleNote);
                conditionBad.addEventListener('change', toggleNote);
            }
        });
    </script>
</body>
</html>
