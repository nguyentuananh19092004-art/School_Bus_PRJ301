<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- 
    Trang hiển thị giao diện đăng nhập cho người dùng.
    Bao gồm form nhập tài khoản, mật khẩu và xử lý hiển thị thông báo lỗi.
--%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Nhập - Hệ thống School Bus</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <style>
        body, html {
            min-height: 100%;
            margin: 0;
            font-family: 'Inter', sans-serif;
        }
        
        .bg-image {
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: url('img/urban_bus_bg.png') no-repeat center center;
            background-size: cover;
            z-index: -2;
        }

        .bg-overlay {
            position: fixed;
            top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0, 0, 0, 0.4); /* Dark overlay */
            z-index: -1;
        }

        /* Header styling */
        .top-header {
            position: fixed;
            top: 0; left: 0; width: 100%;
            height: 70px;
            display: flex;
            align-items: center;
            background: rgba(0, 0, 0, 0.5);
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
            z-index: 100;
            border-bottom: 1px solid rgba(255,255,255,0.1);
            padding: 0 40px;
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
            background: rgba(15, 15, 15, 0.45);
            backdrop-filter: blur(20px);
            -webkit-backdrop-filter: blur(20px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            border-radius: 20px;
            box-shadow: 0 30px 60px rgba(0,0,0,0.5);
            padding: 40px 40px;
            width: 100%;
            max-width: 440px;
            color: #fff;
        }
        .login-header {
            text-align: center;
            margin-bottom: 30px;
        }
        .login-header h3 {
            font-weight: 700;
            font-size: 1.8rem;
            margin-bottom: 5px;
        }
        .login-header p {
            color: rgba(255,255,255,0.7);
            font-size: 0.95rem;
        }
        
        /* Fixed Form Input Styling */
        .form-floating > .form-control {
            background-color: rgba(0, 0, 0, 0.3) !important;
            border: 1px solid rgba(255, 255, 255, 0.2) !important;
            color: #fff !important;
            border-radius: 12px;
            height: calc(3.5rem + 2px);
            padding: 1rem 0.75rem;
        }
        .form-floating > .form-control:focus {
            background-color: rgba(0, 0, 0, 0.5) !important;
            border-color: #ffc107 !important;
            box-shadow: 0 0 0 0.2rem rgba(255, 193, 7, 0.25) !important;
        }
        .form-floating > label {
            color: rgba(255, 255, 255, 0.6);
            padding: 1rem 0.75rem;
        }
        .form-floating > label::after {
            background-color: transparent !important;
        }
        .form-floating > .form-control:focus ~ label,
        .form-floating > .form-control:not(:placeholder-shown) ~ label {
            color: #ffc107;
            transform: scale(0.85) translateY(-0.8rem) translateX(0.15rem);
        }
        
        /* Fix Chrome Autocomplete */
        input:-webkit-autofill,
        input:-webkit-autofill:hover, 
        input:-webkit-autofill:focus, 
        input:-webkit-autofill:active{
            -webkit-box-shadow: 0 0 0 30px rgba(0, 0, 0, 0.5) inset !important;
            -webkit-text-fill-color: white !important;
            transition: background-color 5000s ease-in-out 0s;
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
            box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-top: 15px;
        }
        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(255, 152, 0, 0.6);
            color: #000;
        }

        .form-check-label {
            color: rgba(255,255,255,0.7);
            font-size: 0.9rem;
        }
        .forgot-pass {
            color: #ffc107;
            text-decoration: none;
            font-size: 0.9rem;
            transition: color 0.3s;
        }
        .forgot-pass:hover {
            color: #fff;
            text-decoration: underline;
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

    <div class="bg-image"></div>
    <div class="bg-overlay"></div>

    <!-- Header riêng biệt -->
    <header class="top-header">
        <a href="index.jsp" class="navbar-brand">
            <i class="bi bi-bus-front-fill me-2"></i>SCHOOLBUS
        </a>
        <div class="nav-links">
            <a href="index.jsp">Trang Chủ</a>
            <a href="index.jsp#ve-chung-toi">Về Chúng Tôi</a>
        </div>
    </header>

    <div class="main-container">
        
        <div class="login-card">
            <div class="login-header">
                <h3>Đăng nhập hệ thống</h3>
                <p>Vui lòng nhập tài khoản của bạn</p>
                <% String error = (String) request.getAttribute("errorMessage");
                   if (error != null) { %>
                    <div class="alert alert-danger mt-3 mb-0 py-2"><i class="bi bi-exclamation-circle-fill me-2"></i><%= error %></div>
                <% } %>
            </div>

            <form action="LoginServlet" method="POST">

                <% String usernameVal = (String) request.getAttribute("username"); 
                   String passwordVal = (String) request.getAttribute("password"); %>
                <!-- Credentials -->
                <div class="form-floating mb-3">
                    <input type="text" class="form-control" id="username" name="username" placeholder="Tên đăng nhập" value="<%= usernameVal != null ? usernameVal : "" %>" required>
                    <label for="username">Tên đăng nhập</label>
                </div>
                
                <div class="form-floating mb-4 position-relative">
                    <input type="password" class="form-control" id="password" name="password" placeholder="Mật khẩu" value="<%= passwordVal != null ? passwordVal : "" %>" required style="padding-right: 40px;">
                    <label for="password">Mật khẩu</label>
                    <i class="bi bi-eye-slash position-absolute top-50 end-0 translate-middle-y me-3 text-white toggle-password" data-target="password" style="cursor: pointer; z-index: 10;"></i>
                </div>
                
                <div class="text-end mb-3">
                    <a href="quen_mat_khau.jsp" class="forgot-pass">Quên mật khẩu?</a>
                </div>

                <button type="submit" class="btn btn-login w-100">Đăng Nhập</button>
            </form>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.querySelectorAll('.toggle-password').forEach(function(icon) {
            icon.addEventListener('click', function() {
                const targetId = this.getAttribute('data-target');
                const input = document.getElementById(targetId);
                if (input.getAttribute('type') === 'password') {
                    input.setAttribute('type', 'text');
                    this.classList.replace('bi-eye-slash', 'bi-eye');
                } else {
                    input.setAttribute('type', 'password');
                    this.classList.replace('bi-eye', 'bi-eye-slash');
                }
            });
        });
    </script>
</body>
</html>
