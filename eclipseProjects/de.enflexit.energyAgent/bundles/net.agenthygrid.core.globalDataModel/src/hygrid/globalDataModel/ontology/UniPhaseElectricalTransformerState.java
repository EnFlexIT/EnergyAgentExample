package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UniPhaseElectricalTransformerState
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class UniPhaseElectricalTransformerState extends UniPhaseElectricalNodeState{ 

   /**
* Protege name: ratedVoltage
   */
   private UnitValue ratedVoltage;
   public void setRatedVoltage(UnitValue value) { 
    this.ratedVoltage=value;
   }
   public UnitValue getRatedVoltage() {
     return this.ratedVoltage;
   }

}