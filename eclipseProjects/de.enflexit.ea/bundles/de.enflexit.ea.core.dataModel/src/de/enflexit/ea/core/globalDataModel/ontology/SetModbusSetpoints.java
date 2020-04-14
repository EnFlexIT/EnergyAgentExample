package de.enflexit.ea.core.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: SetModbusSetpoints
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class SetModbusSetpoints extends ModbusAdapterAction{ 

   /**
* Protege name: setpoints
   */
   private List setpoints = new ArrayList();
   public void addSetpoints(ModbusSetpoint elem) { 
     List oldList = this.setpoints;
     setpoints.add(elem);
   }
   public boolean removeSetpoints(ModbusSetpoint elem) {
     List oldList = this.setpoints;
     boolean result = setpoints.remove(elem);
     return result;
   }
   public void clearAllSetpoints() {
     List oldList = this.setpoints;
     setpoints.clear();
   }
   public Iterator getAllSetpoints() {return setpoints.iterator(); }
   public List getSetpoints() {return setpoints; }
   public void setSetpoints(List l) {setpoints = l; }

}