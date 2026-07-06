package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.HocSinh;

public class HocSinhDAO extends DBContext {

    // 1. Lấy toàn bộ danh sách học sinh (Admin dùng)
    public List<HocSinh> getAllHocSinh() {
        List<String> list = new ArrayList<>(); // Giả định dùng DBContext chung của nhóm
        List<HocSinh> listHS = new ArrayList<>();
        String sql = "SELECT * FROM HocSinh";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                HocSinh h = new HocSinh(
                    rs.getString("MaHocSinh"),
                    rs.getNString("TenHocSinh"),
                    rs.getInt("Lop"),
                    rs.getString("TenTK"),
                    rs.getString("MatKhau"),
                    rs.getString("Email"),
                    rs.getInt("DefaultStopID"),
                    rs.getInt("DefaultRouteID"),
                    rs.getNString("TrangThai")
                );
                listHS.add(h);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return listHS;
    }

    // 2. Thêm mới học sinh - Mặc định trạng thái Ngưng hoạt động
    public void insertHocSinh(HocSinh h) {
        String sql = "INSERT INTO HocSinh (MaHocSinh, TenHocSinh, Lop, TenTK, MatKhau, Email, DefaultStopID, DefaultRouteID, TrangThai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, NULL, NULL, N'Ngưng hoạt động')";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, h.getMaHocSinh());
            st.setNString(2, h.getTenHocSinh());
            st.setInt(3, h.getLop());
            st.setString(4, h.getTenTK());
            st.setString(5, h.getMatKhau());
            st.setString(6, h.getEmail());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // 3. Cập nhật thông tin học sinh cơ bản
    public void updateHocSinh(HocSinh h) {
        String sql = "UPDATE HocSinh SET TenHocSinh=?, Lop=?, Email=? WHERE MaHocSinh=?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setNString(1, h.getTenHocSinh());
            st.setInt(2, h.getLop());
            st.setString(3, h.getEmail());
            st.setString(4, h.getMaHocSinh());
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // 4. Xóa học sinh
    public void deleteHocSinh(String maHocSinh) {
        String sql = "DELETE FROM HocSinh WHERE MaHocSinh=?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // 5. Tìm học sinh theo tài khoản Phụ huynh (TenTK)
    public List<HocSinh> getHocSinhByParent(String tenTK) {
        List<HocSinh> list = new ArrayList<>();
        String sql = "SELECT * FROM HocSinh WHERE TenTK = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, tenTK);
            st.setString(1, tenTK);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(new HocSinh(
                    rs.getString("MaHocSinh"),
                    rs.getNString("TenHocSinh"),
                    rs.getInt("Lop"),
                    rs.getString("TenTK"),
                    rs.getString("MatKhau"),
                    rs.getString("Email"),
                    rs.getInt("DefaultStopID"),
                    rs.getInt("DefaultRouteID"),
                    rs.getNString("TrangThai")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return list;
    }

    // 6. Nghiệp vụ: Phụ huynh chọn điểm đón -> Đổi trạng thái sang "Hoạt động"
    public void activateService(String maHocSinh, int stopID, int routeID) {
        String sql = "UPDATE HocSinh SET DefaultStopID = ?, DefaultRouteID = ?, TrangThai = N'Hoạt động' WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, stopID);
            st.setInt(2, routeID);
            st.setString(3, maHocSinh);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // 7. Nghiệp vụ: Ngưng dịch vụ
    public void stopService(String maHocSinh) {
        String sql = "UPDATE HocSinh SET TrangThai = N'Ngưng hoạt động' WHERE MaHocSinh = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, maHocSinh);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}