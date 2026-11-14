package com.mycompany.healthinformationsystem.Household;

import com.mycompany.healthinformationsystem.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HouseholdDao {

    public List<Household> getHouseholdsBySitio(String sitio)
            throws SQLException {

        String sql =
                "SELECT household_id, household_no, sitio, economic_status "
                + "FROM household "
                + "WHERE sitio = ? "
                + "ORDER BY household_no";

        List<Household> household = new ArrayList<>();

        try (
                Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, sitio);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    Household h = new Household();

                    h.setHouseholdId(rs.getInt("household_id"));
                    h.setHouseholdNo(rs.getString("household_no"));
                    h.setSitio(rs.getString("sitio"));
                    h.setEconomicStatus(rs.getString("economic_status"));

                    household.add(h);
                }
            }
        }

        return household;
    }

    public boolean insertHousehold(Household h)
            throws SQLException {

        String sql =
                "INSERT INTO household "
                + "(household_no, sitio, economic_status) "
                + "VALUES (?, ?, ?)";

        try (
                Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, h.getHouseholdNo());
            ps.setString(2, h.getSitio());
            ps.setString(3, h.getEconomicStatus());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateHousehold(Household h)
            throws SQLException {

        String sql =
                "UPDATE household "
                + "SET household_no=?, economic_status=? "
                + "WHERE household_id=?";

        try (
                Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, h.getHouseholdNo());
            ps.setString(2, h.getEconomicStatus());
            ps.setInt(3, h.getHouseholdId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteHousehold(int householdId)
            throws SQLException {

        String sql =
                "DELETE FROM household "
                + "WHERE household_id=?";

        try (
                Connection conn = DB.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, householdId);

            return ps.executeUpdate() > 0;
        }
    }
}