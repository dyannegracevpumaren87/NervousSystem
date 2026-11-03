
package com.mycompany.healthinformationsystem;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HealthController {
   HealthView hv;
   
    public HealthController(HealthView temp){
        hv = temp;
        hv.allListeners(new AllActions());
    }
    class AllActions implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
       
        }
        
    }
}
