package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.HocSinh;

/**
 * DAO class quản lý dữ liệu Học sinh.
 * Xử lý thông tin cá nhân, đăng ký tuyến/điểm đón, và các thay đổi lịch trình theo thời gian (EffectiveDate).
 */
public class HocSinhDAO extends DBContext {

    /**
     * Hàm phụ trợ kiểm tra xem ResultSet hiện tại có chứa một cột cụ thể hay không.
     * Tránh lỗi khi gọi rs.getString() trên cột không tồn tại.
     */
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException sqlex) {
            return false;
        }
    }

    /**
     * Ánh xạ dữ liệu từ ResultSet sang đối tượng HocSinh.
     */
    private HocSinh mapRow(ResultSet rs) throws SQLException {
        HocSinh hs = new HocSinh();
        hs.setMaHocSinh(rs.getString("MaHocSinh"));
        hs.setTenHocSinh(rs.getString("TenHocSinh"));
        hs.setLop(rs.getInt("Lop"));
        hs.setTenTK(rs.getString("TenTK"));
        hs.setMatKhau(rs.getString("MatKhau"));
        
        if (hasColumn(rs, "DefaultStopID") && rs.getObject("DefaultStopID") != null) hs.setDefaultStopID(rs.getInt("DefaultStopID"));
        if (hasColumn(rs, "DefaultRouteID") && rs.getObject("DefaultRouteID") != null) hs.setDefaultRouteID(rs.getInt("DefaultRouteID"));
        if (hasColumn(rs, "TrangThai")) hs.setTrangThai(rs.getString("TrangThai"));
        if (hasColumn(rs, "Email")) hs.setEmail(rs.getString("Email"));
        
        if (hasColumn(rs, "PendingStopID") && rs.getObject("PendingStopID") != null) hs.setPendingStopID(rs.getInt("PendingStopID"));
        if (hasColumn(rs, "PendingRouteID") && rs.getObject("PendingRouteID") != null) hs.setPendingRouteID(rs.getInt("PendingRouteID"));
        if (hasColumn(rs, "EffectiveDate")) hs.setEffectiveDate(rs.getDate("EffectiveDate"));
        
        return hs;
    }

    /**
     * Lấy toàn bộ danh sách học sinh (trừ những học sinh đã bị xóa).
     * Sẽ gọi tự động applyPendingStopChanges() để cập nhật dữ liệu mới nhất.
     */
    public List<HocSinh> getAllHocSinh() {
        applyPendingStopChanges();
        List<HocSinh> list = new ArrayList<>();
        String sql = "SELECT * FROM HocSinh WHERE TrangThai != N'Đã xóa' OR TrangThai IS NULL";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    /**
     * Lấy thông tin học sinh dựa trên Mã Học Sinh.
     */
    public HocSinh getHocSinhByMa(String maHocSinh) {
        applyPendingStopChanges();
        String sql = "SELECT * FROM HocSinh WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    public HocSinh getHocSinhByTenTK(String tenTK) {
        applyPendingStopChanges();
        String sql = "SELECT * FROM HocSinh WHERE TenTK = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, tenTK);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }

    public List<HocSinh> getHocSinhByStopID(int stopID) {
        applyPendingStopChanges();
        List<HocSinh> list = new ArrayList<>();
        String sql = "SELECT * FROM HocSinh WHERE DefaultStopID = ? AND (TrangThai IS NULL OR TrangThai != N'Đã xóa')";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, stopID);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    public void insertHocSinh(HocSinh hs) {
        String sql = "INSERT INTO HocSinh (MaHocSinh, TenHocSinh, Lop, TenTK, MatKhau, DefaultStopID, DefaultRouteID, TrangThai, Email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, hs.getMaHocSinh());
            st.setString(2, hs.getTenHocSinh());
            st.setInt(3, hs.getLop());
            st.setString(4, hs.getTenTK());
            st.setString(5, hs.getMatKhau());
            if (hs.getDefaultStopID() != null) {
                st.setInt(6, hs.getDefaultStopID());
            } else {
                st.setNull(6, java.sql.Types.INTEGER);
            }
            if (hs.getDefaultRouteID() != null) {
                st.setInt(7, hs.getDefaultRouteID());
            } else {
                st.setNull(7, java.sql.Types.INTEGER);
            }
            st.setString(8, hs.getTrangThai());
            st.setString(9, hs.getEmail());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void updateHocSinh(HocSinh hs) {
        String sql = "UPDATE HocSinh SET TenHocSinh = ?, Lop = ?, TenTK = ?, MatKhau = ?, DefaultStopID = ?, DefaultRouteID = ?, TrangThai = ?, Email = ? WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, hs.getTenHocSinh());
            st.setInt(2, hs.getLop());
            st.setString(3, hs.getTenTK());
            st.setString(4, hs.getMatKhau());
            if (hs.getDefaultStopID() != null) {
                st.setInt(5, hs.getDefaultStopID());
            } else {
                st.setNull(5, java.sql.Types.INTEGER);
            }
            if (hs.getDefaultRouteID() != null) {
                st.setInt(6, hs.getDefaultRouteID());
            } else {
                st.setNull(6, java.sql.Types.INTEGER);
            }
            st.setString(7, hs.getTrangThai());
            st.setString(8, hs.getEmail());
            st.setString(9, hs.getMaHocSinh());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Hủy đăng ký xe bus của học sinh (chuyển sang trạng thái ngưng hoạt động), 
     * có hiệu lực bắt đầu từ một ngày cụ thể trong tương lai (EffectiveDate).
     */
    public void stopService(String maHocSinh, java.sql.Date effectiveDate) {
        String sql = "UPDATE HocSinh SET PendingStopID = NULL, PendingRouteID = NULL, EffectiveDate = ? WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDate(1, effectiveDate);
            st.setString(2, maHocSinh);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Hủy đăng ký xe bus của học sinh ngay lập tức.
     */
    public void stopService(String maHocSinh) {
        String sql = "UPDATE HocSinh SET TrangThai = N'Ngưng hoạt động', DefaultStopID = NULL, DefaultRouteID = NULL, PendingStopID = NULL, PendingRouteID = NULL, EffectiveDate = NULL WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void deleteHocSinh(String maHocSinh) {
        String sql = "UPDATE HocSinh SET TrangThai = N'Đã xóa', TenTK = CONCAT(LEFT(TenTK, 25), '_d', MaHocSinh) WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public boolean checkLogin(String tenTK, String matKhau) {
        String sql = "SELECT * FROM HocSinh WHERE TenTK = ? AND MatKhau = ? AND (TrangThai IS NULL OR TrangThai != N'Đã xóa')";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, tenTK);
            st.setString(2, matKhau);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    /**
     * Đếm tổng số lượng học sinh đang sử dụng dịch vụ trên một tuyến đường hiện tại.
     */
    public int countActiveHocSinhByRoute(int routeID) {
        String sql = "SELECT COUNT(*) FROM HocSinh WHERE DefaultRouteID = ? AND TrangThai = N'Sử dụng'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, routeID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return 0;
    }

    /**
     * Đếm số lượng học sinh dự kiến sử dụng dịch vụ trên một tuyến đường vào một ngày cụ thể.
     * Xử lý cả logic học sinh chuyển tuyến trong tương lai dựa trên EffectiveDate.
     */
    public int countActiveHocSinhByRoute(int routeID, java.sql.Date targetDate) {
        String sql = "SELECT COUNT(*) FROM HocSinh WHERE "
                + "( (EffectiveDate IS NOT NULL AND EffectiveDate <= ? AND PendingRouteID = ?) "
                + "OR ((EffectiveDate IS NULL OR EffectiveDate > ?) AND DefaultRouteID = ? AND TrangThai = N'Sử dụng') ) "
                + "AND (TrangThai IS NULL OR TrangThai != N'Đã xóa')";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDate(1, targetDate);
            st.setInt(2, routeID);
            st.setDate(3, targetDate);
            st.setInt(4, routeID);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return 0;
    }

    /**
     * Ghi nhận một yêu cầu thay đổi điểm đón/tuyến đường của học sinh,
     * sẽ chính thức có hiệu lực từ ngày (EffectiveDate) được chỉ định.
     */
    public void setPendingStopChange(String maHocSinh, int newStopID, int newRouteID, java.sql.Date effectiveDate) {
        String sql = "UPDATE HocSinh SET PendingStopID = ?, PendingRouteID = ?, EffectiveDate = ?, TrangThai = N'Sử dụng' WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, newStopID);
            st.setInt(2, newRouteID);
            st.setDate(3, effectiveDate);
            st.setString(4, maHocSinh);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    /**
     * Tự động duyệt qua bảng HocSinh và cập nhật các thay đổi điểm đón/tuyến đường
     * mà ngày hiệu lực (EffectiveDate) đã đến (<= ngày hiện tại).
     * Hàm này thường được gọi ẩn trước các thao tác truy xuất dữ liệu (GET).
     */
    public void applyPendingStopChanges() {
        String sql = "UPDATE HocSinh SET "
                + "TrangThai = CASE WHEN PendingRouteID IS NULL THEN N'Ngưng hoạt động' ELSE N'Sử dụng' END, "
                + "DefaultStopID = CASE WHEN PendingRouteID IS NULL THEN NULL ELSE PendingStopID END, "
                + "DefaultRouteID = CASE WHEN PendingRouteID IS NULL THEN NULL ELSE PendingRouteID END, "
                + "PendingStopID = NULL, PendingRouteID = NULL, EffectiveDate = NULL "
                + "WHERE EffectiveDate IS NOT NULL AND EffectiveDate <= CAST(GETDATE() AS DATE)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public boolean updatePassword(String maHocSinh, String newPassword) {
        String sql = "UPDATE HocSinh SET MatKhau = ? WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, newPassword);
            st.setString(2, maHocSinh);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }

    public boolean checkEmailExist(String email, String excludeMaHocSinh) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 WHERE EXISTS (SELECT 1 FROM HocSinh WHERE Email = ? AND MaHocSinh != ?) OR EXISTS (SELECT 1 FROM Users WHERE Email = ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, email);
            st.setString(2, excludeMaHocSinh != null ? excludeMaHocSinh : "");
            st.setString(3, email);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return false;
    }
}
