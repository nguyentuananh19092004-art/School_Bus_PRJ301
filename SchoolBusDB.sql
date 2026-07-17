-- =======================================================
-- Database Script cho Hệ thống School Bus (SQL Server)
-- =======================================================

USE master;
GO

-- Xóa database nếu đã tồn tại
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'SchoolBusDB')
BEGIN
    ALTER DATABASE SchoolBusDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE SchoolBusDB;
END
GO

CREATE DATABASE SchoolBusDB;
GO

USE SchoolBusDB;
GO

-- 1. Bảng Users (Tài khoản người dùng: Admin, Parent, Driver, Monitor)
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password VARCHAR(255) NOT NULL, -- Sẽ được hash
    Role VARCHAR(20) NOT NULL CHECK (Role IN ('ADMIN', 'PARENT', 'DRIVER', 'MONITOR', 'TECHNICIAN')),
    FullName NVARCHAR(100) NOT NULL,
    Phone VARCHAR(15),
    Email VARCHAR(100),
    Status NVARCHAR(50) DEFAULT N'Sẵn sàng'
);
GO
SET QUOTED_IDENTIFIER ON;
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_Users_Phone ON Users(Phone) WHERE Phone IS NOT NULL AND Phone <> '';
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_Users_Email ON Users(Email) WHERE Email IS NOT NULL AND Email <> '';
GO

-- 2. Bảng Buses (Thông tin xe)
CREATE TABLE Buses (
    BusID INT IDENTITY(1,1) PRIMARY KEY,
    LicensePlate VARCHAR(20) UNIQUE NOT NULL,
    Capacity INT NOT NULL CHECK (Capacity IN (7, 9)),
    Status NVARCHAR(50) DEFAULT N'Sẵn sàng' -- Sẵn sàng, Hoạt động, Bảo dưỡng/Sửa chữa
);

-- 3. Bảng Routes (Các tuyến đường)
CREATE TABLE Routes (
    RouteID INT IDENTITY(1,1) PRIMARY KEY,
    RouteCode VARCHAR(10) UNIQUE NOT NULL, -- LT1, LT2...
    RouteName NVARCHAR(150) NOT NULL,
    Description NVARCHAR(500)
);

-- 4. Bảng Stops (Các điểm dừng/đón)
CREATE TABLE Stops (
    StopID INT IDENTITY(1,1) PRIMARY KEY,
    StopName NVARCHAR(200) NOT NULL,
    Address NVARCHAR(300),
    Latitude DECIMAL(10,8),
    Longitude DECIMAL(11,8)
);

-- 5. Bảng RouteStops (Nối Route và Stop - Thứ tự điểm đón)
CREATE TABLE RouteStops (
    RouteID INT FOREIGN KEY REFERENCES Routes(RouteID),
    StopID INT FOREIGN KEY REFERENCES Stops(StopID),
    StopOrder INT NOT NULL,
    EstimatedTime TIME, -- Giờ đón (chiều đi)
    ReturnTime TIME,    -- Giờ trả (chiều về)
    PRIMARY KEY (RouteID, StopID)
);

-- 6. Bảng HocSinh (Thông tin học sinh)
CREATE TABLE HocSinh (
    MaHocSinh VARCHAR(20) PRIMARY KEY,
    TenHocSinh NVARCHAR(100) NOT NULL,
    Lop INT CHECK (Lop BETWEEN 1 AND 5),
    TenTK VARCHAR(50) UNIQUE NOT NULL,
    MatKhau VARCHAR(255) DEFAULT '123',
    Phone VARCHAR(15),
    Email VARCHAR(100),
    DefaultStopID INT FOREIGN KEY REFERENCES Stops(StopID),
    DefaultRouteID INT FOREIGN KEY REFERENCES Routes(RouteID),
    TrangThai NVARCHAR(20) DEFAULT N'Ngưng hoạt động',
    PendingStopID INT FOREIGN KEY REFERENCES Stops(StopID),
    PendingRouteID INT FOREIGN KEY REFERENCES Routes(RouteID),
    EffectiveDate DATE
);
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_HocSinh_Phone ON HocSinh(Phone) WHERE Phone IS NOT NULL AND Phone <> '';
GO
CREATE UNIQUE NONCLUSTERED INDEX UQ_HocSinh_Email ON HocSinh(Email) WHERE Email IS NOT NULL AND Email <> '';

-- 7. Bảng Schedules (Lịch chạy hàng ngày)
CREATE TABLE Schedules (
    ScheduleID INT IDENTITY(1,1) PRIMARY KEY,
    Date DATE NOT NULL,
    Direction VARCHAR(10) NOT NULL CHECK (Direction IN ('TO_SCHOOL', 'TO_HOME')),
    RouteID INT FOREIGN KEY REFERENCES Routes(RouteID),
    BusID INT FOREIGN KEY REFERENCES Buses(BusID),
    DriverID INT FOREIGN KEY REFERENCES Users(UserID),
    MonitorID INT FOREIGN KEY REFERENCES Users(UserID),
    Status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    IncidentStatus VARCHAR(20) DEFAULT 'NORMAL', -- NORMAL, INCIDENT
    ReplacementBusID INT NULL,
    HandlingTechID INT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Schedules_ReplacementBus FOREIGN KEY (ReplacementBusID) REFERENCES Buses(BusID),
    CONSTRAINT FK_Schedules_HandlingTech FOREIGN KEY (HandlingTechID) REFERENCES Users(UserID)
);

