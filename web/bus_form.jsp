<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Form xe bus</title>
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
        .main-content { display: flex; flex-direction: column; justify-content: center; align-items: center; min-height: 100vh; padding-top: 100px; padding-bottom: 40px; }
        .form-card { background: rgba(15, 15, 15, 0.45); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border: 1px solid rgba(255, 255, 255, 0.2); border-radius: 20px; box-shadow: 0 30px 60px rgba(0,0,0,0.5); padding: 40px; width: 100%; max-width: 480px; }
        .form-title { font-size: 1.8rem; font-weight: 700; margin-bottom: 10px; }
        .form-subtitle { color: rgba(255,255,255,0.6); margin-bottom: 30px; font-size: 0.95rem; }
        .form-floating > .form-control { background-color: rgba(0, 0, 0, 0.3) !important; border: 1px solid rgba(255, 255, 255, 0.2) !important; color: #fff !important; border-radius: 12px; }
        .form-floating > .form-control:focus { background-color: rgba(0, 0, 0, 0.5) !important; border-color: #ffc107 !important; box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25) !important; }
        .form-floating > label { color: rgba(255, 255, 255, 0.6); }
        .form-floating > .form-control:focus ~ label, .form-floating > .form-control:not(:placeholder-shown) ~ label { color: #ffc107; }
        .form-select { background-color: rgba(0, 0, 0, 0.3) !important; border: 1px solid rgba(255, 255, 255, 0.2) !important; color: #fff !important; border-radius: 12px; }
        .form-select:focus { background-color: rgba(0, 0, 0, 0.5) !important; border-color: #ffc107 !important; box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25) !important; }
        .form-select option { background: #1a1f3a; color: #fff; }
        .form-label { color: rgba(255,255,255,0.8); }
        .btn-submit { background: linear-gradient(135deg, #ffc107, #ff9800); border: none; color: #000; padding: 12px; border-radius: 12px; font-weight: 700; font-size: 1.05rem; transition: all 0.3s ease; box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4); text-transform: uppercase; letter-spacing: 0.5px; margin-top: 20px; width: 100%; }
        .btn-submit:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 152, 0, 0.6); color: #000; }
        .btn-back { background: rgba(255, 255, 255, 0.2); border: 1px solid rgba(255, 255, 255, 0.3); color: #fff; padding: 10px; border-radius: 10px; font-weight: 600; transition: all 0.3s ease; text-decoration: none; display: inline-block; margin-left: 10px; }
        .btn-back:hover { background: rgba(255, 255, 255, 0.3); color: #ffc107; }
        .button-group { display: flex; gap: 10px; margin-top: 20px; }
        .button-group button { flex: 1; }
        .error-message { background: rgba(244, 67, 54, 0.2); border: 1px solid rgba(244, 67, 54, 0.5); color: #ff8a80; padding: 12px; border-radius: 10px; margin-bottom: 20px; display: flex; align-items: center; }
        .error-message i { margin-right: 10px; }
        input:-webkit-autofill, input:-webkit-autofill:hover, input:-webkit-autofill:focus, input:-webkit-autofill:active{ -webkit-box-shadow: 0 0 0 30px rgba(0, 0, 0, 0.5) inset !important; -webkit-text-fill-color: white !important; }
        @media (max-width: 768px) { .top-header { padding: 0 20px; } .nav-links { display: none; } .main-content { padding-top: 80px; padding-bottom: 20px; } .form-card { padding: 30px 25px; margin: 0 15px; } }
    </style>
</head>
<body>
    <div class="bg-image"></div>
    <div class="bg-overlay"></div>
    <header class="top-header">
        <a href="index.jsp" class="navbar-brand"><i class="bi bi-bus-front-fill me-2"></i>SCHOOLBUS</a>
        <div class="nav-links"><a href="index.jsp">Trang Chủ</a> <a href="BusListServlet">Quay lại</a></div>
    </header>
    <div class="main-content">
        <div class="form-card">
            <h3 class="form-title"><i class="bi bi-bus-front me-2"></i>${empty bus ? 'Thêm xe mới' : 'Cập nhật xe'}</h3>
            <p class="form-subtitle">${empty bus ? 'Nhập thông tin chi tiết của xe mới' : 'Cập nhật thông tin xe'}</p>
            <c:if test="${not empty error}">
                <div class="error-message"><i class="bi bi-exclamation-circle-fill"></i>${error}</div>
            </c:if>
            <form method="post" action="${empty bus ? 'BusCreateServlet' : 'BusUpdateServlet'}">
                <c:if test="${not empty bus}"><input type="hidden" name="busID" value="${bus.busID}" /></c:if>
                <div class="form-floating mb-3">
                    <input type="text" class="form-control" id="licensePlate" name="licensePlate" placeholder="Biển số" value="${bus.licensePlate}" required>
                    <label for="licensePlate">Biển số xe</label>
                </div>
                <div class="form-floating mb-3">
                    <input type="number" class="form-control" id="capacity" name="capacity" placeholder="Sức chứa" value="${bus.capacity}" required>
                    <label for="capacity">Sức chứa (7 hoặc 9)</label>
                </div>
                <div class="mb-3">
                    <label for="status" class="form-label">Trạng thái</label>
                    <select class="form-select" id="status" name="status" required>
                        <option value="">-- Chọn trạng thái --</option>
                        <option value="Sẵn sàng" ${bus.status == 'Sẵn sàng' ? 'selected' : ''}>Sẵn sàng</option>
                        <option value="Hoạt động" ${bus.status == 'Hoạt động' ? 'selected' : ''}>Hoạt động</option>
                        <option value="Bảo dưỡng/Sửa chữa" ${bus.status == 'Bảo dưỡng/Sửa chữa' ? 'selected' : ''}>Bảo dưỡng/Sửa chữa</option>
                    </select>
                </div>
                <div class="button-group">
                    <button type="submit" class="btn btn-submit"><i class="bi bi-check-circle me-2"></i>Lưu</button>
                    <a href="BusListServlet" class="btn-back"><i class="bi bi-arrow-left me-1"></i>Quay lại</a>
                </div>
            </form>
        </div>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
