package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ElectricalPower
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class ElectricalPower extends Power{ 

   /**
* Protege name: current
   */
   private UnitValue current;
   public void setCurrent(UnitValue value) { 
    this.current=value;
   }
   public UnitValue getCurrent() {
     return this.current;
   }

   /**
* Protege name: voltageAbs
   */
   private UnitValue voltageAbs;
   public void setVoltageAbs(UnitValue value) { 
    this.voltageAbs=value;
   }
   public UnitValue getVoltageAbs() {
     return this.voltageAbs;
   }

}