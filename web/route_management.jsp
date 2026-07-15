<%@page import="java.util.List"%>
<%@page import="model.Route"%>
<%@page import="model.Stop"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Quản lý Tuyến đường (Lộ trình).
    Cho phép quản trị viên xem danh sách tuyến, thêm tuyến mới, quản lý các điểm dừng (stops) trên tuyến, và sắp xếp thứ tự điểm dừng.
--%>
<%
    // Kiểm tra đăng nhập
    if(session.getAttribute("userRole") == null || !"admin".equals(session.getAttribute("userRole"))) {
        response.sendRedirect("dang_nhap.jsp");
        return;
    }
    List<Route> routes = (List<Route>) request.getAttribute("routes");
    Route selectedRoute = (Route) request.getAttribute("selectedRoute");
    List<Stop> stops = (List<Stop>) request.getAttribute("stops");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Lộ trình - School Bus</title>
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Leaflet CSS -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f4f6f9;
        }
        .navbar {
            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
        .route-card {
            cursor: pointer;
            transition: all 0.2s;
            border-left: 4px solid transparent;
        }
        .route-card:hover {
            background-color: #f8f9fa;
        }
        .route-card.active {
            border-left-color: #0d6efd;
            background-color: #e9ecef;
        }
        #map {
            height: 400px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            z-index: 1; /* Để tránh đè lên dropdown nếu có */
        }
        /* Timeline Styles */
        .timeline {
            list-style: none;
            padding: 20px 0 20px;
            position: relative;
        }
        .timeline:before {
            top: 0;
            bottom: 0;
            position: absolute;
            content: " ";
            width: 3px;
            background-color: #dee2e6;
            left: 20px;
            margin-left: -1.5px;
        }
        .timeline > li {
            margin-bottom: 20px;
            position: relative;
        }
        .timeline > li:before, .timeline > li:after {
            content: " ";
            display: table;
        }
        .timeline > li:after {
            clear: both;
        }
        .timeline-panel {
            width: calc(100% - 50px);
            float: right;
            padding: 15px;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
        .timeline-badge {
            color: #fff;
            width: 30px;
            height: 30px;
            line-height: 30px;
            font-size: 14px;
            text-align: center;
            position: absolute;
            top: 10px;
            left: 5px;
            background-color: #28a745;
            border-radius: 50%;
            z-index: 2;
        }
        .timeline-badge.start { background-color: #0d6efd; }
        .timeline-badge.end { background-color: #dc3545; }
    </style>
</head>
<body>

    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-dark sticky-top py-3">
        <div class="container-fluid px-4">
            <a class="navbar-brand fw-bold" href="admin_dashboard.jsp">
                <i class="bi bi-arrow-left-circle me-2"></i>Quản lý Lộ trình
            </a>
            <div class="d-flex align-items-center">
                <span class="text-light me-3"><i class="bi bi-person-circle me-1"></i> Admin</span>
            </div>
        </div>
    </nav>

    <div class="container-fluid px-4 my-4">
        <div class="row g-4">
            <!-- Cột trái: Danh sách tuyến đường -->
            <div class="col-md-4 col-lg-3">
                <div class="card border-0 shadow-sm">
                    <div class="card-header bg-white pt-3 pb-2">
                        <h6 class="fw-bold mb-0 text-primary">Danh sách Tuyến</h6>
                    </div>
                    <div class="card-body p-0">
                        <ul class="list-group list-group-flush">
                            <% if (routes != null) {
                                for (Route r : routes) { 
                                    boolean isActive = (selectedRoute != null && selectedRoute.getRouteID() == r.getRouteID());
                            %>
                            <a href="route-management?routeID=<%= r.getRouteID() %>" class="text-decoration-none text-dark">
                                <li class="list-group-item route-card <%= isActive ? "active" : "" %> p-3">
                                    <div class="d-flex w-100 justify-content-between align-items-center">
                                        <h6 class="mb-1 fw-bold"><%= r.getRouteCode() %></h6>
                                    </div>
                                    <p class="mb-0 text-muted small"><%= r.getRouteName() %></p>
                                </li>
                            </a>
                            <%  }
                               } else { %>
                               <li class="list-group-item">Chưa có dữ liệu tuyến đường.</li>
                            <% } %>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- Cột phải: Chi tiết tuyến đường -->
            <div class="col-md-8 col-lg-9">
                <% if (selectedRoute != null) { %>
                <div class="card border-0 shadow-sm mb-4">
                    <div class="card-body">
                        <h5 class="fw-bold text-dark mb-1"><%= selectedRoute.getRouteCode() %>: <%= selectedRoute.getRouteName() %></h5>
                        <p class="text-muted small mb-3"><%= selectedRoute.getDescription() %></p>
                        
                        <!-- Bản đồ -->
                        <div id="map" class="mb-4"></div>

                        <!-- Danh sách trạm (Timeline) -->
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h6 class="fw-bold text-primary mb-0"><i class="bi bi-list-ol me-2"></i>Các điểm đón và Thời gian dự kiến</h6>
                            <div>
                                <button class="btn btn-sm btn-outline-primary me-2" id="btnReverseRoute">
                                    <i class="bi bi-arrow-down-up me-1"></i> Đảo chiều
                                </button>
                                <button class="btn btn-sm btn-success" data-bs-toggle="modal" data-bs-target="#addStopModal">
                                    <i class="bi bi-plus-circle me-1"></i> Thêm trạm
                                </button>
                            </div>
                        </div>
                        
                        <% if (stops != null && !stops.isEmpty()) { %>
                        <ul class="timeline" id="routeTimeline">
                            <!-- JS sẽ render timeline vào đây -->
                        </ul>
                        <% } else { %>
                            <div class="alert alert-info">Chưa có dữ liệu trạm cho tuyến đường này.</div>
                        <% } %>

                    </div>
                </div>

                <!-- Modal Thêm Trạm Mới -->
                <div class="modal fade" id="addStopModal" tabindex="-1" aria-labelledby="addStopModalLabel" aria-hidden="true">
                  <div class="modal-dialog modal-lg modal-dialog-scrollable">
                    <form action="add-stop" method="POST" class="modal-content">
                      <div class="modal-header bg-primary text-white">
                        <h5 class="modal-title fw-bold" id="addStopModalLabel"><i class="bi bi-pin-map-fill me-2"></i>Thêm Điểm Dừng Mới</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                      </div>
                      <div class="modal-body">
                              <input type="hidden" name="routeID" value="<%= selectedRoute.getRouteID() %>">
                              <input type="hidden" name="latitude" id="latInput">
                              <input type="hidden" name="longitude" id="lngInput">
                              
                              <div class="row g-3 mb-3">
                                  <div class="col-md-12">
                                      <label class="form-label fw-bold">Tên trạm dừng</label>
                                      <input type="text" class="form-control" name="stopName" required placeholder="Vd: S2.15 Vinhomes Ocean Park">
                                  </div>
                                  <div class="col-md-6">
                                      <label class="form-label fw-bold">Dự kiến đón (Sáng)</label>
                                      <input type="time" class="form-control" name="estimatedTime" required min="06:30" max="08:00">
                                  </div>
                                  <div class="col-md-6">
                                      <label class="form-label fw-bold">Dự kiến trả (Chiều)</label>
                                      <input type="time" class="form-control" name="returnTime" required min="16:30" max="18:30">
                                  </div>
                              </div>
                              
                              <div class="mb-3">
                                  <label class="form-label fw-bold">Tìm kiếm địa chỉ & Ghim vị trí</label>
                                  <div class="input-group mb-2">
                                      <input type="text" class="form-control" id="addressSearch" name="address" placeholder="Nhập địa chỉ để tìm kiếm vị trí..." required>
                                      <button class="btn btn-outline-secondary" type="button" id="btnSearchAddress"><i class="bi bi-search"></i> Tìm</button>
                                  </div>
                                  <div id="miniMap" style="height: 250px; border-radius: 8px; border: 1px solid #ccc;"></div>
                                  <small class="text-muted mt-1 d-block"><i class="bi bi-info-circle me-1"></i>Bạn có thể kéo thả ghim màu đỏ trên bản đồ để căn chỉnh vị trí chính xác nhất.</small>
                              </div>
                      </div>
                      <div class="modal-footer bg-light">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-success"><i class="bi bi-check-circle me-1"></i>Lưu Trạm</button>
                      </div>
                    </form>
                  </div>
                </div>

                <!-- Modal Sửa Thời Gian Trạm -->
                <div class="modal fade" id="editStopModal" tabindex="-1" aria-labelledby="editStopModalLabel" aria-hidden="true">
                  <div class="modal-dialog modal-dialog-centered">
                    <form action="update-route-stop" method="POST" class="modal-content">
                      <div class="modal-header bg-warning text-dark">
                        <h5 class="modal-title fw-bold" id="editStopModalLabel"><i class="bi bi-pencil-square me-2"></i>Sửa Thời Gian Trạm</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                      </div>
                      <div class="modal-body">
                          <input type="hidden" name="routeID" value="<%= selectedRoute.getRouteID() %>">
                          <input type="hidden" name="stopID" id="editStopID">
                          
                          <div class="mb-3">
                              <label class="form-label fw-bold">Tên trạm dừng</label>
                              <input type="text" class="form-control bg-light" id="editStopName" readonly>
                          </div>
                          <div class="row g-3">
                              <div class="col-md-6">
                                  <label class="form-label fw-bold text-primary">Dự kiến đón (Sáng)</label>
                                  <input type="time" class="form-control border-primary" id="editEstimatedTime" name="estimatedTime" required min="06:30" max="08:00">
                              </div>
                              <div class="col-md-6">
                                  <label class="form-label fw-bold text-danger">Dự kiến trả (Chiều)</label>
                                  <input type="time" class="form-control border-danger" id="editReturnTime" name="returnTime" required min="16:30" max="18:30">
                              </div>
                          </div>
                      </div>
                      <div class="modal-footer bg-light">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-warning fw-bold"><i class="bi bi-save me-1"></i>Cập Nhật</button>
                      </div>
                    </form>
                  </div>
                </div>

                <% } else { %>
                <div class="d-flex flex-column align-items-center justify-content-center h-100 text-muted" style="min-height: 400px;">
                    <i class="bi bi-map display-1 mb-3 text-light"></i>
                    <h4>Vui lòng chọn một tuyến đường</h4>
                    <p>Chọn một tuyến đường từ danh sách bên trái để xem bản đồ và lộ trình chi tiết.</p>
                </div>
                <% } %>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <!-- Leaflet JS -->
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <!-- Leaflet Routing Machine JS -->
    <script src="https://unpkg.com/leaflet-routing-machine@latest/dist/leaflet-routing-machine.js"></script>
    <style>
        /* Ẩn bảng hướng dẫn đường đi chi tiết (Turn-by-turn) của Routing Machine */
        .leaflet-routing-container {
            display: none !important;
        }
    </style>
    <script>
        <% if (selectedRoute != null && stops != null && !stops.isEmpty()) { %>
        // Khởi tạo mảng các trạm từ Java sang JavaScript
        var originalStops = [
            <% for (int i = 0; i < stops.size(); i++) {
                   Stop s = stops.get(i);
                   out.print("{id: " + s.getStopID() +
                             ", lat: " + s.getLatitude() + 
                             ", lng: " + s.getLongitude() + 
                             ", name: '" + s.getStopName().replace("'", "\\'") + "'" +
                             ", address: '" + (s.getAddress() != null ? s.getAddress().replace("'", "\\'") : "") + "'" +
                             ", estimatedTime: '" + (s.getEstimatedTime() != null ? s.getEstimatedTime() : "") + "'" +
                             ", returnTime: '" + (s.getReturnTime() != null ? s.getReturnTime() : "") + "'}");
                   if (i < stops.size() - 1) out.print(",");
               }
            %>
        ];

        var isReversed = false;
        var map = L.map('map').setView([originalStops[0].lat, originalStops[0].lng], 14);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap'
        }).addTo(map);

        var control = null;

        function renderRoute() {
            var displayStops = [...originalStops];
            if (isReversed) {
                displayStops.reverse();
            }

            var waypoints = [];
            var timelineHtml = "";

            for (var i = 0; i < displayStops.length; i++) {
                var s = displayStops[i];
                waypoints.push(L.latLng(s.lat, s.lng));

                // Build Timeline
                var badgeClass = "";
                var icon = "bi-geo-alt-fill";
                if (i === 0) {
                    badgeClass = "start";
                    icon = "bi-play-circle-fill";
                } else if (i === displayStops.length - 1) {
                    badgeClass = "end";
                    icon = "bi-stop-circle-fill";
                }
                
                var timeStr = isReversed ? s.returnTime : s.estimatedTime;
                if (!timeStr) timeStr = "--:--";

                timelineHtml += "<li>" +
                    "<div class='timeline-badge " + badgeClass + "'><i class='bi " + icon + "'></i></div>" +
                    "<div class='timeline-panel d-flex justify-content-between align-items-center'>" +
                        "<div>" +
                            "<h6 class='mb-1 fw-bold'>" + s.name + "</h6>" +
                            "<small class='text-muted'><i class='bi bi-geo me-1'></i>" + s.address + "</small>" +
                        "</div>" +
                        "<div class='text-end'>" +
                            "<span class='badge bg-light text-dark border fs-6 me-2'><i class='bi bi-clock me-1 text-primary'></i>" + timeStr + "</span>" +
                            "<button class='btn btn-sm btn-outline-secondary rounded-circle' onclick='openEditModal(" + s.id + ", \"" + s.name.replace(/"/g, '&quot;') + "\", \"" + s.estimatedTime + "\", \"" + s.returnTime + "\")' title='Sửa thời gian'><i class='bi bi-pencil'></i></button>" +
                        "</div>" +
                    "</div>" +
                "</li>";
            }

            document.getElementById('routeTimeline').innerHTML = timelineHtml;

            if (control !== null) {
                map.removeControl(control);
            }

            control = L.Routing.control({
                waypoints: waypoints,
                routeWhileDragging: false,
                addWaypoints: false,
                draggableWaypoints: false,
                fitSelectedRoutes: true,
                showAlternatives: false,
                lineOptions: {
                    styles: [{color: isReversed ? '#dc3545' : '#0d6efd', opacity: 0.8, weight: 6}]
                },
                createMarker: function(i, wp, nWps) {
                    var stop = displayStops[i];
                    var timeStr = isReversed ? stop.returnTime : stop.estimatedTime;
                    if (!timeStr) timeStr = "--:--";
                    
                    var popupContent = "<b>" + stop.name + "</b><br/>" + (isReversed ? "Trở về: " : "Dự kiến: ") + "<span class='text-primary fw-bold'>" + timeStr + "</span>";
                    
                    var marker = L.marker(wp.latLng);
                    marker.bindPopup(popupContent);
                    
                    if (i === 0 || i === nWps - 1) {
                        setTimeout(function() { marker.openPopup(); }, 1000);
                    }
                    return marker;
                }
            }).addTo(map);
        }

        // Init
        renderRoute();

        // Handle button click
        document.getElementById('btnReverseRoute').addEventListener('click', function() {
            isReversed = !isReversed;
            // Đổi màu nút để nhận biết trạng thái
            if(isReversed) {
                this.classList.replace('btn-outline-primary', 'btn-primary');
                this.innerHTML = '<i class="bi bi-arrow-up-down me-1"></i> Trở về chiều đi';
            } else {
                this.classList.replace('btn-primary', 'btn-outline-primary');
                this.innerHTML = '<i class="bi bi-arrow-down-up me-1"></i> Đảo chiều';
            }
            renderRoute();
        });

        // Modal Map Logic
        var miniMap = null;
        var miniMarker = null;

        document.getElementById('addStopModal').addEventListener('shown.bs.modal', function () {
            if (miniMap === null) {
                // Default to Hanoi center
                miniMap = L.map('miniMap').setView([21.028511, 105.804817], 12);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '© OpenStreetMap'
                }).addTo(miniMap);

                miniMap.on('click', function(e) {
                    setMarkerLocation(e.latlng.lat, e.latlng.lng);
                });
            } else {
                miniMap.invalidateSize();
            }
        });

        function setMarkerLocation(lat, lng) {
            document.getElementById('latInput').value = lat;
            document.getElementById('lngInput').value = lng;
            
            if (miniMarker) {
                miniMarker.setLatLng([lat, lng]);
            } else {
                miniMarker = L.marker([lat, lng], {draggable: true}).addTo(miniMap);
                miniMarker.on('dragend', function(event) {
                    var position = miniMarker.getLatLng();
                    document.getElementById('latInput').value = position.lat;
                    document.getElementById('lngInput').value = position.lng;
                });
            }
            miniMap.setView([lat, lng], 15);
        }

        // Search Address with Nominatim API
        document.getElementById('btnSearchAddress').addEventListener('click', function() {
            var query = document.getElementById('addressSearch').value;
            if (!query) return;

            // Search bounding box around Hanoi
            var url = 'https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(query + ' Hanoi') + '&format=json&limit=1';
            
            this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>';
            var btn = this;

            fetch(url)
                .then(response => response.json())
                .then(data => {
                    btn.innerHTML = '<i class="bi bi-search"></i> Tìm';
                    if (data && data.length > 0) {
                        var lat = parseFloat(data[0].lat);
                        var lon = parseFloat(data[0].lon);
                        setMarkerLocation(lat, lon);
                    } else {
                        alert("Không tìm thấy địa chỉ. Vui lòng thử từ khóa khác hoặc click trực tiếp lên bản đồ.");
                    }
                })
                .catch(error => {
                    btn.innerHTML = '<i class="bi bi-search"></i> Tìm';
                    console.error('Error:', error);
                    alert("Đã xảy ra lỗi khi tìm kiếm địa chỉ.");
                });
        });

        // Edit Modal Logic
        function openEditModal(id, name, estTime, retTime) {
            document.getElementById('editStopID').value = id;
            document.getElementById('editStopName').value = name;
            
            // Format time to HH:mm for the input type="time"
            if (estTime && estTime.length > 5) estTime = estTime.substring(0, 5);
            if (retTime && retTime.length > 5) retTime = retTime.substring(0, 5);
            
            document.getElementById('editEstimatedTime').value = estTime;
            document.getElementById('editReturnTime').value = retTime;
            
            var editModal = new bootstrap.Modal(document.getElementById('editStopModal'));
            editModal.show();
        }

        <% } %>
    </script>
</body>
</html>
