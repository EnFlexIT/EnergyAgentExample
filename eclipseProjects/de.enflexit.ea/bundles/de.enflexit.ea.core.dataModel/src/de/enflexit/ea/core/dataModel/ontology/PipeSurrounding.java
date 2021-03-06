package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: PipeSurrounding
* @author ontology bean generator
* @version 2021/02/26, 14:45:40
*/
public class PipeSurrounding implements Concept {

   /**
* Protege name: surroundingThermalConductivity
   */
   private float surroundingThermalConductivity;
   public void setSurroundingThermalConductivity(float value) { 
    this.surroundingThermalConductivity=value;
   }
   public float getSurroundingThermalConductivity() {
     return this.surroundingThermalConductivity;
   }

   /**
   * Surrounding temperature
* Protege name: surroundingTemperature
   */
   private UnitValue surroundingTemperature;
   public void setSurroundingTemperature(UnitValue value) { 
    this.surroundingTemperature=value;
   }
   public UnitValue getSurroundingTemperature() {
     return this.surroundingTemperature;
   }

}
