package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ModbusSetpoint
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class ModbusSetpoint implements Concept {

   /**
* Protege name: setpointValue
   */
   private float setpointValue;
   public void setSetpointValue(float value) { 
    this.setpointValue=value;
   }
   public float getSetpointValue() {
     return this.setpointValue;
   }

   /**
* Protege name: registerIndex
   */
   private int registerIndex;
   public void setRegisterIndex(int value) { 
    this.registerIndex=value;
   }
   public int getRegisterIndex() {
     return this.registerIndex;
   }

}
