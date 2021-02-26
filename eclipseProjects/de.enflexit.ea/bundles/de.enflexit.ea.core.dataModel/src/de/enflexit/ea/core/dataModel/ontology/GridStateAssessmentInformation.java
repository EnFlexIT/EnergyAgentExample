package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: GridStateAssessmentInformation
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class GridStateAssessmentInformation extends NetworkStateInformation{ 

//////////////////////////// User code
public GridStateAssessment getGridStateAssessmentNotNull() {
	if (this.gridStateAssessment == null) this.gridStateAssessment = new GridStateAssessment(); 
	return this.gridStateAssessment;
}
   /**
* Protege name: voltageRange
   */
   private VoltageBand voltageRange;
   public void setVoltageRange(VoltageBand value) { 
    this.voltageRange=value;
   }
   public VoltageBand getVoltageRange() {
     return this.voltageRange;
   }

   /**
* Protege name: gridStateAssessment
   */
   private GridStateAssessment gridStateAssessment;
   public void setGridStateAssessment(GridStateAssessment value) { 
    this.gridStateAssessment=value;
   }
   public GridStateAssessment getGridStateAssessment() {
     return this.gridStateAssessment;
   }

}
