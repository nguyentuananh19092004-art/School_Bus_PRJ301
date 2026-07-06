<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý tuyến đường</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        body, html { min-height: 100%; margin: 0; font-family: 'Inter', sans-serif; background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%); color: #fff; }
        .bg-image { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: url('img/urban_bus_bg.png') no-repeat center center; background-size: cover; z-index: -2; opacity: 0.3; }
        .bg-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.6); z-index: -1; }
        .top-header { position: fixed; top: 0; left: 0; width: 100%; height: 70px; display: flex; align-items: center; background: rgba(0, 0, 0, 0.5); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); z-index: 100; border-bottom: 1px solid rgba(255,255,255,0.1); padding: 0 40px; }
        .navbar-brand { font-weight: 800; font-size: 1.5rem; color: #fff !important; text-decoration: none; letter-spacing: 1px; display: flex; align-items: center; }
        .navbar-brand i { color: #ffc107; font-size: 1.8rem; }
        .nav-links { margin-left: auto; display: flex; gap: 25px; }
        .nav-links a { color: rgba(255,255,255,0.9); text-decoration: none; font-weight: 500; font-size: 1rem; transition: color 0.3s; }
        .nav-links a:hover { color: #ffc107; }
        .main-content { margin-top: 90px; padding: 20px 40px; min-height: calc(100vh - 90px); }
        .page-title { font-size: 2rem; font-weight: 700; margin-bottom: 30px; text-shadow: 0 2px 10px rgba(0,0,0,0.3); }
        .box { background: rgba(15, 15, 15, 0.45); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.2); border-radius: 15px; padding: 20px; margin-bottom: 20px; box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
        .box h5 { color: #ffc107; margin-bottom: 20px; font-weight: 600; }
        .column { margin-bottom: 20px; }
        .form-label { color: rgba(255,255,255,0.8); font-weight: 600; }
        .form-control, .form-select { background-color: rgba(0, 0, 0, 0.3) !important; border: 1px solid rgba(255, 255, 255, 0.2) !important; color: #fff !important; border-radius: 8px; }
        .form-control:focus, .form-select:focus { background-color: rgba(0, 0, 0, 0.5) !important; border-color: #ffc107 !important; box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25) !important; }
        .form-select option { background: #1a1f3a; color: #fff; }
        .btn-custom { background: linear-gradient(135deg, #ffc107, #ff9800); border: none; color: #000; padding: 8px 16px; border-radius: 8px; font-weight: 600; font-size: 0.9rem; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4); }
        .btn-custom:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 152, 0, 0.6); color: #000; }
        .table { color: #fff; }
        .table thead th { background: rgba(0, 0, 0, 0.3); color: #ffc107; border: 1px solid rgba(255, 255, 255, 0.1); }
        .table tbody td { border: 1px solid rgba(255, 255, 255, 0.1); }
        .table tbody tr:hover { background: rgba(255, 193, 7, 0.1); }
        .error-message { background: rgba(244, 67, 54, 0.2); border: 1px solid rgba(244, 67, 54, 0.5); color: #ff8a80; padding: 12px; border-radius: 10px; margin-bottom: 20px; }
        @media (max-width: 768px) { .top-header { padding: 0 20px; } .nav-links { display: none; } .main-content { padding: 15px 20px; } .page-title { font-size: 1.5rem; } }
    </style>
</head>
<body>
    <div class="bg-image"></div>
    <div class="bg-overlay"></div>
    <header class="top-header">
        <a href="index.jsp" class="navbar-brand"><i class="bi bi-bus-front-fill me-2"></i>SCHOOLBUS</a>
        <div class="nav-links"><a href="index.jsp">Trang Chủ</a> <a href="AdminDashboardServlet">Dashboard</a></div>
    </header>
    <div class="main-content">
        <h1 class="page-title"><i class="bi bi-diagram-3 me-2"></i>Quản lý tuyến đường</h1>
        <c:if test="${not empty error}">
            <div class="error-message"><i class="bi bi-exclamation-circle-fill me-2"></i>${error}</div>
        </c:if>

        <div class="row">
            <div class="col-md-6">
                <div class="box">
                    <h5><i class="bi bi-list-task me-2"></i>Danh sách tuyến</h5>
                    <div style="overflow-x: auto;">
                        <table class="table table-sm">
                            <thead>
                                <tr><th>Code</th><th>Tên</th><th>Hành động</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="route" items="${routes}">
                                    <tr>
                                        <td><strong>${route.routeCode}</strong></td>
                                        <td>${route.routeName}</td>
                                        <td><a href="RouteManagementServlet?routeId=${route.routeID}" class="btn-custom btn-sm"><i class="bi bi-eye"></i></a></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="box">
                    <h5><i class="bi bi-plus-circle me-2"></i>Thêm / sửa tuyến</h5>
                    <form method="post" action="RouteManagementServlet">
                        <input type="hidden" name="action" value="${empty selectedRoute ? 'create' : 'update'}" />
                        <c:if test="${not empty selectedRoute}">
                            <input type="hidden" name="routeID" value="${selectedRoute.routeID}" />
                        </c:if>
                        <div class="mb-3">
                            <label for="routeCode" class="form-label">Route Code</label>
                            <input type="text" class="form-control" id="routeCode" name="routeCode" value="${selectedRoute.routeCode}" required />
                        </div>
                        <div class="mb-3">
                            <label for="routeName" class="form-label">Tên tuyến</label>
                            <input type="text" class="form-control" id="routeName" name="routeName" value="${selectedRoute.routeName}" required />
                        </div>
                        <div class="mb-3">
                            <label for="description" class="form-label">Mô tả</label>
                            <input type="text" class="form-control" id="description" name="description" value="${selectedRoute.description}" />
                        </div>
                        <button type="submit" class="btn-custom w-100"><i class="bi bi-check-circle me-2"></i>Lưu tuyến</button>
                    </form>
                </div>
            </div>

            <div class="col-md-6">
                <div class="box">
                    <h5><i class="bi bi-geo-alt me-2"></i>Điểm dừng của tuyến</h5>
                    <c:if test="${not empty selectedRoute}">
                        <form method="post" action="RouteStopUpdateServlet">
                            <input type="hidden" name="routeID" value="${selectedRoute.routeID}" />
                            <div style="overflow-x: auto;">
                                <table class="table table-sm">
                                    <thead>
                                        <tr><th>STT</th><th>Điểm dừng</th><th>Giờ đi</th><th>Giờ về</th></tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="item" items="${routeStops}" varStatus="status">
                                            <tr>
                                                <td><input type="number" class="form-control form-control-sm" name="stopOrder" value="${item.stopOrder}" /></td>
                                                <td>
                                                    <input type="hidden" name="stopID" value="${item.stopID}" />
                                                    ${item.stop.stopName}
                                                </td>
                                                <td><input type="time" class="form-control form-control-sm" name="estimatedTime" value="${item.estimatedTime}" /></td>
                                                <td><input type="time" class="form-control form-control-sm" name="returnTime" value="${item.returnTime}" /></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                            <button type="submit" class="btn-custom w-100"><i class="bi bi-clock-history me-2"></i>Cập nhật giờ</button>
                        </form>
                    </c:if>
                </div>

                <div class="box">
                    <h5><i class="bi bi-map me-2"></i>Thêm điểm đón mới</h5>
                    <form method="post" action="StopAddServlet">
                        <div class="mb-3">
                            <label for="stopName" class="form-label">Tên</label>
                            <input type="text" class="form-control" id="stopName" name="stopName" required />
                        </div>
                        <div class="mb-3">
                            <label for="address" class="form-label">Địa chỉ</label>
                            <input type="text" class="form-control" id="address" name="address" />
                        </div>
                        <div class="mb-3">
                            <label for="latitude" class="form-label">GPS Latitude</label>
                            <input type="text" class="form-control" id="latitude" name="latitude" placeholder="10.7234" />
                        </div>
                        <div class="mb-3">
                            <label for="longitude" class="form-label">GPS Longitude</label>
                            <input type="text" class="form-control" id="longitude" name="longitude" placeholder="106.6956" />
                        </div>
                        <button type="submit" class="btn-custom w-100"><i class="bi bi-plus-circle me-2"></i>Thêm điểm đón</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
