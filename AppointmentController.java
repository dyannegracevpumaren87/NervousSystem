package com.mycompany.healthinformationsystem.Appointment;

import com.mycompany.healthinformationsystem.DB;
import com.mycompany.healthinformationsystem.HealthView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class AppointmentController {

    // SQL expression for the worker's full name in the Health Worker combo.
    // Change it if your table stores the name differently, e.g.
    // "CONCAT(first_name, ' ', last_name)".
    // name of the appointments table (used by every query below)
    private static final String TABLE = "appointments";

    // column in appointments that stores the health worker's code (varchar, e.g. BHW-001)
    private static final String WORKER_COL = "health_worker_id";

    private static final String WORKER_NAME_SQL = "CONCAT(first_name, ' ', last_name)";

    private static final DateTimeFormatter DT_FMT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HealthView hv;
    private final DefaultTableModel tableModel;

    // checkup_id of every table row, in the same order as the rows
    private final List<Integer> rowIds = new ArrayList<>();
    private int selectedId = -1;

    // health_worker_id typed in the search box (null = show everything)
    private String currentFilter = null;

    // true while a row is being copied into the fields, so the
    // auto-calculations don't overwrite the saved values
    private boolean loadingFields = false;

    // columns the database fills in itself (MySQL generated columns, e.g. bmi);
    // they are skipped in INSERT and UPDATE
    private final Set<String> generatedCols = new HashSet<>();

    // Health Worker combo item: shows the full name, carries the health_worker_id
    private static class WorkerItem {

        final String id;      // health_worker_id, e.g. BHW-001
        final String label;

        WorkerItem(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public AppointmentController(HealthView temp) {

        hv = temp;

        hv.appointmentListeners(new AllActions());

        // search: button click or Enter key in the search box
        hv.searchBtn.addActionListener(e -> searchByWorker());
        hv.searchField.addActionListener(e -> searchByWorker());

        // auto-generated fields can't be typed in
        hv.checkUpDate.setEditable(false);
        hv.bmi.setEditable(false);
        hv.bmiclassification.setEditable(false);
        hv.bpCategory.setEditable(false);
        hv.classification.setEditable(false);

        loadWorkers();
        detectGeneratedColumns();

        // read-only table model
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
        hv.CheckUpTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // click a row -> fill the fields
        hv.CheckUpTbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFieldsFromSelectedRow();
            }
        });

        // auto-calculations
        DocumentListener bmiListener = onChange(this::calculateBMI);
        hv.weight.getDocument().addDocumentListener(bmiListener);
        hv.height.getDocument().addDocumentListener(bmiListener);

        DocumentListener bpListener = onChange(this::calculateBpCategory);
        hv.systolicBp.getDocument().addDocumentListener(bpListener);
        hv.diastolicBp.getDocument().addDocumentListener(bpListener);

        hv.bloodSugar.getDocument().addDocumentListener(onChange(this::calculateSugarClass));

        // live clock in the date field while no record is selected
        hv.checkUpDate.setText(now());
        new Timer(1000, e -> {
            if (selectedId < 0) {
                hv.checkUpDate.setText(now());
            }
        }).start();

        loadCheckUpTbl();
    }

    private DocumentListener onChange(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    // ------------------------------------------------------------------
    // Health worker combo (shows full names)
    // ------------------------------------------------------------------
    /**
     * Fills the Health Worker combo. Public so the Health Workers page can
     * call it after adding or removing a worker.
     */
    @SuppressWarnings("unchecked")
    public void loadWorkers() {

        JComboBox<Object> cb = (JComboBox<Object>) (JComboBox<?>) hv.healthWorkerSelection;
        DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<>();

        String sql = "SELECT health_worker_id, " + WORKER_NAME_SQL + " AS worker_name "
                + "FROM health_workers ORDER BY " + WORKER_NAME_SQL;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addElement(new WorkerItem(
                        rs.getString("health_worker_id"),
                        rs.getString("worker_name")));
            }

        } catch (SQLException ex) {
            showError(ex);
        }

        cb.setModel(model);
        cb.setSelectedIndex(model.getSize() > 0 ? 0 : -1);
    }

    private void selectWorker(String workerCode) {

        for (int i = 0; i < hv.healthWorkerSelection.getItemCount(); i++) {
            Object item = hv.healthWorkerSelection.getItemAt(i);

            if (item instanceof WorkerItem && ((WorkerItem) item).id.equalsIgnoreCase(workerCode)) {
                hv.healthWorkerSelection.setSelectedIndex(i);
                return;
            }
        }

        hv.healthWorkerSelection.setSelectedIndex(-1);
    }

    // ------------------------------------------------------------------
    // Search by health worker id (e.g. BHW-001)
    // ------------------------------------------------------------------
    private void searchByWorker() {

        String key = hv.searchField.getText().trim();
        currentFilter = key.isEmpty() ? null : key;

        clearFields();
        loadCheckUpTbl();

        if (currentFilter != null && tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(hv,
                    "No check-ups found for health worker " + currentFilter + ".");
        }
    }

    // ------------------------------------------------------------------
    // Load table (respects the current search filter)
    // ------------------------------------------------------------------
    private void loadCheckUpTbl() {

        tableModel.setRowCount(0);
        rowIds.clear();

        String sql = "SELECT c.appointment_id, c.resident_id, "
                + "c." + WORKER_COL + " AS worker_code, "
                + "c.appointment_date, c.weight_kg, c.height_cm, c.bmi, c.bmi_classification, "
                + "c.systolic_bp, c.diastolic_bp, c.bp_category, "
                + "c.blood_sugar_mmol_per_liter, c.classification, c.checkup_notes "
                + "FROM " + TABLE + " c "
                + (currentFilter != null ? "WHERE UPPER(c." + WORKER_COL + ") = UPPER(?) " : "")
                + "ORDER BY c.appointment_date DESC, c.appointment_id DESC";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (currentFilter != null) {
                ps.setString(1, currentFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rowIds.add(rs.getInt("appointment_id"));

                    tableModel.addRow(new Object[]{
                        rs.getString("resident_id"),
                        rs.getString("worker_code"),
                        formatTs(rs.getTimestamp("appointment_date")),
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
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ------------------------------------------------------------------
    // Click a row -> fill the fields
    // ------------------------------------------------------------------
    private void fillFieldsFromSelectedRow() {

        int viewRow = hv.CheckUpTbl.getSelectedRow();

        if (viewRow < 0) {
            selectedId = -1;
            return;
        }

        int row = hv.CheckUpTbl.convertRowIndexToModel(viewRow);

        if (row >= rowIds.size()) {
            selectedId = -1;
            return;
        }

        selectedId = rowIds.get(row);

        String sql = "SELECT * FROM " + TABLE + " WHERE appointment_id = ?";

        loadingFields = true;

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hv.residentId.setText(nz(rs.getString("resident_id")));
                    selectWorker(rs.getString(WORKER_COL));
                    hv.checkUpDate.setText(formatTs(rs.getTimestamp("appointment_date")));
                    hv.weight.setText(nz(rs.getString("weight_kg")));
                    hv.height.setText(nz(rs.getString("height_cm")));
                    hv.bmi.setText(nz(rs.getString("bmi")));
                    hv.bmiclassification.setText(nz(rs.getString("bmi_classification")));
                    hv.systolicBp.setText(nz(rs.getString("systolic_bp")));
                    hv.diastolicBp.setText(nz(rs.getString("diastolic_bp")));
                    hv.bpCategory.setText(nz(rs.getString("bp_category")));
                    hv.bloodSugar.setText(nz(rs.getString("blood_sugar_mmol_per_liter")));
                    hv.classification.setText(nz(rs.getString("classification")));
                    hv.checkUpNotes.setText(nz(rs.getString("checkup_notes")));
                }
            }

        } catch (SQLException ex) {
            showError(ex);
        } finally {
            loadingFields = false;
        }
    }

    // ------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------
    private void insertCheckUp() {

        // a new check-up is always stamped with the current date and time
        hv.checkUpDate.setText(now());

        if (!validateInput()) {
            return;
        }

        List<Col> cols = writableColumns();

        StringBuilder names = new StringBuilder();
        StringBuilder marks = new StringBuilder();
        for (Col c : cols) {
            if (names.length() > 0) {
                names.append(", ");
                marks.append(",");
            }
            names.append(c.name);
            marks.append("?");
        }

        String sql = "INSERT INTO " + TABLE + " (" + names + ") VALUES (" + marks + ")";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindFields(ps, cols);

            if (ps.executeUpdate() > 0) {
                loadCheckUpTbl();
                clearFields();
                hv.refreshDashboard();
                JOptionPane.showMessageDialog(hv, "Check-up inserted successfully!");
                
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ------------------------------------------------------------------
    // UPDATE (with confirmation)
    // ------------------------------------------------------------------
    private void updateCheckUp() {

        if (selectedId < 0) {
            JOptionPane.showMessageDialog(hv, "Please select a record in the table first.");
            return;
        }

        if (!validateInput()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(hv,
                "Are you sure you want to update this check-up?",
                "Confirm Update", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        List<Col> cols = writableColumns();

        StringBuilder sets = new StringBuilder();
        for (Col c : cols) {
            if (sets.length() > 0) {
                sets.append(", ");
            }
            sets.append(c.name).append("=?");
        }

        String sql = "UPDATE " + TABLE + " SET " + sets + " WHERE appointment_id=?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindFields(ps, cols);
            ps.setInt(cols.size() + 1, selectedId);

            if (ps.executeUpdate() > 0) {
                loadCheckUpTbl();
                clearFields();
                hv.refreshDashboard();
                JOptionPane.showMessageDialog(hv, "Check-up updated successfully!");
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ------------------------------------------------------------------
    // DELETE (with warning)
    // ------------------------------------------------------------------
    private void deleteCheckUp() {

        if (selectedId < 0) {
            JOptionPane.showMessageDialog(hv, "Please select a record in the table first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(hv,
                "Are you sure you want to delete this check-up?\n\nThis cannot be undone.",
                "Warning: Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM " + TABLE + " WHERE appointment_id=?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedId);

            if (ps.executeUpdate() > 0) {
                loadCheckUpTbl();
                clearFields();
                hv.refreshDashboard();
                JOptionPane.showMessageDialog(hv, "Check-up deleted successfully!");
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    // ------------------------------------------------------------------
    // Validation and binding
    // ------------------------------------------------------------------
    private boolean validateInput() {

        if (hv.residentId.getText().trim().isEmpty()
                || !(hv.healthWorkerSelection.getSelectedItem() instanceof WorkerItem)) {

            JOptionPane.showMessageDialog(hv,
                    "Resident Id and Health Worker are required.");
            return false;
        }

        try {
            Timestamp.valueOf(hv.checkUpDate.getText().trim());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(hv,
                    "Check-up date is invalid (expected yyyy-MM-dd HH:mm:ss).");
            return false;
        }

        try {
            Integer.valueOf(hv.residentId.getText().trim());
            optInt(hv.systolicBp.getText());
            optInt(hv.diastolicBp.getText());
            optDecimal(hv.weight.getText());
            optDecimal(hv.height.getText());
            optDecimal(hv.bmi.getText());
            optDecimal(hv.bloodSugar.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(hv,
                    "Resident Id, Weight, Height, Systolic, Diastolic and Blood Sugar must be numbers.");
            return false;
        }

        return true;
    }

    private Integer optInt(String s) {
        return (s == null || s.trim().isEmpty()) ? null : Integer.valueOf(s.trim());
    }

    private BigDecimal optDecimal(String s) {
        return (s == null || s.trim().isEmpty()) ? null : new BigDecimal(s.trim());
    }

    private void setNullableString(PreparedStatement ps, int index, String value)
            throws SQLException {

        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, java.sql.Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

    private void setNullableDecimal(PreparedStatement ps, int index, String value)
            throws SQLException {

        BigDecimal d = optDecimal(value);

        if (d == null) {
            ps.setNull(index, java.sql.Types.DECIMAL);
        } else {
            ps.setBigDecimal(index, d);
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, String value)
            throws SQLException {

        Integer i = optInt(value);

        if (i == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setInt(index, i);
        }
    }

    /** One writable column of the appointments table and how to bind its value. */
    private interface Binder {

        void bind(PreparedStatement ps, int index) throws SQLException;
    }

    private static class Col {

        final String name;
        final Binder binder;

        Col(String name, Binder binder) {
            this.name = name;
            this.binder = binder;
        }
    }

    /** Asks MySQL which columns of the table are generated, so they are never written. */
    private void detectGeneratedColumns() {

        generatedCols.clear();

        String sql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? "
                + "AND (EXTRA LIKE '%VIRTUAL GENERATED%' OR EXTRA LIKE '%STORED GENERATED%')";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, TABLE);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    generatedCols.add(rs.getString(1).toLowerCase());
                }
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    }

    /** The form columns minus the ones the database generates by itself. */
    private List<Col> writableColumns() {

        List<Col> all = new ArrayList<>();

        all.add(new Col("resident_id", (ps, i)
                -> ps.setInt(i, Integer.parseInt(hv.residentId.getText().trim()))));
        all.add(new Col(WORKER_COL, (ps, i)
                -> ps.setString(i,
                        ((WorkerItem) hv.healthWorkerSelection.getSelectedItem()).id)));
        all.add(new Col("appointment_date", (ps, i)
                -> ps.setTimestamp(i, Timestamp.valueOf(hv.checkUpDate.getText().trim()))));
        all.add(new Col("weight_kg", (ps, i)
                -> setNullableDecimal(ps, i, hv.weight.getText())));
        all.add(new Col("height_cm", (ps, i)
                -> setNullableDecimal(ps, i, hv.height.getText())));
        all.add(new Col("bmi", (ps, i)
                -> setNullableDecimal(ps, i, hv.bmi.getText())));
        all.add(new Col("bmi_classification", (ps, i)
                -> setNullableString(ps, i, hv.bmiclassification.getText())));
        all.add(new Col("systolic_bp", (ps, i)
                -> setNullableInt(ps, i, hv.systolicBp.getText())));
        all.add(new Col("diastolic_bp", (ps, i)
                -> setNullableInt(ps, i, hv.diastolicBp.getText())));
        all.add(new Col("bp_category", (ps, i)
                -> setNullableString(ps, i, hv.bpCategory.getText())));
        all.add(new Col("blood_sugar_mmol_per_liter", (ps, i)
                -> setNullableDecimal(ps, i, hv.bloodSugar.getText())));
        all.add(new Col("classification", (ps, i)
                -> setNullableString(ps, i, hv.classification.getText())));
        all.add(new Col("checkup_notes", (ps, i)
                -> setNullableString(ps, i, hv.checkUpNotes.getText())));

        all.removeIf(c -> generatedCols.contains(c.name.toLowerCase()));
        return all;
    }

    /** Binds the form values to parameters 1..n (shared by insert and update). */
    private void bindFields(PreparedStatement ps, List<Col> cols) throws SQLException {

        for (int i = 0; i < cols.size(); i++) {
            cols.get(i).binder.bind(ps, i + 1);
        }
    }

    // ------------------------------------------------------------------
    // Auto-generated values
    // ------------------------------------------------------------------
    private void calculateBMI() {

        if (loadingFields) {
            return;
        }

        try {
            double w = Double.parseDouble(hv.weight.getText().trim());
            double heightCm = Double.parseDouble(hv.height.getText().trim());

            if (w <= 0 || heightCm <= 0) {
                clearBmi();
                return;
            }

            double heightM = heightCm / 100.0;
            double bmiValue = w / (heightM * heightM);

            hv.bmi.setText(String.format("%.2f", bmiValue));

            String result;
            if (bmiValue < 18.5) {
                result = "Underweight";
            } else if (bmiValue < 25) {
                result = "Normal Weight";
            } else if (bmiValue < 30) {
                result = "Overweight";
            } else {
                result = "Obese";
            }
            hv.bmiclassification.setText(result);

        } catch (NumberFormatException ex) {
            clearBmi();
        }
    }

    private void clearBmi() {
        hv.bmi.setText("");
        hv.bmiclassification.setText("");
    }

    /** Blood pressure category from systolic and diastolic (mmHg). */
    private void calculateBpCategory() {

        if (loadingFields) {
            return;
        }

        try {
            Integer sys = optInt(hv.systolicBp.getText());
            Integer dia = optInt(hv.diastolicBp.getText());

            if (sys == null || dia == null) {
                hv.bpCategory.setText("");
                return;
            }

            String result;
            if (sys >= 140 || dia >= 90) {
                result = "Hypertensive";
            } else if (sys >= 130 || dia >= 80) {
                result = "Hypertension";
            } else if (sys >= 120) {
                result = "Elevated";
            } else {
                result = "Normal";
            }
            hv.bpCategory.setText(result);

        } catch (NumberFormatException ex) {
            hv.bpCategory.setText("");
        }
    }

    /** Blood sugar classification from mmol/L (fasting thresholds). */
    private void calculateSugarClass() {

        if (loadingFields) {
            return;
        }

        try {
            BigDecimal v = optDecimal(hv.bloodSugar.getText());

            if (v == null) {
                hv.classification.setText("");
                return;
            }

            String result;
            if (v.compareTo(new BigDecimal("5.6")) < 0) {
                result = "Normal";
            } else if (v.compareTo(new BigDecimal("7.0")) < 0) {
                result = "Prediabetes";
            } else {
                result = "Diabetes";
            }
            hv.classification.setText(result);

        } catch (NumberFormatException ex) {
            hv.classification.setText("");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private void clearFields() {

        hv.residentId.setText("");

        if (hv.healthWorkerSelection.getItemCount() > 0) {
            hv.healthWorkerSelection.setSelectedIndex(0);
        }

        hv.weight.setText("");
        hv.height.setText("");
        hv.bmi.setText("");
        hv.bmiclassification.setText("");
        hv.systolicBp.setText("");
        hv.diastolicBp.setText("");
        hv.bpCategory.setText("");
        hv.bloodSugar.setText("");
        hv.classification.setText("");
        hv.checkUpNotes.setText("");

        hv.CheckUpTbl.clearSelection();
        selectedId = -1;
        hv.checkUpDate.setText(now());   // back to the live current date/time
    }

    private String now() {
        return LocalDateTime.now().format(DT_FMT);
    }

    private String formatTs(Timestamp ts) {
        return ts == null ? "" : ts.toLocalDateTime().format(DT_FMT);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private void showError(SQLException ex) {
        JOptionPane.showMessageDialog(hv, "Database Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

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