package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: VoltageBand
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class VoltageBand implements Concept {

   /**
* Protege name: voltageMin
   */
   private float voltageMin;
   public void setVoltageMin(float value) { 
    this.voltageMin=value;
   }
   public float getVoltageMin() {
     return this.voltageMin;
   }

   /**
* Protege name: voltageMax
   */
   private float voltageMax;
   public void setVoltageMax(float value) { 
    this.voltageMax=value;
   }
   public float getVoltageMax() {
     return this.voltageMax;
   }

   /**
* Protege name: voltageAvg
   */
   private float voltageAvg;
   public void setVoltageAvg(float value) { 
    this.voltageAvg=value;
   }
   public float getVoltageAvg() {
     return this.voltageAvg;
   }

}