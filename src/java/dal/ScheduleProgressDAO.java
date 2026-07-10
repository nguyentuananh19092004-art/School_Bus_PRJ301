package dal;

import model.ScheduleProgress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScheduleProgressDAO extends DBContext {

    public void insertProgress(int scheduleID, int stopID) {
        String sql = "INSERT INTO ScheduleProgress (ScheduleID, StopID, ArrivalTime) VALUES (?, ?, GETDATE())";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, scheduleID);
            ps.setInt(2, stopID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ScheduleProgress> getProgressBySchedule(int scheduleID) {
        List<ScheduleProgress> list = new ArrayList<>();
        String sql = "SELECT * FROM ScheduleProgress WHERE ScheduleID = ? ORDER BY ArrivalTime DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, scheduleID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScheduleProgress p = new ScheduleProgress(
                            rs.getInt("ProgressID"),
                            rs.getInt("ScheduleID"),
                            rs.getInt("StopID"),
                            rs.getTimestamp("ArrivalTime")
                    );
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
