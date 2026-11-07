package com.mycompany.healthinformatioonsystem.dashboard;

import com.mycompany.healthinformationsystem.DB;
import java.sql.*;

public class DashboardDao {

    private int count(String sql) {

        try(Connection conn =
                DB.getConnection();
            Statement stmt =
                conn.createStatement();
            ResultSet rs =
                stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch(SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    public int getTotalSitios() {

        return count(
            "SELECT COUNT(*) FROM sitio");
    }

    public int getTotalHouseholds() {

        return count(
            "SELECT COUNT(*) FROM households");
    }

    public int getTotalResidents() {

        return count(
            "SELECT COUNT(*) FROM residents");
    }

    public int getTotalHealthWorkers() {

        return count(
            "SELECT COUNT(*) FROM health_workers");
    }

    public int getTodaysCheckups() {

        return count(
            "SELECT COUNT(*) "
          + "FROM checkups "
          + "WHERE DATE(checkup_date)=CURDATE()");
    }
}