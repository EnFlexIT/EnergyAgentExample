package de.enflexit.ea.core.dataModel.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: NetworkStateInformation
* @author ontology bean generator
* @version 2021/02/15, 12:09:03
*/
public class NetworkStateInformation implements AgentAction {

   /**
* Protege name: timeStamp
   */
   private LongValue timeStamp;
   public void setTimeStamp(LongValue value) { 
    this.timeStamp=value;
   }
   public LongValue getTimeStamp() {
     return this.timeStamp;
   }

}
