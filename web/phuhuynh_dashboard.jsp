<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Cổng Thông Tin Phụ Huynh</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container mt-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Xin chào Phụ huynh, đây là danh sách con em của bạn</h2>
            <a href="logout" class="btn btn-outline-danger btn-sm">Đăng xuất</a>
        </div>

        <c:forEach items="${myChildren}" var="child">
            <div class="card mb-4 shadow-sm">
                <div class="card-header bg-primary text-white d-flex justify-content-between">
                    <h5 class="mb-0">Học sinh: ${child.tenHocSinh} (Mã: ${child.maHocSinh})</h5>
                    <span class="badge bg-light text-primary">${child.trangThai}</span>
                </div>
                <div class="card-body">
                    <p><strong>Lớp học:</strong> Lớp ${child.lop}</p>
                    
                    <c:choose>
                        <%-- TRƯỜNG HỢP 1: CON CHƯA HOẠT ĐỘNG -> BẮT BUỘC CHỌN TUYẾN ĐỂ KÍCH HOẠT --%>
                        <c:when test="${child.trangThai == 'Ngưng hoạt động'}">
                            <div class="alert alert-warning">
                                Con chưa được kích hoạt dịch vụ xe đưa đón. Vui lòng chọn Tuyến đường và Điểm dừng!
                            </div>
                            <form action="parent-action" method="POST" class="row g-3">
                                <input type="hidden" name="action" value="activate">
                                <input type="hidden" name="maHocSinh" value="${child.maHocSinh}">
                                <div class="col-md-4">
                                    <label class="form-label">Chọn Tuyến Đường</label>
                                    <select class="form-select" name="routeID" required>
                                        <option value="1">LT1: Ocean Park -> Marie Curie</option>
                                        <option value="2">LT2: Long Biên -> Marie Curie</option>
                                    </select>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label">Chọn Trạm Đón</label>
                                    <select class="form-select" name="stopID" required>
                                        <option value="1">S2.15 (Ocean Park)</option>
                                        <option value="4">LandMark 72</option>
                                    </select>
                                </div>
                                <div class="col-md-4 d-flex align-items-end">
                                    <button type="submit" class="btn btn-success w-100">Kích Hoạt Dịch Vụ</button>
                                </div>
                            </form>
                        </c:when>

                        <%-- TRƯỜNG HỢP 2: CON ĐANG HOẠT ĐỘNG -> HIỆN ĐƠN XIN NGHỈ & NÚT HỦY DỊCH VỤ --%>
                        <c:otherwise>
                            <p><strong>Tuyến xe cố định:</strong> Tuyến mã số ${child.defaultRouteID} - Điểm dừng số ${child.defaultStopID}</p>
                            
                            <div class="row mt-3 border-top pt-3">
                                <div class="col-md-6 border-end">
                                    <h6>Báo nghỉ học cho con hôm nay/ngày mai</h6>
                                    <form action="parent-action" method="POST" class="d-flex gap-2">
                                        <input type="hidden" name="action" value="leave">
                                        <input type="hidden" name="maHocSinh" value="${child.maHocSinh}">
                                        <input type="date" name="leaveDate" class="form-control" required>
                                        <button type="submit" class="btn btn-warning text-white btn-sm text-nowrap">Gửi Báo Nghỉ</button>
                                    </form>
                                </div>
                                
                                <div class="col-md-6 d-flex align-items-center justify-content-center">
                                    <form action="parent-action" method="POST" onsubmit="return confirm('Bạn có chắc chắn muốn ngưng dịch vụ đưa đón?')">
                                        <input type="hidden" name="action" value="stopService">
                                        <input type="hidden" name="maHocSinh" value="${child.maHocSinh}">
                                        <button type="submit" class="btn btn-danger">Hủy Dịch Vụ Đưa Đón</button>
                                    </form>
                                </div>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </c:forEach>
    </div>
</body>
</html>