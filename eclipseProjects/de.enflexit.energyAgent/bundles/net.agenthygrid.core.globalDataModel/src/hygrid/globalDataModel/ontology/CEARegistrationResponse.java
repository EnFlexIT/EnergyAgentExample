package hygrid.globalDataModel.ontology;

import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
   * The response that is send to registered agents by the CEA
* Protege name: CEARegistrationResponse
* @author ontology bean generator
* @version 2020/01/29, 12:06:04
*/
public class CEARegistrationResponse extends TestbedAgentManagement{ 

   /**
   * The AID of the registered agent's counterpart (i.e. the proxy agent for a remote agent and vice versa)
* Protege name: counterpartAID
   */
   private AID counterpartAID;
   public void setCounterpartAID(AID value) { 
    this.counterpartAID=value;
   }
   public AID getCounterpartAID() {
     return this.counterpartAID;
   }

}