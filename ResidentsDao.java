package com.mycompany.healthinformationsystem.Residents;

import com.mycompany.healthinformationsystem.DB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ResidentsDao {

    // NOTE: the phone column is contact_number (cp_number does not exist).
    // Run "DESCRIBE residents;" in MySQL if any other column name differs.
    private static final String SELECT_BASE
            = "SELECT resident_id, household_id, barangay_id, first_name, middle_name, last_name, "
            + "suffix, birthdate, sex, civil_status, contact_number, email_address, disability, pwd_id "
            + "FROM residents ";

    /** Default list: all residents sorted by resident id. */
    public List<Residents> getAll() throws SQLException {

        String sql = SELECT_BASE + "ORDER BY resident_id ASC";
        List<Residents> list = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        }

        return list;
    }

    /** Search by first or last name (partial match). */
    public List<Residents> searchByName(String keyword) throws SQLException {

        String sql = SELECT_BASE
                + "WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY resident_id ASC";

        List<Residents> list = new ArrayList<>();
        String like = "%" + keyword.trim() + "%";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    /** Residents of one household (used by the Household page's right table). */
    public List<Residents> getByHouseholdId(int householdId) throws SQLException {

        String sql = SELECT_BASE
                + "WHERE household_id = ? "
                + "ORDER BY last_name ASC, first_name ASC";

        List<Residents> list = new ArrayList<>();

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, householdId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }

        return list;
    }

    public boolean insert(Residents r) throws SQLException {

        String sql = "INSERT INTO residents "
                + "(household_id, barangay_id, first_name, middle_name, last_name, suffix, "
                + "birthdate, sex, civil_status, contact_number, email_address, disability, pwd_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bind(ps, r);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Residents r) throws SQLException {

        String sql = "UPDATE residents SET "
                + "household_id=?, barangay_id=?, first_name=?, middle_name=?, last_name=?, suffix=?, "
                + "birthdate=?, sex=?, civil_status=?, contact_number=?, email_address=?, "
                + "disability=?, pwd_id=? "
                + "WHERE resident_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            bind(ps, r);
            ps.setInt(14, r.getResidentId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int residentId) throws SQLException {

        String sql = "DELETE FROM residents WHERE resident_id=?";

        try (Connection con = DB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, residentId);

            return ps.executeUpdate() > 0;
        }
    }

    /** Sets parameters 1-13, shared by insert and update. */
    private void bind(PreparedStatement ps, Residents r) throws SQLException {

        ps.setInt(1, r.getHouseholdId());
        ps.setString(2, r.getBarangayId());
        ps.setString(3, r.getFirstName());
        ps.setString(4, r.getMiddleName());
        ps.setString(5, r.getLastName());
        ps.setString(6, r.getSuffix());

        if (r.getBirthdate() == null) {
            ps.setNull(7, Types.DATE);
        } else {
            ps.setDate(7, Date.valueOf(r.getBirthdate()));
        }

        ps.setString(8, r.getSex());
        ps.setString(9, r.getCivilStatus());
        ps.setString(10, r.getContactNumber());
        ps.setString(11, r.getEmailAddress());
        ps.setString(12, r.getDisability());
        ps.setString(13, r.getPwdId());
    }

    private Residents map(ResultSet rs) throws SQLException {

        Residents r = new Residents();

        r.setResidentId(rs.getInt("resident_id"));
        r.setHouseholdId(rs.getInt("household_id"));
        r.setBarangayId(rs.getString("barangay_id"));
        r.setFirstName(rs.getString("first_name"));
        r.setMiddleName(rs.getString("middle_name"));
        r.setLastName(rs.getString("last_name"));
        r.setSuffix(rs.getString("suffix"));

        Date bday = rs.getDate("birthdate");
        r.setBirthdate(bday == null ? null : bday.toLocalDate());

        r.setSex(rs.getString("sex"));
        r.setCivilStatus(rs.getString("civil_status"));
        r.setContactNumber(rs.getString("contact_number"));
        r.setEmailAddress(rs.getString("email_address"));
        r.setDisability(rs.getString("disability"));
        r.setPwdId(rs.getString("pwd_id"));

        return r;
    }
}