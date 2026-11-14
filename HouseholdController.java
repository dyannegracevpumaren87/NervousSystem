package com.mycompany.healthinformationsystem.Household;

import com.mycompany.healthinformationsystem.HealthView;
import com.mycompany.healthinformationsystem.Residents.Residents;
import com.mycompany.healthinformationsystem.Residents.ResidentsDao;
import com.mycompany.healthinformationsystem.dashboard.DashboardController;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class HouseholdController {

    private final HealthView view;

    private final HouseholdDao householdDao;
    private final ResidentsDao residentsDao;

    private final DefaultTableModel householdModel;
    private final DefaultTableModel residentModel;

    private List<Household> householdList;
    private List<Residents> residentList;

    private String currentSitio;
    private boolean hasUnsavedChanges = false;
    private boolean loadingData = false;

    public HouseholdController(HealthView view) {

        this.view = view;

        householdDao = new HouseholdDao();
        residentsDao = new ResidentsDao();

        householdList = new ArrayList<>();
        residentList = new ArrayList<>();

        currentSitio = "";

        householdModel
                = (DefaultTableModel) view.getHouseholdsTbl().getModel();

        residentModel
                = (DefaultTableModel) view.getResidentsTbl().getModel();

               initializeEvents(); 
    }

    private void initializeEvents() {

        householdModel.addTableModelListener(e -> {
            if (loadingData) {
                return;
            }

            hasUnsavedChanges = true;

            view.getUpdateHouseholdBtn().setEnabled(true);
        });

        view.getIlawodBtn()
                .addActionListener(e -> loadSitio("ILAWOD"));

        view.getKalubihanBtn()
                .addActionListener(e -> loadSitio("KALUBIHAN"));

        view.getKanioganBtn()
                .addActionListener(e -> loadSitio("KANIOGAN"));

        view.getPangpangBtn()
                .addActionListener(e -> loadSitio("PANG-PANG"));

        view.getProperBtn()
                .addActionListener(e -> loadSitio("PROPER"));

        view.getHouseholdsTbl()
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        view.getHouseholdsTbl()
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        loadResidents();
                    }
                });

        view.getInsertHouseholdBtn()
                .addActionListener(e -> insertHousehold());

        view.getUpdateHouseholdBtn()
                .addActionListener(e -> updateHousehold());

        view.getDeleteHouseholdBtn()
                .addActionListener(e -> deleteHousehold());

        view.getUpdateHouseholdBtn().setEnabled(false);
    }

    /**
     * Refreshes the dashboard counts. The DashboardController registers
     * itself in the view, so it is fetched from there.
     */
    private void refreshDashboard() {

        DashboardController dashboard = view.getDashboardController();

        if (dashboard != null) {
            dashboard.loadDashboard();
        }
    }

    private void loadSitio(String sitio) {

        if (hasUnsavedChanges) {

            int answer
                    = JOptionPane.showConfirmDialog(
                            view,
                            "You have unsaved changes.\n\n"
                            + "Do you want to discard them?",
                            "Unsaved Changes",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

            if (answer != JOptionPane.YES_OPTION) {
                return;
            }

            hasUnsavedChanges = false;
        }

        try {
            loadingData = true;
            currentSitio = sitio;

            householdList
                    = householdDao.getHouseholdsBySitio(sitio);

            householdModel.setRowCount(0);
            residentModel.setRowCount(0);

            for (Household household : householdList) {

                householdModel.addRow(new Object[]{
                    household.getHouseholdId(),
                    household.getHouseholdNo(),
                    household.getEconomicStatus()
                });
            }

            System.out.println("DEBUG: loaded " + householdList.size()
                    + " households for sitio " + sitio);

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    view,
                    ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            loadingData = false;
        }
    }

    /**
     * Shows the residents of the selected household in the right table.
     * The household ID is read from the table itself (column 0).
     */
    private void loadResidents() {

        System.out.println("DEBUG 1: selection listener fired");

        int selectedRow
                = view.getHouseholdsTbl().getSelectedRow();

        System.out.println("DEBUG 2: selectedRow = " + selectedRow);

        if (selectedRow < 0) {
            return;
        }

        try {

            int modelRow
                    = view.getHouseholdsTbl()
                            .convertRowIndexToModel(selectedRow);

            Object idObj
                    = householdModel.getValueAt(modelRow, 0);

            System.out.println("DEBUG 3: household id = " + idObj);

            residentModel.setRowCount(0);

            // Unsaved row: no ID yet, so it has no residents
            if (idObj == null) {
                return;
            }

            residentList
                    = residentsDao.getByHouseholdId(
                            Integer.parseInt(idObj.toString())
                    );

            System.out.println("DEBUG 4: residents found = "
                    + residentList.size());

            for (Residents resident : residentList) {

                residentModel.addRow(new Object[]{
                    resident.getResidentId(),
                    resident.getFullName()
                });
            }

            System.out.println("DEBUG 5: rows now in residents table = "
                    + residentModel.getRowCount());

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    view,
                    ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void insertHousehold() {

        if (currentSitio.isBlank()) {

            JOptionPane.showMessageDialog(
                    view,
                    "Please select a sitio first."
            );

            return;
        }

        try {

            String householdNo
                    = JOptionPane.showInputDialog(
                            view,
                            "Enter Household Number:"
                    );

            if (householdNo == null
                    || householdNo.isBlank()) {

                return;
            }

            String economicStatus
                    = JOptionPane.showInputDialog(
                            view,
                            "Enter Economic Status:"
                    );

            if (economicStatus == null
                    || economicStatus.isBlank()) {

                return;
            }

            Household household
                    = new Household();

            household.setHouseholdNo(householdNo);
            household.setSitio(currentSitio);
            household.setEconomicStatus(economicStatus);

            householdDao.insertHousehold(household);

            loadSitio(currentSitio);
            refreshDashboard();

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    view,
                    ex.getMessage()
            );
        }
    }

    private void updateHousehold() {

        int row
                = view.getHouseholdsTbl().getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    view,
                    "Please select a household record first."
            );

            return;
        }

        int answer
                = JOptionPane.showConfirmDialog(
                        view,
                        "Save the changes made to this household record?",
                        "Confirm Update",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int modelRow
                    = view.getHouseholdsTbl()
                            .convertRowIndexToModel(row);

            Object idObj
                    = householdModel.getValueAt(modelRow, 0);

            Household household
                    = new Household();

            String householdNo
                    = String.valueOf(
                            householdModel.getValueAt(modelRow, 1)
                    );

            String economicStatus
                    = String.valueOf(
                            householdModel.getValueAt(modelRow, 2)
                    );

            if (idObj == null) {

                household.setHouseholdNo(householdNo);
                household.setEconomicStatus(economicStatus);
                household.setSitio(currentSitio);

                householdDao.insertHousehold(household);

                // A new household was added, so the count changed
                refreshDashboard();

                JOptionPane.showMessageDialog(
                        view,
                        "New household record inserted successfully."
                );
            } else {

                household.setHouseholdId(
                        Integer.parseInt(idObj.toString())
                );

                household.setHouseholdNo(householdNo);
                household.setEconomicStatus(economicStatus);

                householdDao.updateHousehold(household);

                // No dashboard refresh: editing doesn't change any count
                JOptionPane.showMessageDialog(
                        view,
                        "Household record updated successfully."
                );
            }

            hasUnsavedChanges = false;

            view.getUpdateHouseholdBtn()
                    .setEnabled(false);

            loadSitio(currentSitio);

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    view,
                    ex.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void deleteHousehold() {

        int selectedRow
                = view.getHouseholdsTbl().getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    view,
                    "Please select a household record first."
            );

            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to delete this household?\n\n"
                + "Warning: If residents are still assigned to this household, "
                + "the deletion may fail.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            int modelRow
                    = view.getHouseholdsTbl()
                            .convertRowIndexToModel(selectedRow);

            Object id
                    = householdModel.getValueAt(modelRow, 0);

            if (id == null) {

                // Unsaved row: never reached the database, no refresh needed
                householdModel.removeRow(modelRow);

                JOptionPane.showMessageDialog(
                        view,
                        "Unsaved row removed."
                );

                return;
            }

            householdDao.deleteHousehold(
                    Integer.parseInt(id.toString())
            );

            JOptionPane.showMessageDialog(
                    view,
                    "Household deleted successfully."
            );

            loadSitio(currentSitio);
            refreshDashboard();

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    view,
                    "Unable to delete household.\n\n"
                    + "Residents may still be assigned to it.\n\n"
                    + ex.getMessage(),
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}