package de.enflexit.ea.core.dataModel.blackboard;

import java.io.Serializable;

/**
 * The Class RequestSpecifier can be used in order to specify what information are needed.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class SingleRequestSpecifier implements Serializable {
	
	private static final long serialVersionUID = 7973526480931244163L;
	
	/**The Enumeration RequestType. */
	public enum RequestType {
		SingleRequest,
		SubscriptionRequest
	}
	
	private RequestObjective requestObjective;
	private String identifier;
	
	
	/**
	 * Instantiates a new request specifier.
	 *
	 * @param requestObjective the request objective
	 * @param identifier the identifier for the required object
	 */
	public SingleRequestSpecifier(RequestObjective requestObjective, String identifier) {
		this.requestObjective = requestObjective;
		this.identifier = identifier;
	}
	/**
	 * Returns the request objective.
	 * @return the requestObjective
	 */
	public RequestObjective getRequestObjective() {
		return requestObjective;
	}
	/**
	 * Sets the request objective.
	 * @param requestObjective the requestObjective to set
	 */
	public void setRequestObjective(RequestObjective requestObjective) {
		this.requestObjective = requestObjective;
	}

	/**
	 * Returns the identifier.
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * Sets the identifier.
	 * @param identifier the new identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}