-- Bảng UserLeaves (Lịch báo nghỉ của Nhân sự)
CREATE TABLE UserLeaves (
    LeaveID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT FOREIGN KEY REFERENCES Users(UserID),
    LeaveDate DATE NOT NULL,
    Reason NVARCHAR(255),
    Status VARCHAR(20) DEFAULT 'PENDING',
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Bảng BusMaintenances (Lịch bảo dưỡng Xe bus)
CREATE TABLE BusMaintenances (
    MaintenanceID INT IDENTITY(1,1) PRIMARY KEY,
    BusID INT FOREIGN KEY REFERENCES Buses(BusID),
    MaintenanceDate DATE NOT NULL,
    Description NVARCHAR(255),
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Bảng Notifications (Hòm thư thông báo)
CREATE TABLE Notifications (
    NotifID INT IDENTITY(1,1) PRIMARY KEY,
    UserID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    Message NVARCHAR(1000) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    IsRead BIT DEFAULT 0
);

-- 8. Bảng Attendances (Điểm danh lên/xuống xe)
CREATE TABLE Attendances (
    AttendanceID INT IDENTITY(1,1) PRIMARY KEY,
    ScheduleID INT FOREIGN KEY REFERENCES Schedules(ScheduleID),
    MaHocSinh VARCHAR(20) FOREIGN KEY REFERENCES HocSinh(MaHocSinh),
    StopID INT, -- Điểm thực tế đón/trả
    BoardingTime DATETIME, -- Thời gian lên xe
    AlightingTime DATETIME, -- Thời gian xuống xe
    IsAbsent BIT DEFAULT 0, -- 1: Đã báo nghỉ hoặc vắng mặt
    Note NVARCHAR(255)
);

-- 9. Bảng ScheduleProgress (Tiến trình chuyến đi)
CREATE TABLE ScheduleProgress (
    ProgressID INT IDENTITY(1,1) PRIMARY KEY,
    ScheduleID INT FOREIGN KEY REFERENCES Schedules(ScheduleID),
    StopID INT,
    ArrivalTime DATETIME NOT NULL
);

-- 10. Bảng TechnicianSchedules (Phân ca Kỹ thuật)
CREATE TABLE TechnicianSchedules (
    TechScheduleID INT IDENTITY(1,1) PRIMARY KEY,
    TechnicianID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    Date DATE NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    Status VARCHAR(20) DEFAULT 'PENDING',
    CONSTRAINT UC_TechnicianDate UNIQUE (TechnicianID, Date)
);

-- 11. Bảng StudentLeaves (Lịch nghỉ của Học sinh)
CREATE TABLE StudentLeaves (
    LeaveID INT IDENTITY(1,1) PRIMARY KEY,
    MaHocSinh VARCHAR(20) NOT NULL FOREIGN KEY REFERENCES HocSinh(MaHocSinh),
    LeaveDate DATE NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT UC_StudentLeave UNIQUE (MaHocSinh, LeaveDate)
);

GO
-- =======================================================
-- INSERT DỮ LIỆU MẪU (SEED DATA)
-- =======================================================

-- 1. Insert Users
INSERT INTO Users (Username, Password, Role, FullName, Phone, Email, Status) VALUES
('admin', '123', 'ADMIN', N'Quản trị viên', '0900000001', 'admin@gmail.com', N'Sẵn sàng'),
('taixe1', '123', 'DRIVER', N'Tài xế 1', '0900000011', 'taixe1@gmail.com', N'Sẵn sàng'),
('taixe2', '123', 'DRIVER', N'Tài xế 2', '0900000012', 'taixe2@gmail.com', N'Sẵn sàng'),
('taixe3', '123', 'DRIVER', N'Tài xế 3', '0900000013', 'taixe3@gmail.com', N'Sẵn sàng'),
('taixe4', '123', 'DRIVER', N'Tài xế 4', '0900000014', 'taixe4@gmail.com', N'Sẵn sàng'),
('taixe5', '123', 'DRIVER', N'Tài xế 5', '0900000015', 'taixe5@gmail.com', N'Sẵn sàng'),
('taixe6', '123', 'DRIVER', N'Tài xế 6', '0900000016', 'taixe6@gmail.com', N'Sẵn sàng'),
('taixe7', '123', 'DRIVER', N'Tài xế 7', '0900000017', 'taixe7@gmail.com', N'Sẵn sàng'),
('taixe8', '123', 'DRIVER', N'Tài xế 8', '0900000018', 'taixe8@gmail.com', N'Sẵn sàng'),
('taixe9', '123', 'DRIVER', N'Tài xế 9', '0900000019', 'taixe9@gmail.com', N'Sẵn sàng'),
('taixe10', '123', 'DRIVER', N'Tài xế 10', '0900000010', 'taixe10@gmail.com', N'Sẵn sàng'),
('giamsat1', '123', 'MONITOR', N'Giám sát 1', '0900000021', 'giamsat1@gmail.com', N'Sẵn sàng'),
('giamsat2', '123', 'MONITOR', N'Giám sát 2', '0900000022', 'giamsat2@gmail.com', N'Sẵn sàng'),
('giamsat3', '123', 'MONITOR', N'Giám sát 3', '0900000023', 'giamsat3@gmail.com', N'Sẵn sàng'),
('giamsat4', '123', 'MONITOR', N'Giám sát 4', '0900000024', 'giamsat4@gmail.com', N'Sẵn sàng'),
('giamsat5', '123', 'MONITOR', N'Giám sát 5', '0900000025', 'giamsat5@gmail.com', N'Sẵn sàng'),
('giamsat6', '123', 'MONITOR', N'Giám sát 6', '0900000026', 'giamsat6@gmail.com', N'Sẵn sàng'),
('giamsat7', '123', 'MONITOR', N'Giám sát 7', '0900000027', 'giamsat7@gmail.com', N'Sẵn sàng'),
('giamsat8', '123', 'MONITOR', N'Giám sát 8', '0900000028', 'giamsat8@gmail.com', N'Sẵn sàng'),
('giamsat9', '123', 'MONITOR', N'Giám sát 9', '0900000029', 'giamsat9@gmail.com', N'Sẵn sàng'),
('giamsat10', '123', 'MONITOR', N'Giám sát 10', '0900000020', 'giamsat10@gmail.com', N'Sẵn sàng'),
('kythuat1', '123', 'TECHNICIAN', N'Kỹ thuật 1', '0900000031', 'kythuat1@gmail.com', N'Sẵn sàng'),
('kythuat2', '123', 'TECHNICIAN', N'Kỹ thuật 2', '0900000032', 'kythuat2@gmail.com', N'Sẵn sàng'),
('kythuat3', '123', 'TECHNICIAN', N'Kỹ thuật 3', '0900000033', 'kythuat3@gmail.com', N'Sẵn sàng'),
('kythuat4', '123', 'TECHNICIAN', N'Kỹ thuật 4', '0900000034', 'kythuat4@gmail.com', N'Sẵn sàng'),
('kythuat5', '123', 'TECHNICIAN', N'Kỹ thuật 5', '0900000035', 'kythuat5@gmail.com', N'Sẵn sàng'),
('A1NVL1001', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A1', NULL, 'A1NVL1001@gmail.com', N'Sẵn sàng'),
('A2NVL1002', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A2', NULL, 'A2NVL1002@gmail.com', N'Sẵn sàng'),
('A3NVL1003', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A3', NULL, 'A3NVL1003@gmail.com', N'Sẵn sàng'),
('A4NVL1004', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A4', NULL, 'A4NVL1004@gmail.com', N'Sẵn sàng'),
('A5NVL1005', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A5', NULL, 'A5NVL1005@gmail.com', N'Sẵn sàng'),
('A6NVL1006', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A6', NULL, 'A6NVL1006@gmail.com', N'Sẵn sàng'),
('A7NVL1007', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A7', NULL, 'A7NVL1007@gmail.com', N'Sẵn sàng'),
('A8NVL1008', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A8', NULL, 'A8NVL1008@gmail.com', N'Sẵn sàng'),
('A9NVL1009', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A9', NULL, 'A9NVL1009@gmail.com', N'Sẵn sàng'),
('A10NVL1010', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A10', NULL, 'A10NVL1010@gmail.com', N'Sẵn sàng'),
('A11NVL1011', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A11', NULL, 'A11NVL1011@gmail.com', N'Sẵn sàng'),
('A12NVL1012', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A12', NULL, 'A12NVL1012@gmail.com', N'Sẵn sàng'),
('A13NVL1013', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A13', NULL, 'A13NVL1013@gmail.com', N'Sẵn sàng'),
('A14NVL1014', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A14', NULL, 'A14NVL1014@gmail.com', N'Sẵn sàng'),
('A15NVL1015', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A15', NULL, 'A15NVL1015@gmail.com', N'Sẵn sàng'),
('A16NVL1016', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A16', NULL, 'A16NVL1016@gmail.com', N'Sẵn sàng'),
('A17NVL1017', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A17', NULL, 'A17NVL1017@gmail.com', N'Sẵn sàng'),
('A18NVL1018', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A18', NULL, 'A18NVL1018@gmail.com', N'Sẵn sàng'),
('A19NVL1019', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A19', NULL, 'A19NVL1019@gmail.com', N'Sẵn sàng'),
('A20NVL1020', '123', 'PARENT', N'Phụ huynh Nguyễn Văn A20', NULL, 'A20NVL1020@gmail.com', N'Sẵn sàng'),
('A1NVL2001', '123', 'PARENT', N'Phụ huynh Trần Thị B1', NULL, 'A1NVL2001@gmail.com', N'Sẵn sàng'),
('A2NVL2002', '123', 'PARENT', N'Phụ huynh Trần Thị B2', NULL, 'A2NVL2002@gmail.com', N'Sẵn sàng'),
('A3NVL2003', '123', 'PARENT', N'Phụ huynh Trần Thị B3', NULL, 'A3NVL2003@gmail.com', N'Sẵn sàng'),
('A4NVL2004', '123', 'PARENT', N'Phụ huynh Trần Thị B4', NULL, 'A4NVL2004@gmail.com', N'Sẵn sàng'),
('A5NVL2005', '123', 'PARENT', N'Phụ huynh Trần Thị B5', NULL, 'A5NVL2005@gmail.com', N'Sẵn sàng'),
('A6NVL2006', '123', 'PARENT', N'Phụ huynh Trần Thị B6', NULL, 'A6NVL2006@gmail.com', N'Sẵn sàng'),
('A7NVL2007', '123', 'PARENT', N'Phụ huynh Trần Thị B7', NULL, 'A7NVL2007@gmail.com', N'Sẵn sàng'),
('A8NVL2008', '123', 'PARENT', N'Phụ huynh Trần Thị B8', NULL, 'A8NVL2008@gmail.com', N'Sẵn sàng'),
('A9NVL2009', '123', 'PARENT', N'Phụ huynh Trần Thị B9', NULL, 'A9NVL2009@gmail.com', N'Sẵn sàng'),
('A10NVL2010', '123', 'PARENT', N'Phụ huynh Trần Thị B10', NULL, 'A10NVL2010@gmail.com', N'Sẵn sàng'),
('A11NVL2011', '123', 'PARENT', N'Phụ huynh Trần Thị B11', NULL, 'A11NVL2011@gmail.com', N'Sẵn sàng'),
('A12NVL2012', '123', 'PARENT', N'Phụ huynh Trần Thị B12', NULL, 'A12NVL2012@gmail.com', N'Sẵn sàng'),
('A13NVL2013', '123', 'PARENT', N'Phụ huynh Trần Thị B13', NULL, 'A13NVL2013@gmail.com', N'Sẵn sàng'),
('A14NVL2014', '123', 'PARENT', N'Phụ huynh Trần Thị B14', NULL, 'A14NVL2014@gmail.com', N'Sẵn sàng'),
('A15NVL2015', '123', 'PARENT', N'Phụ huynh Trần Thị B15', NULL, 'A15NVL2015@gmail.com', N'Sẵn sàng'),
('A16NVL2016', '123', 'PARENT', N'Phụ huynh Trần Thị B16', NULL, 'A16NVL2016@gmail.com', N'Sẵn sàng'),
('A17NVL2017', '123', 'PARENT', N'Phụ huynh Trần Thị B17', NULL, 'A17NVL2017@gmail.com', N'Sẵn sàng'),
('A18NVL2018', '123', 'PARENT', N'Phụ huynh Trần Thị B18', NULL, 'A18NVL2018@gmail.com', N'Sẵn sàng'),
('A19NVL2019', '123', 'PARENT', N'Phụ huynh Trần Thị B19', NULL, 'A19NVL2019@gmail.com', N'Sẵn sàng'),
('A20NVL2020', '123', 'PARENT', N'Phụ huynh Trần Thị B20', NULL, 'A20NVL2020@gmail.com', N'Sẵn sàng'),
('A1NVL3001', '123', 'PARENT', N'Phụ huynh Lê Văn C1', NULL, 'A1NVL3001@gmail.com', N'Sẵn sàng'),
('A2NVL3002', '123', 'PARENT', N'Phụ huynh Lê Văn C2', NULL, 'A2NVL3002@gmail.com', N'Sẵn sàng'),
('A3NVL3003', '123', 'PARENT', N'Phụ huynh Lê Văn C3', NULL, 'A3NVL3003@gmail.com', N'Sẵn sàng'),
('A4NVL3004', '123', 'PARENT', N'Phụ huynh Lê Văn C4', NULL, 'A4NVL3004@gmail.com', N'Sẵn sàng'),
('A5NVL3005', '123', 'PARENT', N'Phụ huynh Lê Văn C5', NULL, 'A5NVL3005@gmail.com', N'Sẵn sàng'),
('A6NVL3006', '123', 'PARENT', N'Phụ huynh Lê Văn C6', NULL, 'A6NVL3006@gmail.com', N'Sẵn sàng'),
('A7NVL3007', '123', 'PARENT', N'Phụ huynh Lê Văn C7', NULL, 'A7NVL3007@gmail.com', N'Sẵn sàng'),
('A8NVL3008', '123', 'PARENT', N'Phụ huynh Lê Văn C8', NULL, 'A8NVL3008@gmail.com', N'Sẵn sàng'),
('A9NVL3009', '123', 'PARENT', N'Phụ huynh Lê Văn C9', NULL, 'A9NVL3009@gmail.com', N'Sẵn sàng'),
('A10NVL3010', '123', 'PARENT', N'Phụ huynh Lê Văn C10', NULL, 'A10NVL3010@gmail.com', N'Sẵn sàng'),
('A11NVL3011', '123', 'PARENT', N'Phụ huynh Lê Văn C11', NULL, 'A11NVL3011@gmail.com', N'Sẵn sàng'),
('A12NVL3012', '123', 'PARENT', N'Phụ huynh Lê Văn C12', NULL, 'A12NVL3012@gmail.com', N'Sẵn sàng'),
('A13NVL3013', '123', 'PARENT', N'Phụ huynh Lê Văn C13', NULL, 'A13NVL3013@gmail.com', N'Sẵn sàng'),
('A14NVL3014', '123', 'PARENT', N'Phụ huynh Lê Văn C14', NULL, 'A14NVL3014@gmail.com', N'Sẵn sàng'),
('A15NVL3015', '123', 'PARENT', N'Phụ huynh Lê Văn C15', NULL, 'A15NVL3015@gmail.com', N'Sẵn sàng'),
('A16NVL3016', '123', 'PARENT', N'Phụ huynh Lê Văn C16', NULL, 'A16NVL3016@gmail.com', N'Sẵn sàng'),
('A17NVL3017', '123', 'PARENT', N'Phụ huynh Lê Văn C17', NULL, 'A17NVL3017@gmail.com', N'Sẵn sàng'),
('A18NVL3018', '123', 'PARENT', N'Phụ huynh Lê Văn C18', NULL, 'A18NVL3018@gmail.com', N'Sẵn sàng'),
('A19NVL3019', '123', 'PARENT', N'Phụ huynh Lê Văn C19', NULL, 'A19NVL3019@gmail.com', N'Sẵn sàng'),
('A20NVL3020', '123', 'PARENT', N'Phụ huynh Lê Văn C20', NULL, 'A20NVL3020@gmail.com', N'Sẵn sàng'),
('A1NVL4001', '123', 'PARENT', N'Phụ huynh Phạm Thị D1', NULL, 'A1NVL4001@gmail.com', N'Sẵn sàng'),
('A2NVL4002', '123', 'PARENT', N'Phụ huynh Phạm Thị D2', NULL, 'A2NVL4002@gmail.com', N'Sẵn sàng'),
('A3NVL4003', '123', 'PARENT', N'Phụ huynh Phạm Thị D3', NULL, 'A3NVL4003@gmail.com', N'Sẵn sàng'),
('A4NVL4004', '123', 'PARENT', N'Phụ huynh Phạm Thị D4', NULL, 'A4NVL4004@gmail.com', N'Sẵn sàng'),
('A5NVL4005', '123', 'PARENT', N'Phụ huynh Phạm Thị D5', NULL, 'A5NVL4005@gmail.com', N'Sẵn sàng'),
('A6NVL4006', '123', 'PARENT', N'Phụ huynh Phạm Thị D6', NULL, 'A6NVL4006@gmail.com', N'Sẵn sàng'),
('A7NVL4007', '123', 'PARENT', N'Phụ huynh Phạm Thị D7', NULL, 'A7NVL4007@gmail.com', N'Sẵn sàng'),
('A8NVL4008', '123', 'PARENT', N'Phụ huynh Phạm Thị D8', NULL, 'A8NVL4008@gmail.com', N'Sẵn sàng'),
('A9NVL4009', '123', 'PARENT', N'Phụ huynh Phạm Thị D9', NULL, 'A9NVL4009@gmail.com', N'Sẵn sàng'),
('A10NVL4010', '123', 'PARENT', N'Phụ huynh Phạm Thị D10', NULL, 'A10NVL4010@gmail.com', N'Sẵn sàng'),
('A11NVL4011', '123', 'PARENT', N'Phụ huynh Phạm Thị D11', NULL, 'A11NVL4011@gmail.com', N'Sẵn sàng'),
('A12NVL4012', '123', 'PARENT', N'Phụ huynh Phạm Thị D12', NULL, 'A12NVL4012@gmail.com', N'Sẵn sàng'),
('A13NVL4013', '123', 'PARENT', N'Phụ huynh Phạm Thị D13', NULL, 'A13NVL4013@gmail.com', N'Sẵn sàng'),
('A14NVL4014', '123', 'PARENT', N'Phụ huynh Phạm Thị D14', NULL, 'A14NVL4014@gmail.com', N'Sẵn sàng'),
('A15NVL4015', '123', 'PARENT', N'Phụ huynh Phạm Thị D15', NULL, 'A15NVL4015@gmail.com', N'Sẵn sàng'),
('A16NVL4016', '123', 'PARENT', N'Phụ huynh Phạm Thị D16', NULL, 'A16NVL4016@gmail.com', N'Sẵn sàng'),
('A17NVL4017', '123', 'PARENT', N'Phụ huynh Phạm Thị D17', NULL, 'A17NVL4017@gmail.com', N'Sẵn sàng'),
('A18NVL4018', '123', 'PARENT', N'Phụ huynh Phạm Thị D18', NULL, 'A18NVL4018@gmail.com', N'Sẵn sàng'),
('A19NVL4019', '123', 'PARENT', N'Phụ huynh Phạm Thị D19', NULL, 'A19NVL4019@gmail.com', N'Sẵn sàng'),
('A20NVL4020', '123', 'PARENT', N'Phụ huynh Phạm Thị D20', NULL, 'A20NVL4020@gmail.com', N'Sẵn sàng'),
('A1NVL5001', '123', 'PARENT', N'Phụ huynh Hoàng Văn E1', NULL, 'A1NVL5001@gmail.com', N'Sẵn sàng'),
('A2NVL5002', '123', 'PARENT', N'Phụ huynh Hoàng Văn E2', NULL, 'A2NVL5002@gmail.com', N'Sẵn sàng'),
('A3NVL5003', '123', 'PARENT', N'Phụ huynh Hoàng Văn E3', NULL, 'A3NVL5003@gmail.com', N'Sẵn sàng'),
('A4NVL5004', '123', 'PARENT', N'Phụ huynh Hoàng Văn E4', NULL, 'A4NVL5004@gmail.com', N'Sẵn sàng'),
('A5NVL5005', '123', 'PARENT', N'Phụ huynh Hoàng Văn E5', NULL, 'A5NVL5005@gmail.com', N'Sẵn sàng'),
('A6NVL5006', '123', 'PARENT', N'Phụ huynh Hoàng Văn E6', NULL, 'A6NVL5006@gmail.com', N'Sẵn sàng'),
('A7NVL5007', '123', 'PARENT', N'Phụ huynh Hoàng Văn E7', NULL, 'A7NVL5007@gmail.com', N'Sẵn sàng'),
('A8NVL5008', '123', 'PARENT', N'Phụ huynh Hoàng Văn E8', NULL, 'A8NVL5008@gmail.com', N'Sẵn sàng'),
('A9NVL5009', '123', 'PARENT', N'Phụ huynh Hoàng Văn E9', NULL, 'A9NVL5009@gmail.com', N'Sẵn sàng'),
('A10NVL5010', '123', 'PARENT', N'Phụ huynh Hoàng Văn E10', NULL, 'A10NVL5010@gmail.com', N'Sẵn sàng'),
('A11NVL5011', '123', 'PARENT', N'Phụ huynh Hoàng Văn E11', NULL, 'A11NVL5011@gmail.com', N'Sẵn sàng'),
('A12NVL5012', '123', 'PARENT', N'Phụ huynh Hoàng Văn E12', NULL, 'A12NVL5012@gmail.com', N'Sẵn sàng'),
('A13NVL5013', '123', 'PARENT', N'Phụ huynh Hoàng Văn E13', NULL, 'A13NVL5013@gmail.com', N'Sẵn sàng'),
('A14NVL5014', '123', 'PARENT', N'Phụ huynh Hoàng Văn E14', NULL, 'A14NVL5014@gmail.com', N'Sẵn sàng'),
('A15NVL5015', '123', 'PARENT', N'Phụ huynh Hoàng Văn E15', NULL, 'A15NVL5015@gmail.com', N'Sẵn sàng'),
('A16NVL5016', '123', 'PARENT', N'Phụ huynh Hoàng Văn E16', NULL, 'A16NVL5016@gmail.com', N'Sẵn sàng'),
('A17NVL5017', '123', 'PARENT', N'Phụ huynh Hoàng Văn E17', NULL, 'A17NVL5017@gmail.com', N'Sẵn sàng'),
('A18NVL5018', '123', 'PARENT', N'Phụ huynh Hoàng Văn E18', NULL, 'A18NVL5018@gmail.com', N'Sẵn sàng'),
('A19NVL5019', '123', 'PARENT', N'Phụ huynh Hoàng Văn E19', NULL, 'A19NVL5019@gmail.com', N'Sẵn sàng'),
('A20NVL5020', '123', 'PARENT', N'Phụ huynh Hoàng Văn E20', NULL, 'A20NVL5020@gmail.com', N'Sẵn sàng');

-- 2. Insert Buses
INSERT INTO Buses (LicensePlate, Capacity, Status) VALUES
('29E-111.11', 7, N'Sẵn sàng'),
('29E-222.22', 9, N'Sẵn sàng'),
('29E-333.33', 7, N'Sẵn sàng'),
('29E-444.44', 9, N'Sẵn sàng'),
('29E-555.55', 7, N'Sẵn sàng'),
('29E-666.66', 9, N'Sẵn sàng'),
('29E-777.77', 7, N'Sẵn sàng'),
('29E-888.88', 9, N'Sẵn sàng'),
('29E-999.99', 7, N'Sẵn sàng'),
('29E-101.01', 9, N'Sẵn sàng');

-- 3. Insert Routes (Tất cả 6 tuyến xe)
INSERT INTO Routes (RouteCode, RouteName) VALUES
('LT1', N'Ocean Park -> LandMark 72 -> Trường học'),
('LT2', N'VinCom Long Biên -> Royal City -> Trường học'),
('LT3', N'Vincom Metropolis -> Phạm Hùng -> Trường học'),
('LT4', N'Minh Tảo -> 6th elements -> Trường học'),
('LT5', N'Ecohome 3 -> Vinhome gardenia -> Trường học'),
('LT6', N'Victory Văn Phú -> Matrix One -> Trường học');

-- 4. Insert Stops (Chỉ bao gồm các điểm đón học sinh và Trường học, bỏ qua các tuyến đường đi qua)
INSERT INTO Stops (StopName, Latitude, Longitude) VALUES
(N'S2.15 (Ocean Park)', 20.9910977, 105.9435884),          -- 1
(N'S1.08 (Ocean Park)', 20.9947239, 105.9431557),          -- 2
(N'The Zen Gamuda', 20.9716199, 105.8795939),              -- 3
(N'LandMark 72', 21.0173, 105.7841),                 -- 4
(N'Trường Marie Curie', 21.0165986, 105.7760220),          -- 5
(N'VinCom Long Biên', 21.0509278, 105.9163608),            -- 6
(N'H3 Chu Huy Mân', 21.0419908, 105.9044396),              -- 7
(N'423 Minh Khai', 20.9989958, 105.8664846),               -- 8
(N'VinCom TimesCity', 20.9954, 105.8675),            -- 9
(N'Royal City', 21.0028, 105.8155),                  -- 10
(N'Vincom Metropolis', 21.0317, 105.8143),           -- 11
(N'VinCom Nguyễn Chí Thanh', 21.0235360, 105.8088920),     -- 12
(N'N03-T1 Minh Tảo', 21.0654675, 105.7981705),             -- 13
(N'N01-T6', 21.0633291, 105.7971657),                      -- 14
(N'6th elements', 21.0513256, 105.7996255),                -- 15
(N'Tòa N02, Ecohome 3', 21.0772, 105.7831),          -- 16
(N'27A2 thành phố giao lưu', 21.0520662, 105.7804489),     -- 17
(N'R1 GoldMark City', 21.0435299, 105.7666960),            -- 18
(N'A1 Vinhome gardenia', 21.0360243, 105.7603859),         -- 19
(N'V3 Victory Văn Phú', 20.9591715, 105.7685205),          -- 20
(N'P2 ParkCity', 20.9638593, 105.7566201),                 -- 21
(N'C16 Gleximco', 20.9903249, 105.7383832),                -- 22
(N'A32 Gleximco', 21.0051280, 105.7330400),                -- 23
(N'GS1 SmartCity', 21.0055366, 105.7371635),               -- 24
(N'S401 SmartCity', 21.0062, 105.7451),              -- 25
(N'Mỹ Đình Pearl', 21.00662, 105.76888),               -- 26
(N'Matrix One', 21.0092, 105.7739);                  -- 27

-- 5. Insert RouteStops
-- Tuyến LT1
INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES
(1, 1, 1, '06:40', '17:50'),
(1, 2, 2, '06:50', '17:40'),
(1, 3, 3, '07:10', '17:20'),
(1, 4, 4, '07:40', '16:50'),
(1, 5, 5, '08:00', '16:30');

-- Tuyến LT2
INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES
(2, 6, 1, '06:40', '17:50'),
(2, 7, 2, '06:50', '17:40'),
(2, 8, 3, '07:05', '17:25'),
(2, 9, 4, '07:15', '17:15'),
(2, 10, 5, '07:40', '16:50'),
(2, 5, 6, '08:00', '16:30');

-- Tuyến LT3
INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES
(3, 11, 1, '07:15', '17:15'),
(3, 12, 2, '07:30', '17:00'),
(3, 5, 3, '08:00', '16:30');

-- Tuyến LT4
INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES
(4, 13, 1, '07:00', '17:30'),
(4, 14, 2, '07:15', '17:15'),
(4, 15, 3, '07:30', '17:00'),
(4, 5, 4, '08:00', '16:30');

-- Tuyến LT5
INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES
(5, 16, 1, '06:50', '17:40'),
(5, 17, 2, '07:10', '17:20'),
(5, 18, 3, '07:25', '17:05'),
(5, 19, 4, '07:40', '16:50'),
(5, 5, 5, '08:00', '16:30');

-- Tuyến LT6
INSERT INTO RouteStops (RouteID, StopID, StopOrder, EstimatedTime, ReturnTime) VALUES
(6, 20, 1, '07:00', '17:30'),
(6, 21, 2, '07:07', '17:23'),
(6, 22, 3, '07:15', '17:15'),
(6, 23, 4, '07:22', '17:08'),
(6, 24, 5, '07:30', '17:00'),
(6, 25, 6, '07:37', '16:53'),
(6, 26, 7, '07:45', '16:45'),
(6, 27, 8, '07:52', '16:38'),
(6, 5, 9, '08:00', '16:30');

-- 6. Insert HocSinh (100 học sinh, 20 hs mỗi lớp từ 1 đến 5)
INSERT INTO HocSinh (MaHocSinh, TenHocSinh, Lop, TenTK, MatKhau, Phone, Email, TrangThai) VALUES
('L1001', N'Nguyễn Văn A1', 1, 'A1NVL1001', '123', '090001001', 'A1NVL1001@gmail.com', N'Ngưng hoạt động'), ('L1002', N'Nguyễn Văn A2', 1, 'A2NVL1002', '123', '090001002', 'A2NVL1002@gmail.com', N'Ngưng hoạt động'), ('L1003', N'Nguyễn Văn A3', 1, 'A3NVL1003', '123', '090001003', 'A3NVL1003@gmail.com', N'Ngưng hoạt động'), ('L1004', N'Nguyễn Văn A4', 1, 'A4NVL1004', '123', '090001004', 'A4NVL1004@gmail.com', N'Ngưng hoạt động'), ('L1005', N'Nguyễn Văn A5', 1, 'A5NVL1005', '123', '090001005', 'A5NVL1005@gmail.com', N'Ngưng hoạt động'),
('L1006', N'Nguyễn Văn A6', 1, 'A6NVL1006', '123', '090001006', 'A6NVL1006@gmail.com', N'Ngưng hoạt động'), ('L1007', N'Nguyễn Văn A7', 1, 'A7NVL1007', '123', '090001007', 'A7NVL1007@gmail.com', N'Ngưng hoạt động'), ('L1008', N'Nguyễn Văn A8', 1, 'A8NVL1008', '123', '090001008', 'A8NVL1008@gmail.com', N'Ngưng hoạt động'), ('L1009', N'Nguyễn Văn A9', 1, 'A9NVL1009', '123', '090001009', 'A9NVL1009@gmail.com', N'Ngưng hoạt động'), ('L1010', N'Nguyễn Văn A10', 1, 'A10NVL1010', '123', '090001010', 'A10NVL1010@gmail.com', N'Ngưng hoạt động'),
('L1011', N'Nguyễn Văn A11', 1, 'A11NVL1011', '123', '090001011', 'A11NVL1011@gmail.com', N'Ngưng hoạt động'), ('L1012', N'Nguyễn Văn A12', 1, 'A12NVL1012', '123', '090001012', 'A12NVL1012@gmail.com', N'Ngưng hoạt động'), ('L1013', N'Nguyễn Văn A13', 1, 'A13NVL1013', '123', '090001013', 'A13NVL1013@gmail.com', N'Ngưng hoạt động'), ('L1014', N'Nguyễn Văn A14', 1, 'A14NVL1014', '123', '090001014', 'A14NVL1014@gmail.com', N'Ngưng hoạt động'), ('L1015', N'Nguyễn Văn A15', 1, 'A15NVL1015', '123', '090001015', 'A15NVL1015@gmail.com', N'Ngưng hoạt động'),
('L1016', N'Nguyễn Văn A16', 1, 'A16NVL1016', '123', '090001016', 'A16NVL1016@gmail.com', N'Ngưng hoạt động'), ('L1017', N'Nguyễn Văn A17', 1, 'A17NVL1017', '123', '090001017', 'A17NVL1017@gmail.com', N'Ngưng hoạt động'), ('L1018', N'Nguyễn Văn A18', 1, 'A18NVL1018', '123', '090001018', 'A18NVL1018@gmail.com', N'Ngưng hoạt động'), ('L1019', N'Nguyễn Văn A19', 1, 'A19NVL1019', '123', '090001019', 'A19NVL1019@gmail.com', N'Ngưng hoạt động'), ('L1020', N'Nguyễn Văn A20', 1, 'A20NVL1020', '123', '090001020', 'A20NVL1020@gmail.com', N'Ngưng hoạt động'),
('L2001', N'Trần Thị B1', 2, 'B1TTL2001', '123', '090002001', 'B1TTL2001@gmail.com', N'Ngưng hoạt động'), ('L2002', N'Trần Thị B2', 2, 'B2TTL2002', '123', '090002002', 'B2TTL2002@gmail.com', N'Ngưng hoạt động'), ('L2003', N'Trần Thị B3', 2, 'B3TTL2003', '123', '090002003', 'B3TTL2003@gmail.com', N'Ngưng hoạt động'), ('L2004', N'Trần Thị B4', 2, 'B4TTL2004', '123', '090002004', 'B4TTL2004@gmail.com', N'Ngưng hoạt động'), ('L2005', N'Trần Thị B5', 2, 'B5TTL2005', '123', '090002005', 'B5TTL2005@gmail.com', N'Ngưng hoạt động'),
('L2006', N'Trần Thị B6', 2, 'B6TTL2006', '123', '090002006', 'B6TTL2006@gmail.com', N'Ngưng hoạt động'), ('L2007', N'Trần Thị B7', 2, 'B7TTL2007', '123', '090002007', 'B7TTL2007@gmail.com', N'Ngưng hoạt động'), ('L2008', N'Trần Thị B8', 2, 'B8TTL2008', '123', '090002008', 'B8TTL2008@gmail.com', N'Ngưng hoạt động'), ('L2009', N'Trần Thị B9', 2, 'B9TTL2009', '123', '090002009', 'B9TTL2009@gmail.com', N'Ngưng hoạt động'), ('L2010', N'Trần Thị B10', 2, 'B10TTL2010', '123', '090002010', 'B10TTL2010@gmail.com', N'Ngưng hoạt động'),
('L2011', N'Trần Thị B11', 2, 'B11TTL2011', '123', '090002011', 'B11TTL2011@gmail.com', N'Ngưng hoạt động'), ('L2012', N'Trần Thị B12', 2, 'B12TTL2012', '123', '090002012', 'B12TTL2012@gmail.com', N'Ngưng hoạt động'), ('L2013', N'Trần Thị B13', 2, 'B13TTL2013', '123', '090002013', 'B13TTL2013@gmail.com', N'Ngưng hoạt động'), ('L2014', N'Trần Thị B14', 2, 'B14TTL2014', '123', '090002014', 'B14TTL2014@gmail.com', N'Ngưng hoạt động'), ('L2015', N'Trần Thị B15', 2, 'B15TTL2015', '123', '090002015', 'B15TTL2015@gmail.com', N'Ngưng hoạt động'),
('L2016', N'Trần Thị B16', 2, 'B16TTL2016', '123', '090002016', 'B16TTL2016@gmail.com', N'Ngưng hoạt động'), ('L2017', N'Trần Thị B17', 2, 'B17TTL2017', '123', '090002017', 'B17TTL2017@gmail.com', N'Ngưng hoạt động'), ('L2018', N'Trần Thị B18', 2, 'B18TTL2018', '123', '090002018', 'B18TTL2018@gmail.com', N'Ngưng hoạt động'), ('L2019', N'Trần Thị B19', 2, 'B19TTL2019', '123', '090002019', 'B19TTL2019@gmail.com', N'Ngưng hoạt động'), ('L2020', N'Trần Thị B20', 2, 'B20TTL2020', '123', '090002020', 'B20TTL2020@gmail.com', N'Ngưng hoạt động'),
('L3001', N'Lê Văn C1', 3, 'C1LVL3001', '123', '090003001', 'C1LVL3001@gmail.com', N'Ngưng hoạt động'), ('L3002', N'Lê Văn C2', 3, 'C2LVL3002', '123', '090003002', 'C2LVL3002@gmail.com', N'Ngưng hoạt động'), ('L3003', N'Lê Văn C3', 3, 'C3LVL3003', '123', '090003003', 'C3LVL3003@gmail.com', N'Ngưng hoạt động'), ('L3004', N'Lê Văn C4', 3, 'C4LVL3004', '123', '090003004', 'C4LVL3004@gmail.com', N'Ngưng hoạt động'), ('L3005', N'Lê Văn C5', 3, 'C5LVL3005', '123', '090003005', 'C5LVL3005@gmail.com', N'Ngưng hoạt động'),
('L3006', N'Lê Văn C6', 3, 'C6LVL3006', '123', '090003006', 'C6LVL3006@gmail.com', N'Ngưng hoạt động'), ('L3007', N'Lê Văn C7', 3, 'C7LVL3007', '123', '090003007', 'C7LVL3007@gmail.com', N'Ngưng hoạt động'), ('L3008', N'Lê Văn C8', 3, 'C8LVL3008', '123', '090003008', 'C8LVL3008@gmail.com', N'Ngưng hoạt động'), ('L3009', N'Lê Văn C9', 3, 'C9LVL3009', '123', '090003009', 'C9LVL3009@gmail.com', N'Ngưng hoạt động'), ('L3010', N'Lê Văn C10', 3, 'C10LVL3010', '123', '090003010', 'C10LVL3010@gmail.com', N'Ngưng hoạt động'),
('L3011', N'Lê Văn C11', 3, 'C11LVL3011', '123', '090003011', 'C11LVL3011@gmail.com', N'Ngưng hoạt động'), ('L3012', N'Lê Văn C12', 3, 'C12LVL3012', '123', '090003012', 'C12LVL3012@gmail.com', N'Ngưng hoạt động'), ('L3013', N'Lê Văn C13', 3, 'C13LVL3013', '123', '090003013', 'C13LVL3013@gmail.com', N'Ngưng hoạt động'), ('L3014', N'Lê Văn C14', 3, 'C14LVL3014', '123', '090003014', 'C14LVL3014@gmail.com', N'Ngưng hoạt động'), ('L3015', N'Lê Văn C15', 3, 'C15LVL3015', '123', '090003015', 'C15LVL3015@gmail.com', N'Ngưng hoạt động'),
('L3016', N'Lê Văn C16', 3, 'C16LVL3016', '123', '090003016', 'C16LVL3016@gmail.com', N'Ngưng hoạt động'), ('L3017', N'Lê Văn C17', 3, 'C17LVL3017', '123', '090003017', 'C17LVL3017@gmail.com', N'Ngưng hoạt động'), ('L3018', N'Lê Văn C18', 3, 'C18LVL3018', '123', '090003018', 'C18LVL3018@gmail.com', N'Ngưng hoạt động'), ('L3019', N'Lê Văn C19', 3, 'C19LVL3019', '123', '090003019', 'C19LVL3019@gmail.com', N'Ngưng hoạt động'), ('L3020', N'Lê Văn C20', 3, 'C20LVL3020', '123', '090003020', 'C20LVL3020@gmail.com', N'Ngưng hoạt động'),
('L4001', N'Phạm Thị D1', 4, 'D1PTL4001', '123', '090004001', 'D1PTL4001@gmail.com', N'Ngưng hoạt động'), ('L4002', N'Phạm Thị D2', 4, 'D2PTL4002', '123', '090004002', 'D2PTL4002@gmail.com', N'Ngưng hoạt động'), ('L4003', N'Phạm Thị D3', 4, 'D3PTL4003', '123', '090004003', 'D3PTL4003@gmail.com', N'Ngưng hoạt động'), ('L4004', N'Phạm Thị D4', 4, 'D4PTL4004', '123', '090004004', 'D4PTL4004@gmail.com', N'Ngưng hoạt động'), ('L4005', N'Phạm Thị D5', 4, 'D5PTL4005', '123', '090004005', 'D5PTL4005@gmail.com', N'Ngưng hoạt động'),
('L4006', N'Phạm Thị D6', 4, 'D6PTL4006', '123', '090004006', 'D6PTL4006@gmail.com', N'Ngưng hoạt động'), ('L4007', N'Phạm Thị D7', 4, 'D7PTL4007', '123', '090004007', 'D7PTL4007@gmail.com', N'Ngưng hoạt động'), ('L4008', N'Phạm Thị D8', 4, 'D8PTL4008', '123', '090004008', 'D8PTL4008@gmail.com', N'Ngưng hoạt động'), ('L4009', N'Phạm Thị D9', 4, 'D9PTL4009', '123', '090004009', 'D9PTL4009@gmail.com', N'Ngưng hoạt động'), ('L4010', N'Phạm Thị D10', 4, 'D10PTL4010', '123', '090004010', 'D10PTL4010@gmail.com', N'Ngưng hoạt động'),
('L4011', N'Phạm Thị D11', 4, 'D11PTL4011', '123', '090004011', 'D11PTL4011@gmail.com', N'Ngưng hoạt động'), ('L4012', N'Phạm Thị D12', 4, 'D12PTL4012', '123', '090004012', 'D12PTL4012@gmail.com', N'Ngưng hoạt động'), ('L4013', N'Phạm Thị D13', 4, 'D13PTL4013', '123', '090004013', 'D13PTL4013@gmail.com', N'Ngưng hoạt động'), ('L4014', N'Phạm Thị D14', 4, 'D14PTL4014', '123', '090004014', 'D14PTL4014@gmail.com', N'Ngưng hoạt động'), ('L4015', N'Phạm Thị D15', 4, 'D15PTL4015', '123', '090004015', 'D15PTL4015@gmail.com', N'Ngưng hoạt động'),
('L4016', N'Phạm Thị D16', 4, 'D16PTL4016', '123', '090004016', 'D16PTL4016@gmail.com', N'Ngưng hoạt động'), ('L4017', N'Phạm Thị D17', 4, 'D17PTL4017', '123', '090004017', 'D17PTL4017@gmail.com', N'Ngưng hoạt động'), ('L4018', N'Phạm Thị D18', 4, 'D18PTL4018', '123', '090004018', 'D18PTL4018@gmail.com', N'Ngưng hoạt động'), ('L4019', N'Phạm Thị D19', 4, 'D19PTL4019', '123', '090004019', 'D19PTL4019@gmail.com', N'Ngưng hoạt động'), ('L4020', N'Phạm Thị D20', 4, 'D20PTL4020', '123', '090004020', 'D20PTL4020@gmail.com', N'Ngưng hoạt động'),
('L5001', N'Hoàng Văn E1', 5, 'E1HVL5001', '123', '090005001', 'E1HVL5001@gmail.com', N'Ngưng hoạt động'), ('L5002', N'Hoàng Văn E2', 5, 'E2HVL5002', '123', '090005002', 'E2HVL5002@gmail.com', N'Ngưng hoạt động'), ('L5003', N'Hoàng Văn E3', 5, 'E3HVL5003', '123', '090005003', 'E3HVL5003@gmail.com', N'Ngưng hoạt động'), ('L5004', N'Hoàng Văn E4', 5, 'E4HVL5004', '123', '090005004', 'E4HVL5004@gmail.com', N'Ngưng hoạt động'), ('L5005', N'Hoàng Văn E5', 5, 'E5HVL5005', '123', '090005005', 'E5HVL5005@gmail.com', N'Ngưng hoạt động'),
('L5006', N'Hoàng Văn E6', 5, 'E6HVL5006', '123', '090005006', 'E6HVL5006@gmail.com', N'Ngưng hoạt động'), ('L5007', N'Hoàng Văn E7', 5, 'E7HVL5007', '123', '090005007', 'E7HVL5007@gmail.com', N'Ngưng hoạt động'), ('L5008', N'Hoàng Văn E8', 5, 'E8HVL5008', '123', '090005008', 'E8HVL5008@gmail.com', N'Ngưng hoạt động'), ('L5009', N'Hoàng Văn E9', 5, 'E9HVL5009', '123', '090005009', 'E9HVL5009@gmail.com', N'Ngưng hoạt động'), ('L5010', N'Hoàng Văn E10', 5, 'E10HVL5010', '123', '090005010', 'E10HVL5010@gmail.com', N'Ngưng hoạt động'),
('L5011', N'Hoàng Văn E11', 5, 'E11HVL5011', '123', '090005011', 'E11HVL5011@gmail.com', N'Ngưng hoạt động'), ('L5012', N'Hoàng Văn E12', 5, 'E12HVL5012', '123', '090005012', 'E12HVL5012@gmail.com', N'Ngưng hoạt động'), ('L5013', N'Hoàng Văn E13', 5, 'E13HVL5013', '123', '090005013', 'E13HVL5013@gmail.com', N'Ngưng hoạt động'), ('L5014', N'Hoàng Văn E14', 5, 'E14HVL5014', '123', '090005014', 'E14HVL5014@gmail.com', N'Ngưng hoạt động'), ('L5015', N'Hoàng Văn E15', 5, 'E15HVL5015', '123', '090005015', 'E15HVL5015@gmail.com', N'Ngưng hoạt động'),
('L5016', N'Hoàng Văn E16', 5, 'E16HVL5016', '123', '090005016', 'E16HVL5016@gmail.com', N'Ngưng hoạt động'), ('L5017', N'Hoàng Văn E17', 5, 'E17HVL5017', '123', '090005017', 'E17HVL5017@gmail.com', N'Ngưng hoạt động'), ('L5018', N'Hoàng Văn E18', 5, 'E18HVL5018', '123', '090005018', 'E18HVL5018@gmail.com', N'Ngưng hoạt động'), ('L5019', N'Hoàng Văn E19', 5, 'E19HVL5019', '123', '090005019', 'E19HVL5019@gmail.com', N'Ngưng hoạt động'), ('L5020', N'Hoàng Văn E20', 5, 'E20HVL5020', '123', '090005020', 'E20HVL5020@gmail.com', N'Ngưng hoạt động');




GO
PRINT 'Database Setup Completed successfully.';







