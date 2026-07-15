<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang Đổi mật khẩu.
    Dành cho người dùng đã đăng nhập, cho phép họ thay đổi mật khẩu hiện tại bằng cách nhập mật khẩu cũ và mới.
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đổi Mật Khẩu - Hệ thống School Bus</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        body, html {
            min-height: 100%;
            margin: 0;
            font-family: 'Inter', sans-serif;
            background-color: #f8f9fa;
        }

        /* Header styling */
        .top-header {
            position: fixed;
            top: 0; left: 0; width: 100%;
            height: 70px;
            display: flex;
            align-items: center;
            background: #212529;
            z-index: 100;
            padding: 0 40px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .navbar-brand {
            font-weight: 800;
            font-size: 1.5rem;
            color: #fff !important;
            text-decoration: none;
            letter-spacing: 1px;
            display: flex;
            align-items: center;
        }
        .navbar-brand i {
            color: #ffc107;
            font-size: 1.8rem;
        }
        .nav-links {
            margin-left: auto;
            display: flex;
            gap: 25px;
        }
        .nav-links a {
            color: rgba(255,255,255,0.9);
            text-decoration: none;
            font-weight: 500;
            font-size: 1rem;
            transition: color 0.3s;
        }
        .nav-links a:hover {
            color: #ffc107;
        }

        /* Main Content */
        .main-container {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding-top: 80px; /* Offset for header */
            padding-bottom: 20px;
        }

        .login-card {
            background: #fff;
            border: 1px solid rgba(0, 0, 0, 0.1);
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.05);
            padding: 40px 40px;
            width: 100%;
            max-width: 440px;
            color: #333;
        }
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        .login-header h3 {
            font-weight: 700;
            font-size: 1.8rem;
            margin-bottom: 5px;
            color: #212529;
        }
        .login-header p {
            color: #6c757d;
            font-size: 0.95rem;
        }
        
        .form-floating > .form-control {
            border-radius: 12px;
            height: calc(3.5rem + 2px);
            padding: 1rem 0.75rem;
        }
        .form-floating > .form-control:focus {
            border-color: #ffc107;
            box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25);
        }

        .btn-login {
            background: linear-gradient(135deg, #ffc107, #ff9800);
            border: none;
            color: #000;
            padding: 12px;
            border-radius: 12px;
            font-weight: 700;
            font-size: 1.05rem;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-top: 15px;
        }
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(255, 152, 0, 0.4);
            color: #000;
        }

        @media (max-width: 768px) {
            .top-header {
                padding: 0 20px;
            }
            .nav-links {
                display: none;
            }
            .login-card {
                padding: 30px 25px;
                max-width: 90%;
            }
        }
    </style>
</head>
<body>
    <%
        if (session.getAttribute("userID") == null) {
            response.sendRedirect("dang_nhap.jsp");
            return;
        }
        
        String role = (String) session.getAttribute("role");
        String backUrl = "index.jsp";
        if (role != null) {
            switch(role) {
                case "ADMIN": backUrl = "admin_dashboard.jsp"; break;
                case "TAIXE": backUrl = "taixe_dashboard.jsp"; break;
                case "GIAMTHI": backUrl = "giamthi_dashboard.jsp"; break;
                case "PHUHUYNH": backUrl = "phuhuynh_dashboard.jsp"; break;
                case "KYTHUAT": backUrl = "kythuat_dashboard.jsp"; break;
            }
        }
    %>

    <header class="top-header">
        <a href="<%= backUrl %>" class="navbar-brand">
            <i class="bi bi-bus-front-fill me-2"></i>SCHOOLBUS
        </a>
        <div class="nav-links">
            <a href="<%= backUrl %>"><i class="bi bi-arrow-left"></i> Quay lại Dashboard</a>
        </div>
    </header>

    <div class="main-container">
        <div class="login-card">
            <div class="login-header">
                <h3>Đổi Mật Khẩu</h3>
                <p>Nhập mật khẩu cũ và mật khẩu mới của bạn</p>
                <% String error = (String) request.getAttribute("errorMessage");
                   if (error != null) { %>
                    <div class="alert alert-danger mt-3 mb-0 py-2"><i class="bi bi-exclamation-circle-fill me-2"></i><%= error %></div>
                <% } %>
                <% String success = (String) request.getAttribute("successMessage");
                   if (success != null) { %>
                    <div class="alert alert-success mt-3 mb-0 py-2"><i class="bi bi-check-circle-fill me-2"></i><%= success %></div>
                <% } %>
            </div>

            <%-- Form xử lý nhập liệu / gửi dữ liệu lên Server --%>
<form action="change-password" method="POST">
                <div class="form-floating mb-3">
                    <input type="password" class="form-control" id="oldPassword" name="oldPassword" placeholder="Mật khẩu cũ" required>
                    <label for="oldPassword">Mật khẩu cũ</label>
                </div>
                
                <div class="form-floating mb-1">
                    <input type="password" class="form-control" id="newPassword" name="newPassword" placeholder="Mật khẩu mới" required pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}" title="Mật khẩu phải từ 8 kí tự trở lên, bao gồm chữ in thường, in hoa, số và kí tự đặc biệt.">
                    <label for="newPassword">Mật khẩu mới</label>
                </div>
                <div class="password-requirements text-start mb-3" style="font-size: 0.85rem; color: #6c757d; padding-left: 5px;">
                    Yêu cầu mật khẩu:
                    <ul class="mb-0 ps-4">
                        <li>Từ 8 ký tự trở lên</li>
                        <li>Chữ cái in thường và in hoa</li>
                        <li>Chữ số và ký tự đặc biệt</li>
                    </ul>
                </div>

                <div class="form-floating mb-3">
                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" placeholder="Xác nhận mật khẩu mới" required>
                    <label for="confirmPassword">Xác nhận mật khẩu mới</label>
                </div>

                <button type="submit" class="btn btn-login w-100">Xác nhận đổi</button>
            </form>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
