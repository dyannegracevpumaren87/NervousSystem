package com.mycompany.healthinformationsystem.Appointment;

import com.mycompany.healthinformationsystem.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDao {

    public List<String> getHealthWorkerIds() {

        List<String> list =
                new ArrayList<>();

        String sql =
                "SELECT health_worker_id "
                + "FROM health_workers "
                + "ORDER BY health_worker_id";

        try (
                Connection conn =
                        DB.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()) {

            while (rs.next()) {

                list.add(
                        rs.getString(
                                "health_worker_id"));
            }

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

        return list;
    }

    public int getWorkerId(
            String healthWorkerId) {

        String sql =
                "SELECT worker_id "
                + "FROM health_workers "
                + "WHERE health_worker_id=?";

        try (
                Connection conn =
                        DB.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    healthWorkerId);

            try (
                    ResultSet rs =
                            ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(
                            "worker_id");
                }
            }

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

        return -1;
    }

    public String getHealthWorkerId(
            int workerId) {

        String sql =
                "SELECT health_worker_id "
                + "FROM health_workers "
                + "WHERE worker_id=?";

        try (
                Connection conn =
                        DB.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    workerId);

            try (
                    ResultSet rs =
                            ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getString(
                            "health_worker_id");
                }
            }

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

        return "";
    }

    public int getResidentAge(
            int residentId) {

        String sql =
                "SELECT TIMESTAMPDIFF("
                + "YEAR,"
                + "birthdate,"
                + "CURDATE()) age "
                + "FROM residents "
                + "WHERE resident_id=?";

        try (
                Connection conn =
                        DB.getConnection();

                PreparedStatement ps =
                        conn.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    residentId);

            try (
                    ResultSet rs =
                            ps.executeQuery()) {

                if (rs.next()) {

                    return rs.getInt(
                            "age");
                }
            }

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

        return -1;
    }

    public int getTotalAppointment() {

        return count(
                "SELECT COUNT(*) FROM appointment");
    }

    public int getTodaysAppointment() {

        return count(
                "SELECT COUNT(*) "
                + "FROM appointment "
                + "WHERE DATE(appointment_date)=CURDATE()");
    }

    private int count(
            String sql) {

        try (
                Connection conn =
                        DB.getConnection();

                Statement stmt =
                        conn.createStatement();

                ResultSet rs =
                        stmt.executeQuery(sql)) {

            if (rs.next()) {

                return rs.getInt(1);
            }

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

        return 0;
    }
}