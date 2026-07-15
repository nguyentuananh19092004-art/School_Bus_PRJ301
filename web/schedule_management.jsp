<%@page import="model.Schedule"%>
<%@page import="model.Route"%>
<%@page import="model.Bus"%>
<%@page import="model.User"%>
<%@page import="java.util.List"%>
<%-- 
    Trang Quản lý Lịch trình (Phân ca).
    Admin sử dụng trang này để phân bổ xe bus, tài xế và giám thị cho từng tuyến đường theo từng ngày.
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // 1. Chặn người dùng không có quyền truy cập, đá văng về màn hình đăng nhập
    if(session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    
    // 2. Lấy dữ liệu từ Servlet truyền xuống để hiển thị
    List<User> drivers = (List<User>) request.getAttribute("drivers");
    List<User> monitors = (List<User>) request.getAttribute("monitors");
    List<Bus> buses = (List<Bus>) request.getAttribute("buses");
    List<Route> routes = (List<Route>) request.getAttribute("routes");
    List<Schedule> schedules = (List<Schedule>) request.getAttribute("schedules");
    List<User> technicians = (List<User>) request.getAttribute("technicians");
    List<model.TechnicianSchedule> techSchedules = (List<model.TechnicianSchedule>) request.getAttribute("techSchedules");
    
    List<Bus> busesToSchool = (List<Bus>) request.getAttribute("busesToSchool");
    List<User> driversToSchool = (List<User>) request.getAttribute("driversToSchool");
    List<User> monitorsToSchool = (List<User>) request.getAttribute("monitorsToSchool");
    
    List<Bus> busesToHome = (List<Bus>) request.getAttribute("busesToHome");
    List<User> driversToHome = (List<User>) request.getAttribute("driversToHome");
    List<User> monitorsToHome = (List<User>) request.getAttribute("monitorsToHome");
    
    List<User> availableTechnicians = (List<User>) request.getAttribute("availableTechnicians");
    List<Bus> allBuses = (List<Bus>) request.getAttribute("allBuses");
    List<Bus> allHistoricalBuses = (List<Bus>) request.getAttribute("allHistoricalBuses");
    List<User> allDrivers = (List<User>) request.getAttribute("allDrivers");
    List<User> allMonitors = (List<User>) request.getAttribute("allMonitors");
%>
<%!
    String getBusPlate(List<Bus> allBuses, int id) {
        if (allBuses != null) {
            for (Bus b : allBuses) {
                if (b.getBusID() == id) {
                    if ("Bảo dưỡng/Sửa chữa".equals(b.getStatus())) {
                        return b.getLicensePlate() + " <i class='bi bi-wrench text-danger' title='Đang bảo dưỡng/sửa chữa'></i>";
                    }
                    return b.getLicensePlate();
                }
            }
        }
        return "Unknown";
    }
    String getUserName(List<User> allUsers, int id) {
        if (allUsers != null) {
            for (User u : allUsers) {
                if (u.getUserID() == id) return u.getFullName();
            }
        }
        return "Unknown";
    }
    String getRouteName(List<Route> routes, int id) {
        if (routes != null) {
            for (Route r : routes) {
                if (r.getRouteID() == id) return r.getRouteName();
            }
        }
        return "Unknown";
    }
    String checkSelected(String errDir, String targetDir, String paramVal, int currentVal) {
        if (errDir != null && errDir.equals(targetDir) && paramVal != null && paramVal.equals(String.valueOf(currentVal))) {
            return "selected";
        }
        return "";
    }
    String checkDefault(String errDir, String targetDir, String paramVal) {
        if (errDir != null && errDir.equals(targetDir) && paramVal != null && !paramVal.isEmpty()) {
            return "";
        }
        return "selected";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Phân ca & Lịch trình - Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">

<nav class="navbar navbar-dark bg-dark mb-4">
    <div class="container">
        <a class="navbar-brand" href="AdminDashboardServlet"><i class="bi bi-arrow-left"></i> Về Dashboard</a>
        <span class="navbar-text text-white">Quản lý Phân Ca</span>
    </div>
</nav>

<div class="container">
    <%-- 3. Xử lý hiển thị thông báo trả về từ Servlet (dựa trên URL parameter "msg") --%>
    <% if("success".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-success alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Thêm phân ca thành công!</div>
    <% } else if("bus_changed".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-success alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Đổi xe thành công!</div>
    <% } else if("deleted".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-success alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Xóa phân ca thành công!</div>
    <% } else if("conflict".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-warning alert-dismissible fade show">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> <strong>Cảnh báo:</strong> Xe bus, Tài xế, hoặc Giám thị này đã được phân công vào ca này rồi! Bạn không thể phân công trùng lặp.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    <% } else if("error".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Có lỗi xảy ra!</div>
    <% } else if("tech_success".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-success alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Phân ca kỹ thuật thành công!</div>
    <% } else if("tech_deleted".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-success alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Xóa ca kỹ thuật thành công!</div>
    <% } else if("tech_conflict".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-warning alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Lỗi: Kỹ thuật viên này đã được phân công vào ngày này rồi!</div>
    <% } else if("tech_error".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button>Có lỗi xảy ra khi xử lý ca kỹ thuật!</div>
    <% } else if("past_date".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Không thể phân ca cho ngày trong quá khứ!</div>
    <% } else if("timeout_school".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Đã quá 6h sáng, không thể phân ca chiều đi (Đến trường) cho ngày hôm nay nữa!</div>
    <% } else if("timeout_home".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Đã quá 16h (4h chiều), không thể phân ca chiều về (Về nhà) cho ngày hôm nay nữa!</div>
    <% } else if("tech_past_date".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Không thể phân ca kỹ thuật cho ngày hôm trước!</div>
    <% } else if("overcapacity".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Không thể phân thêm xe! Tuyến này đã đủ sức chứa cho số lượng học sinh hiện tại (không cần thêm xe).</div>
    <% } else if("no_students".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Không thể phân xe! Tuyến này hiện không có học sinh nào đăng ký hoạt động.</div>
    <% } else if("tech_on_leave".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Kỹ thuật viên này đã được duyệt nghỉ phép vào ngày được chọn!</div>
    <% } else if("driver_on_leave".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Tài xế này đã được duyệt nghỉ phép vào ngày được chọn!</div>
    <% } else if("monitor_on_leave".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-danger alert-dismissible fade show"><button type="button" class="btn-close" data-bs-dismiss="alert"></button><i class="bi bi-x-circle-fill me-2"></i><strong>Lỗi:</strong> Giám thị này đã được duyệt nghỉ phép vào ngày được chọn!</div>
    <% } else if("need_more_bus".equals(request.getParameter("msg"))) { %>
        <div class="alert alert-warning alert-dismissible fade show">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> <strong>Cảnh báo:</strong> Xe thay thế khẩn cấp bạn vừa chọn KHÔNG ĐỦ CHỖ. Hệ thống yêu cầu bạn <strong>phân thêm xe</strong> cho tuyến này!
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    <% } %>

    <% 
       // 4. Bóc tách các tham số liên quan đến lỗi form trước đó để restore dữ liệu form (nếu có)
       String errDir = request.getParameter("direction");
       String errRoute = request.getParameter("routeID");
       String errBus = request.getParameter("busID");
       String errDriver = request.getParameter("driverID");
       String errMonitor = request.getParameter("monitorID");
       String selectedDate = (String) request.getAttribute("selectedDate"); 
       
       boolean isPastDate = false;
       if (selectedDate != null && !selectedDate.isEmpty()) {
           try {
               isPastDate = java.time.LocalDate.parse(selectedDate).isBefore(java.time.LocalDate.now());
           } catch (Exception e) {}
       }
    %>
    <!-- Date Picker Form -->
    <div class="card shadow-sm mb-4 border-info">
        <div class="card-body bg-light d-flex align-items-center">
            <h5 class="mb-0 me-3 text-info"><i class="bi bi-calendar3 me-2"></i>Chọn ngày phân ca:</h5>
            <form action="ScheduleServlet" method="GET" class="d-flex m-0" id="dateForm">
                <input type="date" name="selectedDate" value="<%= selectedDate %>" class="form-control" required onchange="document.getElementById('dateForm').submit();" style="cursor: pointer;" onclick="this.showPicker()">
            </form>
        </div>
    </div>

    <% 
       if (!isPastDate) {
           java.util.List<String> capacityWarnings = (java.util.List<String>) request.getAttribute("capacityWarnings"); 
           if (capacityWarnings != null && !capacityWarnings.isEmpty()) { %>
        <div class="card shadow-sm border-danger mb-4">
            <div class="card-header bg-danger text-white fw-bold">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> Bảng Cảnh Báo & Đề Xuất Phân Xe Ngày <%= selectedDate %>
            </div>
            <div class="card-body bg-light">
                <ul class="mb-0 text-danger fw-bold">
                    <% for (String warning : capacityWarnings) { %>
                        <li><%= warning %></li>
                    <% } %>
                </ul>
            </div>
        </div>
    <%     } 
       } 
    %>

    <% if (!isPastDate) { %>
    <div class="row mb-4">
        <!-- 5. Form Phân ca Chiều Đi (Từ Nhà Đến Trường) -->
        <div class="col-md-6">
            <div class="card shadow-sm border-primary">
                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="bi bi-sun me-2"></i>Phân ca Chiều đi (Đến trường)</h5>
                    <span class="badge bg-light text-primary"><%= selectedDate %></span>
                </div>
                <div class="card-body">
                    <form action="ScheduleServlet" method="POST">
                        <input type="hidden" name="date" value="<%= selectedDate %>">
                        <input type="hidden" name="direction" value="TO_SCHOOL">
                        <div class="mb-3">
                            <label>Tuyến đường</label>
                            <select name="routeID" class="form-select" required>
                                <option value="" disabled <%= checkDefault(errDir, "TO_SCHOOL", errRoute) %>>-- Chọn tuyến đường --</option>
                                <% java.util.Map<Integer, Integer> routeStudentCounts = (java.util.Map<Integer, Integer>) request.getAttribute("routeStudentCounts"); %>
                                <% if(routes != null) for(Route r : routes) { 
                                    int c = routeStudentCounts != null && routeStudentCounts.containsKey(r.getRouteID()) ? routeStudentCounts.get(r.getRouteID()) : 0;
                                %>
                                    <option value="<%= r.getRouteID() %>" <%= checkSelected(errDir, "TO_SCHOOL", errRoute, r.getRouteID()) %>><%= r.getRouteName() %> (Đang có <%= c %> học sinh)</option>
                                <% } %>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label>Xe Bus</label>
                            <select name="busID" class="form-select" required>
                                <option value="" disabled <%= checkDefault(errDir, "TO_SCHOOL", errBus) %>>-- Chọn xe bus --</option>
                                <% if(busesToSchool != null) for(Bus b : busesToSchool) { %>
                                    <option value="<%= b.getBusID() %>" <%= checkSelected(errDir, "TO_SCHOOL", errBus, b.getBusID()) %>><%= b.getLicensePlate() %> (<%= b.getCapacity() %> chỗ)</option>
                                <% } %>
                            </select>
                        </div>
                        <div class="row mb-3">
                            <div class="col-6">
                                <label>Tài xế</label>
                                <select name="driverID" class="form-select" required>
                                    <option value="" disabled <%= checkDefault(errDir, "TO_SCHOOL", errDriver) %>>-- Chọn tài xế --</option>
                                    <% if(driversToSchool != null) for(User d : driversToSchool) { %>
                                        <option value="<%= d.getUserID() %>" <%= checkSelected(errDir, "TO_SCHOOL", errDriver, d.getUserID()) %>><%= d.getFullName() %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="col-6">
                                <label>Giám thị</label>
                                <select name="monitorID" class="form-select" required>
                                    <option value="" disabled <%= checkDefault(errDir, "TO_SCHOOL", errMonitor) %>>-- Chọn giám thị --</option>
                                    <% if(monitorsToSchool != null) for(User m : monitorsToSchool) { %>
                                        <option value="<%= m.getUserID() %>" <%= checkSelected(errDir, "TO_SCHOOL", errMonitor, m.getUserID()) %>><%= m.getFullName() %></option>
                                    <% } %>
                                </select>
                            </div>
                        </div>
                        <button type="submit" class="btn btn-primary w-100 fw-bold"><i class="bi bi-floppy me-2"></i>Lưu ca Đến trường</button>
                    </form>
                </div>
            </div>
        </div>

        <!-- 6. Form Phân ca Chiều Về (Từ Trường Về Nhà) -->
        <div class="col-md-6">
            <div class="card shadow-sm border-success">
                <div class="card-header bg-success text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="bi bi-moon-stars me-2"></i>Phân ca Chiều về (Về nhà)</h5>
                    <span class="badge bg-light text-success"><%= selectedDate %></span>
                </div>
                <div class="card-body">
                    <form action="ScheduleServlet" method="POST">
                        <input type="hidden" name="date" value="<%= selectedDate %>">
                        <input type="hidden" name="direction" value="TO_HOME">
                        <div class="mb-3">
                            <label>Tuyến đường</label>
                            <select name="routeID" class="form-select" required>
                                <option value="" disabled <%= checkDefault(errDir, "TO_HOME", errRoute) %>>-- Chọn tuyến đường --</option>
                                <% if(routes != null) for(Route r : routes) { 
                                    int c = routeStudentCounts != null && routeStudentCounts.containsKey(r.getRouteID()) ? routeStudentCounts.get(r.getRouteID()) : 0;
                                %>
                                    <option value="<%= r.getRouteID() %>" <%= checkSelected(errDir, "TO_HOME", errRoute, r.getRouteID()) %>><%= r.getRouteName() %> (Đang có <%= c %> học sinh)</option>
                                <% } %>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label>Xe Bus</label>
                            <select name="busID" class="form-select" required>
                                <option value="" disabled <%= checkDefault(errDir, "TO_HOME", errBus) %>>-- Chọn xe bus --</option>
                                <% if(busesToHome != null) for(Bus b : busesToHome) { %>
                                    <option value="<%= b.getBusID() %>" <%= checkSelected(errDir, "TO_HOME", errBus, b.getBusID()) %>><%= b.getLicensePlate() %> (<%= b.getCapacity() %> chỗ)</option>
                                <% } %>
                            </select>
                        </div>
                        <div class="row mb-3">
                            <div class="col-6">
                                <label>Tài xế</label>
                                <select name="driverID" class="form-select" required>
                                    <option value="" disabled <%= checkDefault(errDir, "TO_HOME", errDriver) %>>-- Chọn tài xế --</option>
                                    <% if(driversToHome != null) for(User d : driversToHome) { %>
                                        <option value="<%= d.getUserID() %>" <%= checkSelected(errDir, "TO_HOME", errDriver, d.getUserID()) %>><%= d.getFullName() %></option>
                                    <% } %>
                                </select>
                            </div>
                            <div class="col-6">
                                <label>Giám thị</label>
                                <select name="monitorID" class="form-select" required>
                                    <option value="" disabled <%= checkDefault(errDir, "TO_HOME", errMonitor) %>>-- Chọn giám thị --</option>
                                    <% if(monitorsToHome != null) for(User m : monitorsToHome) { %>
                                        <option value="<%= m.getUserID() %>" <%= checkSelected(errDir, "TO_HOME", errMonitor, m.getUserID()) %>><%= m.getFullName() %></option>
                                    <% } %>
                                </select>
                            </div>
                        </div>
                        <button type="submit" class="btn btn-success w-100 fw-bold"><i class="bi bi-floppy me-2"></i>Lưu ca Về nhà</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <% } else { %>
        <div class="alert alert-info">
            <i class="bi bi-clock-history me-2"></i> Đang xem dữ liệu lịch sử phân ca của ngày <strong><%= selectedDate %></strong>. Không thể thêm mới phân ca cho ngày đã qua.
        </div>
    <% } %>

    <div class="row">
        <!-- 7. Bảng danh sách các lịch trình phân ca đã chốt / đang chờ của ngày được chọn -->
        <div class="col-md-12">
            <div class="card shadow-sm">
                <div class="card-header bg-white">
                    <h5 class="mb-0">Danh sách Phân ca Ngày <%= selectedDate %></h5>
                </div>
                <div class="card-body p-0 table-responsive">
                    <table class="table table-hover table-striped mb-0 text-nowrap">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Ngày</th>
                                <th>Chiều</th>
                                <th>Tuyến</th>
                                <th>Xe Bus</th>
                                <th>Tài xế</th>
                                <th>Giám thị</th>
                                <th>Trạng thái</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if(schedules != null) { 
                                for(Schedule s : schedules) { %>
                                <tr>
                                    <td>#<%= s.getScheduleID() %></td>
                                    <td class="fw-bold text-primary"><%= s.getDate() %></td>
                                    <td><%= s.getDirection().equals("TO_SCHOOL") ? "Đến trường" : "Về nhà" %></td>
                                    <td>LT<%= s.getRouteID() %></td>
                                    <td><%= getBusPlate(allHistoricalBuses, s.getBusID()) %></td>
                                    <td><%= getUserName(allDrivers, s.getDriverID()) %></td>
                                    <td><%= getUserName(allMonitors, s.getMonitorID()) %></td>
                                    <td><span class="badge bg-warning"><%= s.getStatus() %></span></td>
                                    <td>
                                        <% if ("PENDING".equals(s.getStatus())) { %>
                                        <a href="ScheduleServlet?action=delete&id=<%= s.getScheduleID() %>&selectedDate=<%= selectedDate %>" class="btn btn-sm btn-danger" onclick="return confirm('Bạn có chắc chắn muốn xóa lịch phân ca này không?');">
                                            <i class="bi bi-trash"></i> Xóa
                                        </a>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-secondary" disabled title="Chỉ được xóa khi trạng thái là PENDING"><i class="bi bi-lock"></i> Đã chốt</button>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- Phân ca Kỹ thuật -->
    <hr class="my-5">
    <h4 class="mb-4 text-secondary"><i class="bi bi-tools me-2"></i>Phân ca Kỹ thuật viên (Xưởng bảo dưỡng)</h4>
    <div class="row">
        <% if (!isPastDate) { %>
        <!-- Form Kỹ thuật -->
        <div class="col-md-4">
            <div class="card shadow-sm border-warning">
                <div class="card-header bg-warning text-dark fw-bold d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">Thêm Ca Kỹ Thuật</h5>
                    <span class="badge bg-light text-warning"><%= selectedDate %></span>
                </div>
                <div class="card-body">
                    <form action="tech-schedule" method="POST">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="date" value="<%= selectedDate %>">
                        <div class="mb-4">
                            <label>Kỹ thuật viên</label>
                            <select name="technicianID" class="form-select" required>
                                <% if(availableTechnicians != null) for(User t : availableTechnicians) { %>
                                    <option value="<%= t.getUserID() %>"><%= t.getFullName() %></option>
                                <% } %>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-warning w-100 fw-bold"><i class="bi bi-plus-circle me-1"></i> Lưu ca Kỹ thuật</button>
                    </form>
                </div>
            </div>
        </div>
        <% } %>

        <!-- Danh sách ca kỹ thuật -->
        <div class="col-md-<%= isPastDate ? "12" : "8" %>">
            <div class="card shadow-sm">
                <div class="card-header bg-white">
                    <h5 class="mb-0">Danh sách Ca Kỹ thuật Ngày <%= selectedDate %></h5>
                </div>
                <div class="card-body p-0 table-responsive">
                    <table class="table table-hover table-striped mb-0 text-nowrap">
                        <thead class="table-light">
                            <tr>
                                <th>ID Ca</th>
                                <th>Ngày làm việc</th>
                                <th>Tên Kỹ thuật viên</th>
                                <th>Trạng thái</th>
                                <th>Ngày tạo</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if(techSchedules != null && !techSchedules.isEmpty()) { 
                                for(model.TechnicianSchedule ts : techSchedules) { 
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
                                <tr>
                                    <td>#<%= ts.getTechScheduleID() %></td>
                                    <td class="fw-bold text-primary"><%= ts.getDate() %></td>
                                    <td><%= ts.getTechnicianName() %></td>
                                    <td><span class="badge <%= statusBadge %>"><%= statusText %></span></td>
                                    <td class="text-muted"><%= new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts.getCreatedAt()) %></td>
                                    <td>
                                        <% if ("PENDING".equals(ts.getStatus())) { %>
                                        <form action="tech-schedule" method="POST" style="display:inline;">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="<%= ts.getTechScheduleID() %>">
                                            <input type="hidden" name="selectedDate" value="<%= selectedDate %>">
                                            <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('Bạn có chắc chắn muốn xóa ca kỹ thuật này?');">
                                                <i class="bi bi-trash"></i> Xóa
                                            </button>
                                        </form>
                                        <% } else { %>
                                            <button class="btn btn-sm btn-secondary" disabled title="Chỉ được xóa khi trạng thái là PENDING"><i class="bi bi-trash"></i> Xóa</button>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } } else { %>
                                <tr>
                                    <td colspan="5" class="text-center py-3 text-muted">Chưa có lịch phân ca kỹ thuật nào.</td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
