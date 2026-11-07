package com.mycompany.healthinformationsystem.Residents;

import com.mycompany.healthinformationsystem.DB;
import com.mycompany.healthinformationsystem.HealthView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ResidentsController {

    HealthView hv;
    private final DefaultTableModel tableModel;

    // resident_id of each table row (same order as the rows shown)
    private final List<Integer> rowIds = new ArrayList<>();

    // resident_id of the selected row (-1 = none)
    private int selectedId = -1;

    public ResidentsController(HealthView temp) {
        hv = temp;
        hv.residentListeners(new AllActions());

        tableModel = new DefaultTableModel(new String[]{
            "Household Id", "Barangay Id", "Full Name", "Birthdate", "Sex",
            "Civil Status", "Contact Number", "Email Address", "Disability", "Pwd Id"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hv.ResidentsTbl.setModel(tableModel);

        hv.ResidentsTbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFieldsFromSelectedRow();
            }
        });

        loadResidentsTbl();
    }

    // ---------- LOAD TABLE ----------
    private void loadResidentsTbl() {
        tableModel.setRowCount(0);
        rowIds.clear();

        String sql = "SELECT resident_id, household_id, barangay_id, first_name, middle_name, "
                   + "last_name, suffix, birthdate, sex, civil_status, contact_number, "
                   + "email_address, disability, pwd_id FROM residents";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rowIds.add(rs.getInt("resident_id"));
                tableModel.addRow(new Object[]{
                    rs.getString("household_id"),
                    rs.getString("barangay_id"),
                    buildFullName(rs.getString("first_name"), rs.getString("middle_name"),
                                  rs.getString("last_name"), rs.getString("suffix")),
                    rs.getDate("birthdate"),
                    rs.getString("sex"),
                    rs.getString("civil_status"),
                    rs.getString("contact_number"),
                    rs.getString("email_address"),
                    rs.getString("disability"),
                    rs.getString("pwd_id")
                });
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private String buildFullName(String first, String middle, String last, String suffix) {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{first, middle, last, suffix}) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(part.trim());
            }
        }
        return sb.toString();
    }

    // ---------- CLICK ROW -> FILL FIELDS ----------
    private void fillFieldsFromSelectedRow() {
        int row = hv.ResidentsTbl.getSelectedRow();
        if (row < 0 || row >= rowIds.size()) {
            return;
        }
        selectedId = rowIds.get(row);

        String sql = "SELECT * FROM residents WHERE resident_id = ?";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hv.jTextField12.setText(nz(rs.getString("household_id")));
                    hv.jTextField14.setText(nz(rs.getString("barangay_id")));
                    hv.jTextField34.setText(nz(rs.getString("first_name")));
                    hv.jTextField42.setText(nz(rs.getString("middle_name")));
                    hv.jTextField43.setText(nz(rs.getString("last_name")));
                    hv.jTextField44.setText(nz(rs.getString("suffix")));
                    hv.jTextField45.setText(nz(rs.getString("birthdate")));
                    hv.jComboBox2.setSelectedItem(rs.getString("sex"));            // sex
                    hv.jComboBox3.setSelectedItem(rs.getString("civil_status"));   // civil status
                    hv.jTextField48.setText(nz(rs.getString("contact_number")));
                    hv.jTextField49.setText(nz(rs.getString("email_address")));
                    hv.jTextField50.setText(nz(rs.getString("disability")));
                    hv.jTextField51.setText(nz(rs.getString("pwd_id")));
                }
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    // ---------- VALIDATION ----------
    private boolean validateInput() {
        if (hv.jTextField12.getText().trim().isEmpty()          // household id
                || hv.jTextField14.getText().trim().isEmpty()   // barangay id
                || hv.jTextField34.getText().trim().isEmpty()   // first name
                || hv.jTextField43.getText().trim().isEmpty()   // last name
                || hv.jTextField45.getText().trim().isEmpty()   // birthdate
                || hv.jComboBox2.getSelectedItem() == null) {   // sex
            JOptionPane.showMessageDialog(hv,
                "Household Id, Barangay Id, First Name, Last Name, Birthdate and Sex are required.");
            return false;
        }
        try {
            Date.valueOf(hv.jTextField45.getText().trim());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(hv, "Birthdate must be in YYYY-MM-DD format (e.g. 2001-05-23).");
            return false;
        }
        return true;
    }

    // empty optional field -> NULL
    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    // binds the 13 form values to parameters 1..13
    private void bindFields(PreparedStatement ps) throws SQLException {
        ps.setString(1, hv.jTextField12.getText().trim());             // household id
        ps.setString(2, hv.jTextField14.getText().trim());             // barangay id
        ps.setString(3, hv.jTextField34.getText().trim());             // first name
        setNullableString(ps, 4, hv.jTextField42.getText());           // middle name
        ps.setString(5, hv.jTextField43.getText().trim());             // last name
        setNullableString(ps, 6, hv.jTextField44.getText());           // suffix
        ps.setDate(7, Date.valueOf(hv.jTextField45.getText().trim())); // birthdate
        ps.setString(8, hv.jComboBox2.getSelectedItem().toString());   // sex
        Object civil = hv.jComboBox3.getSelectedItem();                // civil status
        setNullableString(ps, 9, civil == null ? null : civil.toString());
        setNullableString(ps, 10, hv.jTextField48.getText());          // contact number
        setNullableString(ps, 11, hv.jTextField49.getText());          // email
        setNullableString(ps, 12, hv.jTextField50.getText());          // disability
        setNullableString(ps, 13, hv.jTextField51.getText());          // pwd id
    }

    // ---------- INSERT ----------
    private void insertResident() {
        if (!validateInput()) return;

        String sql = "INSERT INTO residents (household_id, barangay_id, first_name, middle_name, "
                   + "last_name, suffix, birthdate, sex, civil_status, contact_number, email_address, "
                   + "disability, pwd_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindFields(ps);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(hv, "Resident inserted successfully!");
                loadResidentsTbl();
                clearFields();
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- UPDATE ----------
    private void updateResident() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(hv, "Click a resident in the table first.");
            return;
        }
        if (!validateInput()) return;

        String sql = "UPDATE residents SET household_id=?, barangay_id=?, first_name=?, middle_name=?, "
                   + "last_name=?, suffix=?, birthdate=?, sex=?, civil_status=?, contact_number=?, "
                   + "email_address=?, disability=?, pwd_id=? WHERE resident_id=?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindFields(ps);
            ps.setInt(14, selectedId);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(hv, "Resident updated successfully!");
                loadResidentsTbl();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(hv, "Resident not found.");
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- DELETE ----------
    private void deleteResident() {
        if (selectedId < 0) {
            JOptionPane.showMessageDialog(hv, "Click a resident in the table first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(hv,
                "Are you sure you want to delete this resident?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM residents WHERE resident_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedId);

            if (ps.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(hv, "Resident deleted successfully!");
                loadResidentsTbl();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(hv, "Resident not found.");
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- HELPERS ----------
    private void clearFields() {
        hv.jTextField12.setText("");
        hv.jTextField14.setText("");
        hv.jTextField34.setText("");
        hv.jTextField42.setText("");
        hv.jTextField43.setText("");
        hv.jTextField44.setText("");
        hv.jTextField45.setText("");
        if (hv.jComboBox2.getItemCount() > 0) hv.jComboBox2.setSelectedIndex(0);
        if (hv.jComboBox3.getItemCount() > 0) hv.jComboBox3.setSelectedIndex(0);
        hv.jTextField48.setText("");
        hv.jTextField49.setText("");
        hv.jTextField50.setText("");
        hv.jTextField51.setText("");
        hv.ResidentsTbl.clearSelection();
        selectedId = -1;
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(hv, "Database Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- BUTTON EVENTS (Residents tab buttons only) ----------
    class AllActions implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == hv.Insert3) {
                insertResident();
            } else if (source == hv.Update3) {
                updateResident();
            } else if (source == hv.Delete3) {
                deleteResident();
            }
        }
    }
}