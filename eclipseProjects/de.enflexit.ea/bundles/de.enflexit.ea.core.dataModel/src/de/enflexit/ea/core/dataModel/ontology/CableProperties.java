package de.enflexit.ea.core.dataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CableProperties
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class CableProperties extends EdgeComponentProperties{ 

   /**
* Protege name: dim
   */
   private String dim;
   public void setDim(String value) { 
    this.dim=value;
   }
   public String getDim() {
     return this.dim;
   }

   /**
* Protege name: linearResistance
   */
   private UnitValue linearResistance;
   public void setLinearResistance(UnitValue value) { 
    this.linearResistance=value;
   }
   public UnitValue getLinearResistance() {
     return this.linearResistance;
   }

   /**
* Protege name: linearReactance
   */
   private UnitValue linearReactance;
   public void setLinearReactance(UnitValue value) { 
    this.linearReactance=value;
   }
   public UnitValue getLinearReactance() {
     return this.linearReactance;
   }

   /**
* Protege name: maxCurrent
   */
   private UnitValue maxCurrent;
   public void setMaxCurrent(UnitValue value) { 
    this.maxCurrent=value;
   }
   public UnitValue getMaxCurrent() {
     return this.maxCurrent;
   }

   /**
* Protege name: din
   */
   private String din;
   public void setDin(String value) { 
    this.din=value;
   }
   public String getDin() {
     return this.din;
   }

   /**
   * The linear capacitance of this medium voltage cable.
* Protege name: linearCapacitance
   */
   private UnitValue linearCapacitance;
   public void setLinearCapacitance(UnitValue value) { 
    this.linearCapacitance=value;
   }
   public UnitValue getLinearCapacitance() {
     return this.linearCapacitance;
   }

   /**
* Protege name: length
   */
   private UnitValue length;
   public void setLength(UnitValue value) { 
    this.length=value;
   }
   public UnitValue getLength() {
     return this.length;
   }

   /**
   * The linear conductance of this medium voltage cable.
* Protege name: linearConductance
   */
   private UnitValue linearConductance;
   public void setLinearConductance(UnitValue value) { 
    this.linearConductance=value;
   }
   public UnitValue getLinearConductance() {
     return this.linearConductance;
   }

}
