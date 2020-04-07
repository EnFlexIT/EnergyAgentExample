package hygrid.globalDataModel.blackboard;

import java.util.HashMap;

import hygrid.globalDataModel.ontology.CableState;
import hygrid.globalDataModel.ontology.ElectricalNodeState;

/**
 * The Class PowerFlowCalculationResultAnswer represents an extended {@link AbstractBlackoardAnswer}.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PowerFlowCalculationResultAnswer extends AbstractBlackoardAnswer {

	private static final long serialVersionUID = -679778118583726548L;

	private HashMap<String, ElectricalNodeState> graphNodeStates;
	private HashMap<String, CableState> networkComponentStates;

	
	/**
	 * Instantiates a new power flow calculation result answer for a {@link BlackboardRequest}.
	 *
	 * @param graphNodeStates the graph node states
	 * @param networkComponentStates the network component states
	 */
	public PowerFlowCalculationResultAnswer(HashMap<String, ElectricalNodeState> graphNodeStates, HashMap<String, CableState> networkComponentStates) {
		this.graphNodeStates = graphNodeStates;
		this.networkComponentStates = networkComponentStates;
	}
	
	
	/**
	 * Gets the graph node states.
	 * @return the graph node states
	 */
	public HashMap<String, ElectricalNodeState> getGraphNodeStates() {
		return graphNodeStates;
	}
	/**
	 * Sets the graph node states.
	 * @param graphNodeStates the graph node states
	 */
	public void setGraphNodeStates(HashMap<String, ElectricalNodeState> graphNodeStates) {
		this.graphNodeStates = graphNodeStates;
	}
	
	
	/**
	 * Gets the network component states.
	 * @return the network component states
	 */
	public HashMap<String, CableState> getNetworkComponentStates() {
		return networkComponentStates;
	}
	/**
	 * Sets the network component states.
	 * @param networkComponentStates the network component states
	 */
	public void setNetworkComponentStates(HashMap<String, CableState> networkComponentStates) {
		this.networkComponentStates = networkComponentStates;
	}
	
}