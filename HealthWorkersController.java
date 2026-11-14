package com.mycompany.healthinformationsystem.HealthWorkers;

import com.mycompany.healthinformationsystem.HealthView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class HealthWorkersController {

    private final HealthView hv;

    private final HealthWorkersDao dao;

    private final DefaultTableModel tableModel;

    private final List<Integer> rowIds =
            new ArrayList<>();

    private int selectedId = -1;

    public HealthWorkersController(
            HealthView temp) {

        hv = temp;

        dao = new HealthWorkersDao();

        hv.healthWorkerListeners(
                new AllActions());

        tableModel =
                new DefaultTableModel(
                        new String[]{
                            "Health Worker ID",
                            "First Name",
                            "Middle Name",
                            "Last Name",
                            "Suffix",
                            "Contact Number",
                            "Position"
                        }, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        hv.healthWorkerTbl.setModel(
                tableModel);

        hv.healthWorkerTbl
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        fillFieldsFromSelectedRow();
                    }
                });

        loadWorkerTable();
    }

    private void loadWorkerTable() {

        tableModel.setRowCount(0);

        rowIds.clear();

        for (HealthWorkers hw :
                dao.getAll()) {

            rowIds.add(
                    hw.getWorkerId());

            tableModel.addRow(
                    new Object[]{
                        hw.getHealthWorkerId(),
                        hw.getFirstName(),
                        hw.getMiddleName(),
                        hw.getLastName(),
                        hw.getSuffix(),
                        hw.getContactNumber(),
                        hw.getPosition()
                    });
        }
    }

    private void fillFieldsFromSelectedRow() {

        int row =
                hv.healthWorkerTbl.getSelectedRow();

        if (row < 0 ||
                row >= rowIds.size()) {

            return;
        }

        selectedId =
                rowIds.get(row);

        hv.HWID.setText(cell(row, 0));
        hv.HWFN.setText(cell(row, 1));
        hv.HWMN.setText(cell(row, 2));
        hv.HWLN.setText(cell(row, 3));
        hv.HWEX.setText(cell(row, 4));
        hv.HWNO.setText(cell(row, 5));
        hv.HWPOS.setText(cell(row, 6));
    }

    /** Returns the table cell as text, or "" if the cell is empty (null). */
    private String cell(int row, int column) {

        Object value = tableModel.getValueAt(row, column);

        return value == null ? "" : value.toString();
    }

    private boolean validateInput() {

        if (hv.HWID.getText()
                .trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    hv,
                    "Health Worker ID is required.");

            return false;
        }

        if (hv.HWFN.getText()
                .trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    hv,
                    "First Name is required.");

            return false;
        }

        if (hv.HWLN.getText()
                .trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                    hv,
                    "Last Name is required.");

            return false;
        }

        return true;
    }

    private HealthWorkers buildWorker() {

        HealthWorkers hw =
                new HealthWorkers();

        hw.setWorkerId(
                selectedId);

        hw.setHealthWorkerId(
                hv.HWID.getText().trim());

        hw.setFirstName(
                hv.HWFN.getText().trim());

        hw.setMiddleName(
                hv.HWMN.getText().trim());

        hw.setLastName(
                hv.HWLN.getText().trim());

        hw.setSuffix(
                hv.HWEX.getText().trim());

        hw.setContactNumber(
                hv.HWNO.getText().trim());

        hw.setPosition(
                hv.HWPOS.getText().trim());

        return hw;
    }

    private void insertWorker() {

        if (!validateInput()) {

            return;
        }

        if (dao.insert(
                buildWorker())) {

            JOptionPane.showMessageDialog(
                    hv,
                    "Health Worker added successfully.");

            loadWorkerTable();

            hv.refreshDashboard();

            clearFields();

        } else {

            JOptionPane.showMessageDialog(
                    hv,
                    "Insert failed.");
        }
    }

    private void updateWorker() {

        if (selectedId == -1) {

            JOptionPane.showMessageDialog(
                    hv,
                    "Please select a record.");

            return;
        }

        if (!validateInput()) {

            return;
        }

        if (dao.update(
                buildWorker())) {

            JOptionPane.showMessageDialog(
                    hv,
                    "Health Worker updated successfully.");

            loadWorkerTable();

            hv.refreshDashboard();

            clearFields();

        } else {

            JOptionPane.showMessageDialog(
                    hv,
                    "Update failed.");
        }
    }

    private void deleteWorker() {

        if (selectedId == -1) {

            JOptionPane.showMessageDialog(
                    hv,
                    "Please select a record.");

            return;
        }

        int result =
                JOptionPane.showConfirmDialog(
                        hv,
                        "Delete selected worker?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

        if (result !=
                JOptionPane.YES_OPTION) {

            return;
        }

        if (dao.delete(
                selectedId)) {

            JOptionPane.showMessageDialog(
                    hv,
                    "Health Worker deleted successfully.");

            loadWorkerTable();

            hv.refreshDashboard();

            clearFields();

        } else {

            JOptionPane.showMessageDialog(
                    hv,
                    "Delete failed.");
        }
    }

    private void clearFields() {

        hv.HWID.setText("");
        hv.HWFN.setText("");
        hv.HWMN.setText("");
        hv.HWLN.setText("");
        hv.HWEX.setText("");
        hv.HWNO.setText("");
        hv.HWPOS.setText("");

        hv.healthWorkerTbl.clearSelection();

        selectedId = -1;
    }

    class AllActions
            implements ActionListener {

        @Override
        public void actionPerformed(
                ActionEvent e) {

            Object source =
                    e.getSource();

            if (source ==
                    hv.Insert5) {

                insertWorker();

            } else if (source ==
                    hv.Update5) {

                updateWorker();

            } else if (source ==
                    hv.Delete5) {

                deleteWorker();
            }
        }
    }
}