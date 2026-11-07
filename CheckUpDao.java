package com.mycompany.healthinformationsystem.CheckUp;
import com.mycompany.healthinformationsystem.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckUpDao {

    // ---------- Check-up counts ----------

    public int getTotalCheckups() {
        return count("SELECT COUNT(*) FROM check_ups");
    }

    public int getCheckupsToday() {
        return count("SELECT COUNT(*) FROM check_ups WHERE DATE(checkup_date) = CURDATE()");
    }

    public int getCheckupsByBmiClass(String bmiClassification) {
        return count("SELECT COUNT(*) FROM check_ups WHERE bmi_classification = ?", bmiClassification);
    }

    public int getCheckupsByResident(int residentId) {
        return count("SELECT COUNT(*) FROM check_ups WHERE resident_id = ?", residentId);
    }

    public int getCheckupsByWorker(int workerId) {
        return count("SELECT COUNT(*) FROM check_ups WHERE worker_id = ?", workerId);
    }

    // ---------- Helpers ----------

    private int count(String sql) {
        try (Connection conn = DB.getConnection();
             Statement stat = conn.createStatement();
             ResultSet rs = stat.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private int count(String sql, Object param) {
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setObject(1, param);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}