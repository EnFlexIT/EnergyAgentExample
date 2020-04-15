package de.enflexit.ea.core.centralExecutiveAgent.behaviour;

import java.util.Date;

import de.enflexit.ea.core.centralExecutiveAgent.CentralExecutiveAgent;
import de.enflexit.ea.core.dataModel.PlatformUpdater;
import jade.core.behaviours.WakerBehaviour;

/**
 * The Class PlatformUpdateBehaviour is used to check for or for updating the 
 * underlying Agent.Workbench platform and its installed features.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class PlatformUpdateBehaviour extends WakerBehaviour {

	private static final long serialVersionUID = -7488509213368577104L;

	private CentralExecutiveAgent cea;
	private boolean enforceUpdate;
	
	/**
	 * Instantiates a new platform update behaviour.
	 *
	 * @param cea the instance of the CentralExecutiveAgent
	 * @param wakeUpTime the wake up time
	 */
	public PlatformUpdateBehaviour(CentralExecutiveAgent cea, Date wakeUpTime) {
		super(cea, wakeUpTime);
		this.cea = cea;
	}
	
	/**
	 * Sets the enforce update.
	 * @param enforceUpdate the new enforce update
	 */
	public void setEnforceUpdate(boolean enforceUpdate) {
		this.enforceUpdate = enforceUpdate;
	}
	/**
	 * Checks if is enforce update.
	 * @return true, if is enforce update
	 */
	public boolean isEnforceUpdate() {
		return enforceUpdate;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.behaviours.WakerBehaviour#onWake()
	 */
	@Override
	protected void onWake() {
		
		try {
			// --- Check for p2 updates -----------------------------
			PlatformUpdater.getInstance().executeAWBUpdateCheck(true, this.isEnforceUpdate());
			// --- Check for project updates ------------------------
			PlatformUpdater.getInstance().executeProjectUpdateCheck(this.isEnforceUpdate());
			
		} catch (Exception ex) {
			System.err.println("[" + this.cea.getLocalName() + "] Error while checking for updates:");
			ex.printStackTrace();
			
		} finally {
			// --- Start a new RepositoryMirrorBehaviour ------------
			this.cea.startNewPlatformUpdateBehaviour();
			
		}
	}
	
}
