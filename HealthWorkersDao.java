package com.mycompany.healthinformationsystem.HealthWorkers;

import com.mycompany.healthinformationsystem.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HealthWorkersDao {

    public boolean insert(HealthWorkers hw) {

        String sql =
            "INSERT INTO health_workers "
          + "(health_worker_id,"
          + "first_name,"
          + "middle_name,"
          + "last_name,"
          + "suffix,"
          + "contact_number,"
          + "position) "
          + "VALUES(?,?,?,?,?,?,?)";

        try(Connection conn = DB.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, hw.getHealthWorkerId());
            ps.setString(2, hw.getFirstName());
            ps.setString(3, hw.getMiddleName());
            ps.setString(4, hw.getLastName());
            ps.setString(5, hw.getSuffix());
            ps.setString(6, hw.getContactNumber());
            ps.setString(7, hw.getPosition());

            return ps.executeUpdate() > 0;

        } catch(SQLException ex) {

            ex.printStackTrace();
        }

        return false;
    }

    public boolean update(HealthWorkers hw) {

        String sql =
            "UPDATE health_workers "
          + "SET first_name=?,"
          + "middle_name=?,"
          + "last_name=?,"
          + "suffix=?,"
          + "contact_number=?,"
          + "position=? "
          + "WHERE health_worker_id=?";

        try(Connection conn = DB.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1, hw.getFirstName());
            ps.setString(2, hw.getMiddleName());
            ps.setString(3, hw.getLastName());
            ps.setString(4, hw.getSuffix());
            ps.setString(5, hw.getContactNumber());
            ps.setString(6, hw.getPosition());

            ps.setString(7, hw.getHealthWorkerId());

            return ps.executeUpdate() > 0;

        } catch(SQLException ex) {

            ex.printStackTrace();
        }

        return false;
    }

    public boolean delete(String healthWorkerId) {

        String sql =
            "DELETE FROM health_workers "
          + "WHERE health_worker_id=?";

        try(Connection conn = DB.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1,
                    healthWorkerId);

            return ps.executeUpdate() > 0;

        } catch(SQLException ex) {

            ex.printStackTrace();
        }

        return false;
    }

    public List<HealthWorkers> getAll() {

        List<HealthWorkers> list =
                new ArrayList<>();

        String sql =
                "SELECT * FROM health_workers";

        try(Connection conn = DB.getConnection();
            PreparedStatement ps =
                conn.prepareStatement(sql);
            ResultSet rs =
                ps.executeQuery()) {

            while(rs.next()) {

                HealthWorkers hw =
                        new HealthWorkers();

                hw.setHealthWorkerId(
                        rs.getString(
                                "health_worker_id"));

                hw.setFirstName(
                        rs.getString(
                                "first_name"));

                hw.setMiddleName(
                        rs.getString(
                                "middle_name"));

                hw.setLastName(
                        rs.getString(
                                "last_name"));

                hw.setSuffix(
                        rs.getString(
                                "suffix"));

                hw.setContactNumber(
                        rs.getString(
                                "contact_number"));

                hw.setPosition(
                        rs.getString(
                                "position"));

                list.add(hw);
            }

        } catch(SQLException ex) {

            ex.printStackTrace();
        }

        return list;
    }
}