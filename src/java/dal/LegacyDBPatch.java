package dal;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class LegacyDBPatch {
    private static boolean dbPatched = false;

    public static void checkAndPatchDatabase(Connection connection) {
        if (dbPatched || connection == null) return;
        synchronized(LegacyDBPatch.class) {
            if (dbPatched) return;
            try (Statement st = connection.createStatement()) {
                // 1. Check/Add columns to HocSinh
                try { st.execute("SELECT PendingStopID FROM HocSinh"); } catch (SQLException e) {
                    st.execute("ALTER TABLE HocSinh ADD PendingStopID INT NULL FOREIGN KEY REFERENCES Stops(StopID)");
                }
                try { st.execute("SELECT PendingRouteID FROM HocSinh"); } catch (SQLException e) {
                    st.execute("ALTER TABLE HocSinh ADD PendingRouteID INT NULL FOREIGN KEY REFERENCES Routes(RouteID)");
                }
                try { st.execute("SELECT EffectiveDate FROM HocSinh"); } catch (SQLException e) {
                    st.execute("ALTER TABLE HocSinh ADD EffectiveDate DATE NULL");
                }

                // 2. Check/Add columns to Schedules
                try { st.execute("SELECT IncidentStatus FROM Schedules"); } catch (SQLException e) {
                    st.execute("ALTER TABLE Schedules ADD IncidentStatus VARCHAR(20) DEFAULT 'NORMAL'");
                }
                try { st.execute("SELECT ReplacementBusID FROM Schedules"); } catch (SQLException e) {
                    st.execute("ALTER TABLE Schedules ADD ReplacementBusID INT NULL FOREIGN KEY REFERENCES Buses(BusID)");
                }
                try { st.execute("SELECT HandlingTechID FROM Schedules"); } catch (SQLException e) {
                    st.execute("ALTER TABLE Schedules ADD HandlingTechID INT NULL FOREIGN KEY REFERENCES Users(UserID)");
                }

                // 3. Check/Create TechnicianSchedules
                st.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='TechnicianSchedules' AND xtype='U') " +
                           "CREATE TABLE TechnicianSchedules (" +
                           "    TechScheduleID INT IDENTITY(1,1) PRIMARY KEY," +
                           "    TechnicianID INT NOT NULL FOREIGN KEY REFERENCES Users(UserID)," +
                           "    Date DATE NOT NULL," +
                           "    CreatedAt DATETIME DEFAULT GETDATE()," +
                           "    Status VARCHAR(20) DEFAULT 'PENDING'," +
                           "    CONSTRAINT UC_TechnicianDate UNIQUE (TechnicianID, Date)" +
                           ")");

                // 4. Check/Create Notifications
                st.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Notifications' AND xtype='U') " +
                           "CREATE TABLE Notifications (" +
                           "    NotifID INT IDENTITY(1,1) PRIMARY KEY," +
                           "    Username VARCHAR(50) NOT NULL," +
                           "    Message NVARCHAR(1000) NOT NULL," +
                           "    CreatedAt DATETIME DEFAULT GETDATE()," +
                           "    IsRead BIT DEFAULT 0" +
                           ")");

                // 5. Check/Create ScheduleProgress
                st.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ScheduleProgress' AND xtype='U') " +
                           "CREATE TABLE ScheduleProgress (" +
                           "    ProgressID INT IDENTITY(1,1) PRIMARY KEY," +
                           "    ScheduleID INT FOREIGN KEY REFERENCES Schedules(ScheduleID)," +
                           "    StopID INT," +
                           "    ArrivalTime DATETIME NOT NULL" +
                           ")");

                // 6. Check/Create BusMaintenances
                st.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='BusMaintenances' AND xtype='U') " +
                           "CREATE TABLE BusMaintenances (" +
                           "    MaintenanceID INT IDENTITY(1,1) PRIMARY KEY," +
                           "    BusID INT FOREIGN KEY REFERENCES Buses(BusID)," +
                           "    MaintenanceDate DATE NOT NULL," +
                           "    Description NVARCHAR(255)," +
                           "    CreatedAt DATETIME DEFAULT GETDATE()" +
                           ")");

                dbPatched = true;
                System.out.println("Auto-patched database structure successfully checked/updated.");
            } catch (SQLException e) {
                System.out.println("Error auto-patching database: " + e.getMessage());
            }
        }
    }
}
