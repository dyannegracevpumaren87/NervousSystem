package com.mycompany.healthinformationsystem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class HealthController {

    HealthView hv;
    private DefaultTableModel tableModel;

    public HealthController(HealthView temp) {

        hv = temp;

        hv.allListeners(new AllActions());

        tableModel = new DefaultTableModel(
            new String[]{"House No.", "Sitio", "Economic Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hv.householdTbl.setModel(tableModel);

        loadHouseholdTbl();
    }

    // SITIO NAME -> sitio_id (match these IDs to your sitio table)
    private int getSitioId(String name) {
        switch (name) {
            case "Ilawod":    return 1;
            case "Kalubihan": return 2;
            case "Kaniogan":  return 3;
            case "Pang-Pang": return 4;
            case "Proper":    return 5;
            default:          return 0;
        }
    }

    // sitio_id -> SITIO NAME
    private String getSitioName(int id) {
        switch (id) {
            case 1:  return "Ilawod";
            case 2:  return "Kalubihan";
            case 3:  return "Kaniogan";
            case 4:  return "Pang-Pang";
            case 5:  return "Proper";
            default: return "";
        }
    }

    // DISPLAY HOUSEHOLD RECORDS
    private void loadHouseholdTbl() {

        tableModel.setRowCount(0);

        String sql = "SELECT household_no, sitio_id, economic_status FROM household";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("household_no"),
                    getSitioName(rs.getInt("sitio_id")),
                    rs.getString("economic_status")
                });
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // INSERT HOUSEHOLD
    private void insertHousehold() {

        String houseNo = hv.txtHouseNo.getText().trim();
        String economicStatus = hv.txtEconomicStatus.getText().trim();
        Object selectedSitio = hv.jComboBox1.getSelectedItem();

        if (houseNo.isEmpty() || economicStatus.isEmpty() || selectedSitio == null) {
            JOptionPane.showMessageDialog(hv, "Please fill in all fields.");
            return;
        }

        String sitio = selectedSitio.toString();

        String sql = "INSERT INTO household (household_no, sitio_id, economic_status) VALUES (?, ?, ?)";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, houseNo);
            ps.setInt(2, getSitioId(sitio));
            ps.setString(3, economicStatus);

            int result = ps.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(hv, "Household inserted successfully!");
                loadHouseholdTbl();
                clearFields();
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // UPDATE HOUSEHOLD
    private void updateHousehold() {

        String houseNo = hv.txtHouseNo.getText().trim();
        String economicStatus = hv.txtEconomicStatus.getText().trim();
        Object selectedSitio = hv.jComboBox1.getSelectedItem();

        if (houseNo.isEmpty() || economicStatus.isEmpty() || selectedSitio == null) {
            JOptionPane.showMessageDialog(hv, "Please fill in all fields.");
            return;
        }

        String sitio = selectedSitio.toString();

        String sql = "UPDATE household SET sitio_id = ?, economic_status = ? WHERE household_no = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, getSitioId(sitio));
            ps.setString(2, economicStatus);
            ps.setString(3, houseNo);

            int result = ps.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(hv, "Household updated successfully!");
                loadHouseholdTbl();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(hv, "House number not found.");
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // DELETE HOUSEHOLD
    private void deleteHousehold() {

        String houseNo = hv.txtHouseNo.getText().trim();

        if (houseNo.isEmpty()) {
            JOptionPane.showMessageDialog(hv, "Enter House No. first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                hv,
                "Are you sure you want to delete House No. " + houseNo + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM household WHERE household_no = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, houseNo);

            int result = ps.executeUpdate();

            if (result > 0) {
                JOptionPane.showMessageDialog(hv, "Household deleted successfully!");
                loadHouseholdTbl();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(hv, "House number not found.");
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // CLEAR INPUT FIELDS
    private void clearFields() {
        hv.txtHouseNo.setText("");
        hv.jComboBox1.setSelectedIndex(0);
        hv.txtEconomicStatus.setText("");
    }

    // DISPLAY DATABASE ERROR
    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(
                hv,
                "Database Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // BUTTON EVENTS
    class AllActions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();

            if (source == hv.btnInsert) {
                insertHousehold();
            } else if (source == hv.btnUpdate) {
                updateHousehold();
            } else if (source == hv.btnDelete) {
                deleteHousehold();
            }
        }
    }
}