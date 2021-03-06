package de.enflexit.ea.core.dataModel.opsOntology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: LiveMonitoringUpdate
* @author ontology bean generator
* @version 2020/09/18, 14:36:59
*/
public class LiveMonitoringUpdate implements AgentAction {

   /**
   * The local name of the agent originally sending this data
* Protege name: agentID
   */
   private String agentID;
   public void setAgentID(String value) { 
    this.agentID=value;
   }
   public String getAgentID() {
     return this.agentID;
   }

   /**
   * The new TSSE sent by the agent, encoded as Base64 string
* Protege name: newTsseBase64
   */
   private String newTsseBase64;
   public void setNewTsseBase64(String value) { 
    this.newTsseBase64=value;
   }
   public String getNewTsseBase64() {
     return this.newTsseBase64;
   }

}
