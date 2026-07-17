package model;

public class HocSinh {
    private String maHocSinh;
    private String tenHocSinh;
    private int lop;
    private String tenTK;
    private String matKhau;
    private Integer defaultStopID;
    private Integer defaultRouteID;
    private String trangThai;
    private String email;
    private String phone;
    
    // Pending fields for changing stop
    private Integer pendingStopID;
    private Integer pendingRouteID;
    private java.sql.Date effectiveDate;

    public HocSinh() {
    }

    public HocSinh(String maHocSinh, String tenHocSinh, int lop, String tenTK, String matKhau, Integer defaultStopID, Integer defaultRouteID, String trangThai, String email, String phone) {
        this.maHocSinh = maHocSinh;
        this.tenHocSinh = tenHocSinh;
        this.lop = lop;
        this.tenTK = tenTK;
        this.matKhau = matKhau;
        this.defaultStopID = defaultStopID;
        this.defaultRouteID = defaultRouteID;
        this.trangThai = trangThai;
        this.email = email;
        this.phone = phone;
    }

    public String getMaHocSinh() {
        return maHocSinh;
    }

    public void setMaHocSinh(String maHocSinh) {
        this.maHocSinh = maHocSinh;
    }

    public String getTenHocSinh() {
        return tenHocSinh;
    }

    public void setTenHocSinh(String tenHocSinh) {
        this.tenHocSinh = tenHocSinh;
    }

    public int getLop() {
        return lop;
    }

    public void setLop(int lop) {
        this.lop = lop;
    }

    public String getTenTK() {
        return tenTK;
    }

    public void setTenTK(String tenTK) {
        this.tenTK = tenTK;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public Integer getDefaultStopID() {
        return defaultStopID;
    }

    public void setDefaultStopID(Integer defaultStopID) {
        this.defaultStopID = defaultStopID;
    }

    public Integer getDefaultRouteID() {
        return defaultRouteID;
    }

    public void setDefaultRouteID(Integer defaultRouteID) {
        this.defaultRouteID = defaultRouteID;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getPendingStopID() {
        return pendingStopID;
    }

    public void setPendingStopID(Integer pendingStopID) {
        this.pendingStopID = pendingStopID;
    }

    public Integer getPendingRouteID() {
        return pendingRouteID;
    }

    public void setPendingRouteID(Integer pendingRouteID) {
        this.pendingRouteID = pendingRouteID;
    }

    public java.sql.Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(java.sql.Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
