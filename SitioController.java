package com.mycompany.healthinforamtionsystem.Sitio;

import com.mycompany.healthinformationsystem.HealthView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;

public class SitioController {

    private final HealthView hv;

    private final SitioDao dao;

    private final DefaultTableModel model;

    public SitioController(
            HealthView temp) {

        hv = temp;

        dao = new SitioDao();

        model =
            new DefaultTableModel(
                new String[]{

                    "HOUSEHOLD ID",
                    "House No.",
                    "Economic Status"

                },0);

        hv.HouseholdTbl.setModel(
                model);

        hv.IlawodBtn.addActionListener(
                new SitioAction(
                        "Ilawod"));

        hv.KalubihanBtn.addActionListener(
                new SitioAction(
                        "Kalubihan"));

        hv.KanioganBtn.addActionListener(
                new SitioAction(
                        "Kaniogan"));

        hv.PangpangBtn.addActionListener(
                new SitioAction(
                        "Pang-Pang"));

        hv.ProperBtn.addActionListener(
                new SitioAction(
                        "Proper"));
    }

    private void loadSitio(
            String sitio) {

        model.setRowCount(0);

        for(SitioModel sh :
                dao.getHouseholdsBySitio(
                        sitio)) {

            model.addRow(
                new Object[]{

                    sh.getHouseholdId(),
                    sh.getHouseNo(),
                    sh.getEconomicStatus()
                });
        }
    }

    class SitioAction
            implements ActionListener {

        private final String sitio;

        public SitioAction(
                String sitio) {

            this.sitio = sitio;
        }

        @Override
        public void actionPerformed(
                ActionEvent e) {

            loadSitio(sitio);
        }
    }
}