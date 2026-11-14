package com.mycompany.healthinformationsystem.Residents;

import com.mycompany.healthinformationsystem.HealthView;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class ResidentsController {

    private final ResidentsDao dao = new ResidentsDao();
    private final HealthView view;
    private final DefaultTableModel tableModel;

    // Residents objects in the same order as the table rows
    // (this is how we know the resident_id of a selected row)
    private List<Residents> currentList = new ArrayList<>();

    public ResidentsController(HealthView view) {

        this.view = view;

        // Replace the table model with a read-only one: rows are changed
        // through the form + buttons, not by typing into the table.
        DefaultTableModel old = (DefaultTableModel) view.getTblResidents().getModel();
        String[] columns = new String[old.getColumnCount()];
        for (int i = 0; i < columns.length; i++) {
            columns[i] = old.getColumnName(i);
        }
        // (removed: columns[6] = "Contact Number"; it overwrote "Civil Status".
        //  The header already says "Contact Number" at index 7.)

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        view.getTblResidents().setModel(tableModel);
        view.getTblResidents().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // SEARCH button and Enter key in the search box
        view.getBtnSearchResident().addActionListener(e
                -> search(view.getTxtSearchResident().getText()));
        view.getTxtSearchResident().addActionListener(e
                -> search(view.getTxtSearchResident().getText()));

        // INSERT / UPDATE / DELETE
        view.Insert3.addActionListener(e -> insertResident());
        view.Update3.addActionListener(e -> updateResident());
        view.Delete3.addActionListener(e -> deleteResident());

        // Click a row -> fill the form
        view.getTblResidents().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateForm();
            }
        });

        loadResidents();
    }

    // ------------------------------------------------------------------
    // Loading / searching
    // ------------------------------------------------------------------
    public void loadResidents() {
        try {
            fillTable(dao.getAll());
        } catch (SQLException e) {
            showError(e);
        }
    }

    public void search(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            loadResidents();
            return;
        }

        try {
            List<Residents> results = dao.searchByName(keyword.trim());
            fillTable(results);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(view, "No matching residents found.");
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void fillTable(List<Residents> residents) {

        currentList = new ArrayList<>(residents);
        tableModel.setRowCount(0);

        for (Residents r : residents) {
            tableModel.addRow(new Object[]{
                r.getResidentId(),      // <-- was missing: fills the "Resident Id" column
                r.getHouseholdId(), r.getBarangayId(), r.getFullName(), r.getBirthdate(),
                r.getSex(), r.getCivilStatus(), r.getContactNumber(), r.getEmailAddress(),
                r.getDisability(), r.getPwdId()
            });
        }

        clearForm();
    }

    // ------------------------------------------------------------------
    // INSERT
    // ------------------------------------------------------------------
    private void insertResident() {

        Residents r = readForm();
        if (r == null) {
            return;
        }

        try {
            dao.insert(r);

            loadResidents();          // the new row shows up in the table
            view.refreshDashboard();  // resident count changed

            JOptionPane.showMessageDialog(view, "Resident added successfully.");

        } catch (SQLException e) {
            showError(e);
        }
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------
    private void updateResident() {

        Residents selected = getSelectedResident();
        if (selected == null) {
            JOptionPane.showMessageDialog(view, "Please select a resident in the table first.");
            return;
        }

        Residents r = readForm();
        if (r == null) {
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                view,
                "Save the changes made to this resident?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        r.setResidentId(selected.getResidentId());

        try {
            dao.update(r);

            loadResidents();          // the table shows the edited values

            JOptionPane.showMessageDialog(view, "Resident updated successfully.");

        } catch (SQLException e) {
            showError(e);
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------
    private void deleteResident() {

        Residents selected = getSelectedResident();
        if (selected == null) {
            JOptionPane.showMessageDialog(view, "Please select a resident in the table first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                view,
                "Delete " + selected.getFullName() + "?\n\nThis cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.delete(selected.getResidentId());

            loadResidents();          // the row disappears from the table
            view.refreshDashboard();  // resident count changed

            JOptionPane.showMessageDialog(view, "Resident deleted successfully.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    view,
                    "Unable to delete resident.\n\n"
                    + "The resident may still have check-up records.\n\n"
                    + e.getMessage(),
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ------------------------------------------------------------------
    // Table <-> form
    // ------------------------------------------------------------------
    /** Returns the Residents object for the selected table row, or null. */
    private Residents getSelectedResident() {

        int viewRow = view.getTblResidents().getSelectedRow();
        if (viewRow < 0) {
            return null;
        }

        int row = view.getTblResidents().convertRowIndexToModel(viewRow);
        if (row >= currentList.size()) {
            return null;
        }

        return currentList.get(row);
    }

    /** Copies the selected table row into the form fields. */
    private void populateForm() {

        Residents r = getSelectedResident();
        if (r == null) {
            return;
        }

        view.getTxtHouseholdId().setText(str(r.getHouseholdId()));
        view.getTxtBarangayId().setText(str(r.getBarangayId()));
        view.getTxtFirstName().setText(str(r.getFirstName()));
        view.getTxtMiddleName().setText(str(r.getMiddleName()));
        view.getTxtLastName().setText(str(r.getLastName()));
        view.getTxtSuffix().setText(str(r.getSuffix()));
        view.getTxtBirthdate().setText(str(r.getBirthdate()));

        String sex = str(r.getSex());
        if (sex.equalsIgnoreCase("Female")) {
            view.getRdoFemale().setSelected(true);
        } else if (sex.equalsIgnoreCase("Male")) {
            view.getRdoMale().setSelected(true);
        } else {
            view.getSexGroup().clearSelection();
        }

        view.getCmbCivilStatus().setSelectedItem(str(r.getCivilStatus()));
        view.getTxtCpNumber().setText(str(r.getContactNumber()));
        view.getTxtEmailAddress().setText(str(r.getEmailAddress()));
        view.getTxtDisability().setText(str(r.getDisability()));
        view.getTxtPwdId().setText(str(r.getPwdId()));
    }

    /**
     * Builds a Residents object from the form fields.
     * Returns null (after showing a message) if something is invalid.
     */
    private Residents readForm() {

        String householdText = view.getTxtHouseholdId().getText().trim();
        String firstName = view.getTxtFirstName().getText().trim();
        String lastName = view.getTxtLastName().getText().trim();
        String birthdayText = view.getTxtBirthdate().getText().trim();

        if (householdText.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            warn("Household ID, First Name and Last Name are required.");
            return null;
        }

        int householdId;
        try {
            householdId = Integer.parseInt(householdText);
        } catch (NumberFormatException e) {
            warn("Household ID must be a number.");
            return null;
        }

        LocalDate birthdate = null;
        if (!birthdayText.isEmpty()) {
            try {
                birthdate = LocalDate.parse(birthdayText);   // YYYY-MM-DD
            } catch (DateTimeParseException e) {
                warn("Birthday must be in YYYY-MM-DD format, for example 2001-05-24.");
                return null;
            }
        }

        String sex = null;
        if (view.getRdoFemale().isSelected()) {
            sex = "Female";
        } else if (view.getRdoMale().isSelected()) {
            sex = "Male";
        }

        Object civil = view.getCmbCivilStatus().getSelectedItem();

        Residents r = new Residents();
        r.setHouseholdId(householdId);
        r.setBarangayId(blankToNull(view.getTxtBarangayId().getText()));
        r.setFirstName(firstName);
        r.setMiddleName(blankToNull(view.getTxtMiddleName().getText()));
        r.setLastName(lastName);
        r.setSuffix(blankToNull(view.getTxtSuffix().getText()));
        r.setBirthdate(birthdate);
        r.setSex(sex);
        r.setCivilStatus(civil == null ? null : civil.toString());
        r.setContactNumber(blankToNull(view.getTxtCpNumber().getText()));
        r.setEmailAddress(blankToNull(view.getTxtEmailAddress().getText()));
        r.setDisability(blankToNull(view.getTxtDisability().getText()));
        r.setPwdId(blankToNull(view.getTxtPwdId().getText()));

        return r;
    }

    public void clearForm() {

        view.getTxtHouseholdId().setText("");
        view.getTxtBarangayId().setText("");
        view.getTxtFirstName().setText("");
        view.getTxtMiddleName().setText("");
        view.getTxtLastName().setText("");
        view.getTxtSuffix().setText("");
        view.getTxtBirthdate().setText("");
        view.getSexGroup().clearSelection();

        if (view.getCmbCivilStatus().getItemCount() > 0) {
            view.getCmbCivilStatus().setSelectedIndex(0);
        }

        view.getTxtCpNumber().setText("");
        view.getTxtEmailAddress().setText("");
        view.getTxtDisability().setText("");
        view.getTxtPwdId().setText("");
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private void warn(String message) {
        JOptionPane.showMessageDialog(view, message, "Missing or invalid data",
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(SQLException e) {
        JOptionPane.showMessageDialog(view, "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}