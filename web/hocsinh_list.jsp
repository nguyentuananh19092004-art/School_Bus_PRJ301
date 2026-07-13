<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Quản Lý Học Sinh</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="container mt-4">
        <h2 class="mb-4">Danh Sách Học Sinh Hệ Thống</h2>
        <div class="d-flex justify-content-between align-items-center mb-3">
            <a href="hocsinh-add" class="btn btn-primary">Thêm học sinh</a>
            <a href="AdminDashboardServlet" class="btn btn-outline-secondary">Về Dashboard</a>
        </div>
        <c:if test="${param.msg == 'stopped'}">
            <div class="alert alert-warning alert-dismissible fade show">
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                <strong>Thông báo:</strong> Dịch vụ ngưng hoạt động từ ngày mai. Trạng thái học sinh đã chuyển về "Ngưng hoạt động".
            </div>
        </c:if>
        <table class="table table-bordered table-striped">
            <thead class="table-dark">
                <tr>
                    <th>Mã HS</th>
                    <th>Tên Học Sinh</th>
                    <th>Lớp</th>
                    <th>Tài Khoản PH</th>
                    <th>Tuyến Điểm Mặc Định</th>
                    <th>Trạng Thái</th>
                    <th>Hành động</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${listHS}" var="hs">
                    <tr>
                        <td>${hs.maHocSinh}</td>
                        <td>${hs.tenHocSinh}</td>
                        <td>Lớp ${hs.lop}</td>
                        <td>${hs.tenTK}</td>
                        <td>
                            <c:choose>
                                <c:when test="${hs.defaultRouteID != null && hs.defaultRouteID != 0}">
                                    <span class="badge bg-info text-dark">${routeMap[hs.defaultRouteID]}</span>
                                </c:when>
                                <c:when test="${hs.pendingRouteID != null && hs.pendingRouteID != 0}">
                                    <span class="badge bg-warning text-dark"><i class="bi bi-clock-history"></i> Chờ duyệt: ${routeMap[hs.pendingRouteID]}</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">Chưa thiết lập</span>
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <span class="badge ${hs.trangThai == 'Sử dụng' ? 'bg-success' : 'bg-danger'}">
                                ${hs.trangThai}
                            </span>
                        </td>
                        <td>
                            <a href="hocsinh-edit?id=${hs.maHocSinh}" class="btn btn-sm btn-warning">Sửa</a>
                            <a href="hocsinh-delete?id=${hs.maHocSinh}" class="btn btn-sm btn-danger" onclick="return confirm('Bạn có chắc chắn muốn xóa học sinh này?');">Xóa</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </body>
</html>