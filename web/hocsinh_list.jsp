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
        <table class="table table-bordered table-striped">
            <thead class="table-dark">
                <tr>
                    <th>Mã HS</th>
                    <th>Tên Học Sinh</th>
                    <th>Lớp</th>
                    <th>Tài Khoản PH</th>
                    <th>Tuyến Điểm Mặc Định</th>
                    <th>Trạng Thái</th>
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
                                <c:when test="${hs.defaultStopID == 0}">
                                    <span class="text-muted">Chưa thiết lập</span>
                                </c:when>
                                <c:otherwise>
                                    Tuyến: ${hs.defaultRouteID} - Trạm: ${hs.defaultStopID}
                                </c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <span class="badge ${hs.trangThai == 'Hoạt động' ? 'bg-success' : 'bg-danger'}">
                                ${hs.trangThai}
                            </span>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </body>
</html>