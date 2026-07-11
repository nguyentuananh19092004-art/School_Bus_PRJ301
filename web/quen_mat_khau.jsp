<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quên mật khẩu - Hệ thống School Bus</title>
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
            background: rgba(0, 0, 0, 0.4);
            z-index: -1;
        }

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
        
        .main-container {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding-top: 80px;
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
        
        .back-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: rgba(255,255,255,0.7);
            text-decoration: none;
            transition: color 0.3s;
        }
        .back-link:hover {
            color: #ffc107;
        }
    </style>
</head>
<body>
    <div class="bg-image"></div>
    <div class="bg-overlay"></div>

    <header class="top-header">
        <a href="index.jsp" class="navbar-brand">
            <i class="bi bi-bus-front-fill me-2"></i>SCHOOLBUS
        </a>
    </header>

    <div class="main-container">
        <div class="login-card">
            <div class="login-header">
                <h3>Quên mật khẩu</h3>
                <p>Nhập tên đăng nhập để nhận mã OTP qua Email</p>
                <% String error = (String) request.getAttribute("errorMessage");
                   if (error != null) { %>
                    <div class="alert alert-danger mt-3 mb-0 py-2"><i class="bi bi-exclamation-circle-fill me-2"></i><%= error %></div>
                <% } %>
                <% String success = (String) request.getAttribute("successMessage");
                   if (success != null) { %>
                    <div class="alert alert-success mt-3 mb-0 py-2"><i class="bi bi-check-circle-fill me-2"></i><%= success %></div>
                <% } %>
            </div>

            <form action="ForgotPasswordServlet" method="POST">
                <div class="form-floating mb-3">
                    <input type="text" class="form-control" id="username" name="username" placeholder="Tên đăng nhập" value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>" required>
                    <label for="username">Tên đăng nhập</label>
                </div>
                
                <button type="submit" class="btn btn-login w-100">Gửi mã OTP</button>
            </form>
            
            <a href="dang_nhap.jsp" class="back-link"><i class="bi bi-arrow-left me-1"></i> Quay lại đăng nhập</a>
        </div>
    </div>
</body>
</html>
