<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý xe bus</title>
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
        .btn-primary-custom { background: linear-gradient(135deg, #ffc107, #ff9800); border: none; color: #000; padding: 10px 20px; border-radius: 10px; font-weight: 600; font-size: 0.95rem; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4); text-decoration: none; display: inline-block; margin-bottom: 20px; }
        .btn-primary-custom:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 152, 0, 0.6); color: #000; text-decoration: none; }
        .btn-sm-custom { background: #ff9800; border: none; color: #000; padding: 6px 12px; border-radius: 6px; font-weight: 600; font-size: 0.85rem; transition: all 0.3s ease; text-decoration: none; }
        .btn-sm-custom:hover { background: #ffc107; transform: translateY(-1px); }
        .btn-danger-custom { background: #d32f2f; border: none; color: #fff; padding: 6px 12px; border-radius: 6px; font-weight: 600; font-size: 0.85rem; transition: all 0.3s ease; text-decoration: none; }
        .btn-danger-custom:hover { background: #ff5252; transform: translateY(-1px); }
        .table-card { background: rgba(15, 15, 15, 0.45); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.2); border-radius: 15px; overflow: hidden; box-shadow: 0 25px 50px rgba(0,0,0,0.5); margin-bottom: 30px; }
        .table-card-header { padding: 20px; background: rgba(0, 0, 0, 0.3); border-bottom: 1px solid rgba(255, 255, 255, 0.1); }
        .table { margin-bottom: 0; color: #fff; }
        .table thead th { background: rgba(0, 0, 0, 0.2); color: #ffc107; border: 1px solid rgba(255, 255, 255, 0.1); font-weight: 600; text-transform: uppercase; font-size: 0.85rem; }
        .table tbody td { border: 1px solid rgba(255, 255, 255, 0.1); padding: 15px; vertical-align: middle; }
        .table tbody tr { transition: background-color 0.3s ease; }
        .table tbody tr:hover { background: rgba(255, 193, 7, 0.1); }
        .status-badge { display: inline-block; padding: 6px 12px; border-radius: 20px; font-weight: 600; font-size: 0.85rem; }
        .status-ready { background: rgba(76, 175, 80, 0.3); color: #4caf50; }
        .status-active { background: rgba(33, 150, 243, 0.3); color: #2196f3; }
        .status-maintenance { background: rgba(255, 152, 0, 0.3); color: #ff9800; }
        @media (max-width: 768px) { .top-header { padding: 0 20px; } .nav-links { display: none; } .main-content { padding: 15px 20px; } .page-title { font-size: 1.5rem; } .table { font-size: 0.9rem; } .table tbody td { padding: 10px; } }
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
        <h1 class="page-title"><i class="bi bi-bus-front me-2"></i>Danh sách xe bus</h1>
        <a href="BusCreateServlet" class="btn-primary-custom"><i class="bi bi-plus-circle me-2"></i>Thêm xe mới</a>
        <div class="table-card">
            <div class="table-card-header"><h5 class="mb-0"><i class="bi bi-list-check me-2"></i>Danh sách toàn bộ xe</h5></div>
            <div style="overflow-x: auto;">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Biển số</th>
                            <th>Sức chứa</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="bus" items="${buses}">
                            <tr>
                                <td><strong>${bus.licensePlate}</strong></td>
                                <td><i class="bi bi-people-fill"></i> ${bus.capacity} chỗ</td>
                                <td><span class="status-badge ${bus.status == 'Sẵn sàng' ? 'status-ready' : bus.status == 'Hoạt động' ? 'status-active' : 'status-maintenance'}">${bus.status}</span></td>
                                <td>
                                    <a href="BusUpdateServlet?id=${bus.busID}" class="btn-sm-custom me-2"><i class="bi bi-pencil"></i> Sửa</a>
                                    <a href="BusDeleteServlet?id=${bus.busID}" class="btn-danger-custom me-2" onclick="return confirm('Xác nhận xóa/disable xe này?');"><i class="bi bi-trash"></i> Xóa</a>
                                    <a href="BusMaintenanceHistoryServlet?id=${bus.busID}" class="btn-sm-custom"><i class="bi bi-clock-history"></i> Lịch sử</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
