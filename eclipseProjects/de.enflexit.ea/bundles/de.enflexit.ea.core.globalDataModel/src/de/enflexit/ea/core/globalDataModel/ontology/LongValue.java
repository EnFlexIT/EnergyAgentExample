package de.enflexit.ea.core.globalDataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: LongValue
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class LongValue implements Concept {

//////////////////////////// User code
public Long getLongValue(){
 try{
  return Long.parseLong(getStringLongValue());
 }catch(NumberFormatException ex){
  return null;
 }
}
public void setLongValue(long value){
 setStringLongValue(""+value);
}
public void setLongValue(Long value){
 setStringLongValue(value.toString());
}
   /**
* Protege name: stringLongValue
   */
   private String stringLongValue;
   public void setStringLongValue(String value) { 
    this.stringLongValue=value;
   }
   public String getStringLongValue() {
     return this.stringLongValue;
   }

}