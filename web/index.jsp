<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hệ thống Xe Bus Đưa Đón Học Sinh - Marie Curie</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        .hero-section {
            background-color: #f8f9fa;
            padding: 80px 0;
            border-bottom: 1px solid #dee2e6;
        }
        .bus-image {
            max-width: 100%;
            height: auto;
            mix-blend-mode: multiply;
        }
        .feature-icon {
            font-size: 2.5rem;
            color: #ffc107; /* Màu vàng xe bus */
        }
        .navbar-brand {
            font-weight: 700;
            color: #ffc107 !important;
            text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
        }
    </style>
</head>
<body>

    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
        <div class="container">
            <a class="navbar-brand" href="index.jsp">
                <i class="bi bi-bus-front-fill me-2"></i>SchoolBus
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse justify-content-end" id="navbarNav">
                <ul class="navbar-nav me-4">
                    <li class="nav-item"><a class="nav-link" href="#ve-chung-toi">Về chúng tôi</a></li>
                </ul>
                <div class="d-flex gap-2">
                    <a href="dang_nhap.jsp" class="btn btn-warning fw-bold px-4">Đăng Nhập</a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-section text-center text-lg-start">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-lg-6 mb-5 mb-lg-0">
                    <h1 class="display-4 fw-bold mb-4">An toàn trên mọi nẻo đường tới trường</h1>
                    <p class="lead text-secondary mb-5">Hệ thống quản lý xe đưa đón học sinh trường Marie Curie. Giúp phụ huynh an tâm, nhà trường dễ dàng quản lý và giám sát chặt chẽ.</p>
                    <div class="d-grid gap-3 d-md-flex justify-content-md-start">
                        <a href="dang_nhap.jsp" class="btn btn-warning btn-lg px-5 fw-bold shadow-sm">Bắt đầu ngay</a>
                    </div>
                </div>
                <div class="col-lg-6 text-center">
                    <!-- Ảnh do người dùng upload -->
                    <img src="img/anh1.png" alt="School Bus" class="bus-image img-fluid">
                </div>
            </div>
        </div>
    </section>

    <!-- Features Section -->
    <section class="py-5 bg-white" id="ve-chung-toi">
        <div class="container my-5">
            <div class="row text-center g-5">
                <div class="col-md-4">
                    <div class="feature-icon mb-3"><i class="bi bi-shield-check"></i></div>
                    <h3 class="h4 fw-bold">An Toàn Tuyệt Đối</h3>
                    <p class="text-secondary">Hệ thống giám sát điểm danh chặt chẽ từ khi lên xe tới khi xuống xe. Loại bỏ rủi ro để quên học sinh.</p>
                </div>
                <div class="col-md-4">
                    <div class="feature-icon mb-3"><i class="bi bi-bell-fill"></i></div>
                    <h3 class="h4 fw-bold">Thông Báo Thời Gian Thực</h3>
                    <p class="text-secondary">Phụ huynh nhận thông báo ngay lập tức trước khi xe tới trạm 15 phút, và khi con đã lên/xuống xe an toàn.</p>
                </div>
                <div class="col-md-4">
                    <div class="feature-icon mb-3"><i class="bi bi-geo-alt-fill"></i></div>
                    <h3 class="h4 fw-bold">Lộ Trình Tối Ưu</h3>
                    <p class="text-secondary">8 lộ trình phủ khắp Hà Nội với các điểm đón tập trung, đảm bảo học sinh di chuyển thuận tiện nhất.</p>
                </div>
            </div>
        </div>
    </section>



    <!-- Footer -->
    <footer class="bg-dark text-light py-4 text-center">
        <div class="container">
            <p class="mb-1"><i class="bi bi-geo-alt-fill me-2 text-warning"></i>Địa chỉ: KĐT Mỹ Đình - Mễ Trì, Nam Từ Liêm, Hà Nội</p>
            <p class="mb-1"><i class="bi bi-telephone-fill me-2 text-warning"></i>Số điện thoại: 024 3222 2555</p>
            <p class="mb-3"><i class="bi bi-envelope-fill me-2 text-warning"></i>Email: mariecuriehanoi@gmail.com</p>
            <p class="mb-0">&copy; 2026 Hệ thống School Bus Marie Curie. All rights reserved.</p>
        </div>
    </footer>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
