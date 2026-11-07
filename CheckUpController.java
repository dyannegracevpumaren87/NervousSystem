package com.mycompany.healthinformationsystem.CheckUp;

import com.mycompany.healthinformationsystem.DB;
import com.mycompany.healthinformationsystem.HealthView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class CheckUpController {

    HealthView hv;
    private final DefaultTableModel tableModel;

    // checkup_id of each table row (same order as the rows shown)
    private final List<Integer> rowIds = new ArrayList<>();

    // checkup_id of the selected row (-1 = none)
    private int selectedId = -1;

    public CheckUpController(HealthView temp) {
        hv = temp;
        hv.checkUpListeners(new AllActions());
        hv.jTextField24.setEditable(false);
hv.bmiClassification.setEditable(false);

        tableModel = new DefaultTableModel(new String[]{
            "Resident Id", "Worker Id", "Check-up Date", "Weight", "Height", "Bmi",
            "Bmi Classification", "Systolic Bp", "Diastolic Bp", "Bp Category",
            "Blood Sugar", "Classification", "Check-up Notes"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        hv.CheckUpTbl.setModel(tableModel);

        hv.CheckUpTbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFieldsFromSelectedRow();
            }
        });

        loadCheckUpTbl();
        hv.jTextField22.addKeyListener(
        new java.awt.event.KeyAdapter() {

            
    @Override
    public void keyReleased(
            java.awt.event.KeyEvent evt) {

        calculateBMI();
    }
});

hv.jTextField23.addKeyListener(
        new java.awt.event.KeyAdapter() {

    @Override
    public void keyReleased(
            java.awt.event.KeyEvent evt) {

        calculateBMI();
    }
});
    }

    // ---------- LOAD TABLE ----------
    private void loadCheckUpTbl() {
        tableModel.setRowCount(0);
        rowIds.clear();

        String sql = "SELECT checkup_id, resident_id, worker_id, checkup_date, weight_kg, height_cm, bmi, "
                + "bmi_classification, systolic_bp, diastolic_bp, bp_category, blood_sugar_mmol_per_liter, "
                + "classification, checkup_notes FROM checkups";

        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rowIds.add(rs.getInt("checkup_id"));
                tableModel.addRow(new Object[]{
                    rs.getString("resident_id"),
                    rs.getString("worker_id"),
                    rs.getDate("checkup_date"),
                    rs.getString("weight_kg"),
                    rs.getString("height_cm"),
                    rs.getString("bmi"),
                    rs.getString("bmi_classification"),
                    rs.getString("systolic_bp"),
                    rs.getString("diastolic_bp"),
                    rs.getString("bp_category"),
                    rs.getString("blood_sugar_mmol_per_liter"),
                    rs.getString("classification"),
                    rs.getString("checkup_notes")
                });
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- CLICK ROW -> FILL FIELDS ----------
    private void fillFieldsFromSelectedRow() {
        int row = hv.CheckUpTbl.getSelectedRow();
        if (row < 0 || row >= rowIds.size()) {
            return;
        }
        selectedId = rowIds.get(row);

        String sql = "SELECT * FROM checkups WHERE checkup_id = ?";
        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hv.jTextField13.setText(nz(rs.getString("resident_id")));
                    hv.jTextField15.setText(nz(rs.getString("worker_id")));
                    hv.jTextField21.setText(nz(rs.getString("checkup_date")));
                    hv.jTextField22.setText(nz(rs.getString("weight_kg")));
                    hv.jTextField23.setText(nz(rs.getString("height_cm")));
                    hv.jTextField24.setText(nz(rs.getString("bmi")));
                    hv.bmiClassification.setText(nz(rs.getString("bmi_classification")));
                    hv.jTextField26.setText(nz(rs.getString("systolic_bp")));
                    hv.jTextField27.setText(nz(rs.getString("diastolic_bp")));
                    hv.jComboBox5.setSelectedItem(rs.getString("bp_category"));
                    hv.jTextField52.setText(nz(rs.getString("blood_sugar_mmol_per_liter")));
                    hv.jTextField53.setText(nz(rs.getString("classification")));
                    hv.jTextField54.setText(nz(rs.getString("checkup_notes")));
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
        if (hv.jTextField13.getText().trim().isEmpty() // resident id
                || hv.jTextField15.getText().trim().isEmpty() // worker id
                || hv.jTextField21.getText().trim().isEmpty()) { // check-up date
            JOptionPane.showMessageDialog(hv,
                    "Resident Id, Worker Id and Check-up Date are required.");
            return false;
        }
        try {
            Date.valueOf(hv.jTextField21.getText().trim());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(hv, "Check-up Date must be in YYYY-MM-DD format (e.g. 2026-11-06).");
            return false;
        }
        try {
            Integer.valueOf(hv.jTextField13.getText().trim());
            Integer.valueOf(hv.jTextField15.getText().trim());
            optInt(hv.jTextField26.getText());
            optInt(hv.jTextField27.getText());
            optDecimal(hv.jTextField22.getText());
            optDecimal(hv.jTextField23.getText());
            optDecimal(hv.jTextField24.getText());
            optDecimal(hv.jTextField52.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(hv,
                    "Resident Id, Worker Id, Weight, Height, BMI, Systolic, Diastolic and Blood Sugar must be numbers.");
            return false;
        }
        return true;
    }

    // blank -> null, otherwise a number (throws NumberFormatException if invalid)
    private Integer optInt(String s) {
        return (s == null || s.trim().isEmpty()) ? null : Integer.valueOf(s.trim());
    }

    private BigDecimal optDecimal(String s) {
        return (s == null || s.trim().isEmpty()) ? null : new BigDecimal(s.trim());
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private void setNullableDecimal(PreparedStatement ps, int index, String value) throws SQLException {
        BigDecimal d = optDecimal(value);
        if (d == null) {
            ps.setNull(index, java.sql.Types.DECIMAL);
        } else {
            ps.setBigDecimal(index, d);
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, String value) throws SQLException {
        Integer i = optInt(value);
        if (i == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, i);
        }
    }

    // binds the 13 form values to parameters 1..13
    private void bindFields(PreparedStatement ps) throws SQLException {
        ps.setInt(1, Integer.parseInt(hv.jTextField13.getText().trim()));   // resident id
        ps.setInt(2, Integer.parseInt(hv.jTextField15.getText().trim()));   // worker id
        ps.setDate(3, Date.valueOf(hv.jTextField21.getText().trim()));      // check-up date
        setNullableDecimal(ps, 4, hv.jTextField22.getText());               // weight
        setNullableDecimal(ps, 5, hv.jTextField23.getText());               // height
        
        setNullableString(ps, 6, hv.bmiClassification.getText());
        setNullableInt(ps, 7, hv.jTextField26.getText());                   // systolic bp
        setNullableInt(ps, 8, hv.jTextField27.getText());                   // diastolic bp
        Object bpCat = hv.jComboBox5.getSelectedItem();                     // bp category
        setNullableString(ps,9, bpCat == null ? null : bpCat.toString());
        setNullableDecimal(ps, 10, hv.jTextField52.getText());              // blood sugar
        setNullableString(ps, 11, hv.jTextField53.getText());               // classification
        setNullableString(ps, 12, hv.jTextField54.getText());               // notes
    }

    // ---------- INSERT ----------
    private void insertCheckUp() {

        if (!validateInput()) {
            return;
        }

        String sql = "INSERT INTO checkups "
                + "(resident_id, worker_id, checkup_date, weight_kg, height_cm, "
                + "bmi_classification, systolic_bp, diastolic_bp, bp_category, "
                + "blood_sugar_mmol_per_liter, classification, checkup_notes) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            bindFields(ps);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                JOptionPane.showMessageDialog(
                        hv,
                        "Check-up inserted successfully!"
                );

                loadCheckUpTbl();
                clearFields();
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- UPDATE ----------
    private void updateCheckUp() {

        if (selectedId < 0) {
            JOptionPane.showMessageDialog(
                    hv,
                    "Please select a record first.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        String sql
                = "UPDATE checkups SET "
                + "resident_id=?, "
                + "worker_id=?, "
                + "checkup_date=?, "
                + "weight_kg=?, "
                + "height_cm=?, "
                + "bmi_classification=?, "
                + "systolic_bp=?, "
                + "diastolic_bp=?, "
                + "bp_category=?, "
                + "blood_sugar_mmol_per_liter=?, "
                + "classification=?, "
                + "checkup_notes=? "
                + "WHERE checkup_id=?";

        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            bindFields(ps);

            ps.setInt(14, selectedId);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                JOptionPane.showMessageDialog(
                        hv,
                        "Check-up updated successfully!"
                );

                loadCheckUpTbl();
                clearFields();
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ---------- DELETE ----------
    private void deleteCheckUp() {

        if (selectedId < 0) {
            JOptionPane.showMessageDialog(
                    hv,
                    "Please select a record first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                hv,
                "Delete selected check-up?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM checkups WHERE checkup_id=?";

        try (Connection conn = DB.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedId);

            int rows = ps.executeUpdate();

            if (rows > 0) {

                JOptionPane.showMessageDialog(
                        hv,
                        "Check-up deleted successfully!"
                );

                loadCheckUpTbl();
                clearFields();
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void calculateBMI() {

    try {

        double weight =
                Double.parseDouble(
                        hv.jTextField22.getText().trim());

        double heightCm =
                Double.parseDouble(
                        hv.jTextField23.getText().trim());

        double heightM = heightCm / 100.0;

        double bmi =
                weight /
                (heightM * heightM);

        hv.jTextField24.setText(
                String.format("%.2f", bmi));

        updateBMIClassification(bmi);

    } catch (NumberFormatException ex) {

        hv.jTextField24.setText("");
        hv.bmiClassification.setText("");
    }
}
    private void updateBMIClassification(double bmi) {

    if (bmi < 18.5) {

        hv.bmiClassification.setText("Underweight");

    } else if (bmi < 25) {

        hv.bmiClassification.setText("Normal Weight");

    } else if (bmi < 30) {

        hv.bmiClassification.setText("Overweight");

    } else {

        hv.bmiClassification.setText("Obese");
    }
}
    // ---------- HELPERS ----------
    private void clearFields() {
        hv.jTextField13.setText("");
        hv.jTextField15.setText("");
        hv.jTextField21.setText("");
        hv.jTextField22.setText("");
        hv.jTextField23.setText("");
        hv.jTextField24.setText("");
        hv.bmiClassification.setText("");
        hv.jTextField26.setText("");
        hv.jTextField27.setText("");
        if (hv.jComboBox5.getItemCount() > 0) {
            hv.jComboBox5.setSelectedIndex(0);
        }
        hv.jTextField52.setText("");
        hv.jTextField53.setText("");
        hv.jTextField54.setText("");
        hv.CheckUpTbl.clearSelection();
        selectedId = -1;
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(hv, "Database Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- BUTTON EVENTS (Check-up tab buttons only) ----------
    class AllActions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();

            if (source == hv.Insert4) {

                insertCheckUp();

            } else if (source == hv.Update4) {

                updateCheckUp();

            } else if (source == hv.Delete4) {

                deleteCheckUp();
            }
        }
    }
}
