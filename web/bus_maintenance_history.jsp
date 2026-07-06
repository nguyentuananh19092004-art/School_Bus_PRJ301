<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử bảo dưỡng</title>
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
        .bus-info { background: rgba(15, 15, 15, 0.45); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.2); border-radius: 15px; padding: 20px; margin-bottom: 30px; box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
        .bus-info h5 { color: #ffc107; margin-bottom: 10px; font-weight: 600; }
        .bus-info p { margin: 5px 0; color: rgba(255,255,255,0.8); }
        .btn-back { background: linear-gradient(135deg, #ffc107, #ff9800); border: none; color: #000; padding: 10px 20px; border-radius: 10px; font-weight: 600; font-size: 0.95rem; transition: all 0.3s ease; text-decoration: none; display: inline-block; margin-bottom: 20px; box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4); }
        .btn-back:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 152, 0, 0.6); color: #000; text-decoration: none; }
        .table-card { background: rgba(15, 15, 15, 0.45); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.2); border-radius: 15px; overflow: hidden; box-shadow: 0 25px 50px rgba(0,0,0,0.5); }
        .table-card-header { padding: 20px; background: rgba(0, 0, 0, 0.3); border-bottom: 1px solid rgba(255, 255, 255, 0.1); }
        .table { margin-bottom: 0; color: #fff; }
        .table thead th { background: rgba(0, 0, 0, 0.2); color: #ffc107; border: 1px solid rgba(255, 255, 255, 0.1); font-weight: 600; text-transform: uppercase; font-size: 0.85rem; }
        .table tbody td { border: 1px solid rgba(255, 255, 255, 0.1); padding: 15px; vertical-align: middle; }
        .table tbody tr:hover { background: rgba(255, 193, 7, 0.1); }
        .empty-state { text-align: center; padding: 60px 20px; color: rgba(255,255,255,0.6); }
        .empty-state i { font-size: 3rem; margin-bottom: 20px; color: rgba(255, 193, 7, 0.5); }
        @media (max-width: 768px) { .top-header { padding: 0 20px; } .nav-links { display: none; } .main-content { padding: 15px 20px; } .page-title { font-size: 1.5rem; } .table { font-size: 0.9rem; } .table tbody td { padding: 10px; } }
    </style>
</head>
<body>
    <div class="bg-image"></div>
    <div class="bg-overlay"></div>
    <header class="top-header">
        <a href="index.jsp" class="navbar-brand"><i class="bi bi-bus-front-fill me-2"></i>SCHOOLBUS</a>
        <div class="nav-links"><a href="index.jsp">Trang Chủ</a> <a href="BusListServlet">Danh sách xe</a></div>
    </header>
    <div class="main-content">
        <h1 class="page-title"><i class="bi bi-clock-history me-2"></i>Lịch sử bảo dưỡng</h1>
        <a href="BusListServlet" class="btn-back"><i class="bi bi-arrow-left me-2"></i>Quay lại danh sách xe</a>
        <div class="bus-info">
            <h5><i class="bi bi-info-circle me-2"></i>Thông tin xe</h5>
            <p><strong>Biển số:</strong> ${bus.licensePlate}</p>
            <p><strong>Sức chứa:</strong> ${bus.capacity} chỗ</p>
            <p><strong>Trạng thái:</strong> ${bus.status}</p>
        </div>
        <div class="table-card">
            <div class="table-card-header"><h5 class="mb-0"><i class="bi bi-tools me-2"></i>Các lần bảo dưỡng</h5></div>
            <c:choose>
                <c:when test="${empty maintenances}">
                    <div class="empty-state"><i class="bi bi-inbox"></i><p>Không có lịch sử bảo dưỡng nào</p></div>
                </c:when>
                <c:otherwise>
                    <div style="overflow-x: auto;">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Ngày bảo dưỡng</th>
                                    <th>Nội dung</th>
                                    <th>Ngày tạo</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="m" items="${maintenances}">
                                    <tr>
                                        <td><i class="bi bi-calendar3 me-2"></i>${m.maintenanceDate}</td>
                                        <td>${m.description}</td>
                                        <td><small class="text-muted">${m.createdAt}</small></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
