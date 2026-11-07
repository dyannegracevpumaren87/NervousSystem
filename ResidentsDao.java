package com.mycompany.healthinformationsystem.Residents;

import com.mycompany.healthinformationsystem.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResidentsDao {

    // ---------- Resident counts ----------

    public int getTotalResidents() {
        return count("SELECT COUNT(*) FROM residents");
    }

    public int getTotalMale() {
        return count("SELECT COUNT(*) FROM residents WHERE sex = ?", "Male");
    }

    public int getTotalFemale() {
        return count("SELECT COUNT(*) FROM residents WHERE sex = ?", "Female");
    }

    public int getTotalPwd() {
        return count("SELECT COUNT(*) FROM residents WHERE pwd_id IS NOT NULL AND pwd_id <> ''");
    }

    public int getTotalSeniors() {
        // 60 years old and above
        return count("SELECT COUNT(*) FROM residents WHERE birthdate <= DATE_SUB(CURDATE(), INTERVAL 60 YEAR)");
    }

    public int getResidentsByHousehold(int householdId) {
        return count("SELECT COUNT(*) FROM residents WHERE household_id = ?", householdId);
    }

    public int getResidentsByCivilStatus(String civilStatus) {
        return count("SELECT COUNT(*) FROM residents WHERE civil_status = ?", civilStatus);
    }

    // ---------- Helpers ----------

    // Runs a "SELECT COUNT(*) ..." query with no parameters
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

    // Runs a "SELECT COUNT(*) ..." query with one parameter (String or Integer)
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