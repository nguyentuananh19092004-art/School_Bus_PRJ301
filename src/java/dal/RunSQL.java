package dal;

import java.sql.Connection;
import java.sql.Statement;

public class RunSQL {
    public static void main(String[] args) {
        try {
            DBContext db = new DBContext() {};
            Connection conn = db.connection;
            Statement st = conn.createStatement();
            
            // Create TechnicianSchedules table
            String[] updates = {
                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='TechnicianSchedules' and xtype='U') " +
                "CREATE TABLE TechnicianSchedules (" +
                "    TechScheduleID INT IDENTITY(1,1) PRIMARY KEY," +
                "    TechnicianID INT NOT NULL," +
                "    Date DATE NOT NULL," +
                "    CreatedAt DATETIME DEFAULT GETDATE()," +
                "    Status VARCHAR(20) DEFAULT 'PENDING'," +
                "    FOREIGN KEY (TechnicianID) REFERENCES Users(UserID)" +
                ") ELSE " +
                "BEGIN " +
                "    IF COL_LENGTH('TechnicianSchedules', 'Status') IS NULL " +
                "    ALTER TABLE TechnicianSchedules ADD Status VARCHAR(20) DEFAULT 'PENDING' " +
                "END",
                "IF COL_LENGTH('Schedules', 'ReplacementBusID') IS NULL " +
                "ALTER TABLE Schedules ADD ReplacementBusID INT NULL",
                "IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_Schedules_ReplacementBus') " +
                "ALTER TABLE Schedules ADD CONSTRAINT FK_Schedules_ReplacementBus FOREIGN KEY (ReplacementBusID) REFERENCES Buses(BusID)"
            };
            
            for (String sql : updates) {
                st.execute(sql);
            }
            System.out.println("Created TechnicianSchedules table.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
