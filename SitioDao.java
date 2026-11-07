package com.mycompany.healthinforamtionsystem.Sitio;

import com.mycompany.healthinformationsystem.DB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SitioDao {

    public List<SitioModel>
        getHouseholdsBySitio(
            String sitioName) {

        List<SitioModel> list =
                new ArrayList<>();

        String sql =
        "SELECT household_id, "
      + "house_no, "
      + "economic_status "
      + "FROM households "
      + "WHERE sitio=?";

        try(Connection conn =
                DB.getConnection();

            PreparedStatement ps =
                conn.prepareStatement(sql)) {

            ps.setString(1,
                    sitioName);

            ResultSet rs =
                    ps.executeQuery();

            while(rs.next()) {

                SitioModel sh =
                        new SitioModel();

                sh.setHouseholdId(
                        rs.getInt(
                                "household_id"));

                sh.setHouseNo(
                        rs.getString(
                                "house_no"));

                sh.setEconomicStatus(
                        rs.getString(
                                "economic_status"));

                list.add(sh);
            }

        } catch(SQLException ex) {

            ex.printStackTrace();
        }

        return list;
    }
}