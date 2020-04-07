package de.enflexit.energyAgent.core;

import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;

import de.enflexit.energyAgent.core.behaviour.PlatformUpdateBehaviour;
import de.enflexit.energyAgent.core.monitoring.IOListFilterForLogging;
import energy.FixedVariableList;
import energy.OptionModelController;
import energy.optionModel.TechnicalSystem;
import energy.persistence.TechnicalSystem_BundleLoader.BundleModel;
import energy.schedule.ScheduleController;
import energygroup.GroupController;
import hygrid.deployment.dataModel.AgentOperatingMode;
import hygrid.deployment.dataModel.AgentSpecifier;
import hygrid.deployment.dataModel.DeploymentGroupsHelper;
import hygrid.deployment.dataModel.SetupExtension;
import hygrid.env.HyGridAbstractEnvironmentModel;
import hygrid.globalDataModel.DirectoryHelper;
import hygrid.globalDataModel.DirectoryHelper.DirectoryType;
import hygrid.globalDataModel.PlatformUpdater;
import hygrid.globalDataModel.cea.CeaConfigModel;
import hygrid.globalDataModel.phonebook.PhoneBook;
import hygrid.globalDataModel.phonebook.PhoneBookEntry;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Serializable;


/**
 * The abstract Class AbstractInternalDataModel represents  
 * the whole internal data model of the corresponding agent.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public abstract class AbstractInternalDataModel extends Observable implements Serializable {

	private static final long serialVersionUID = 8589606262871989270L;
	
	/** The Enumeration of elements that could have been changed. */
	public static enum CHANGED {
		NETWORK_MODEL,
		NETWORK_COMPONENT,
		MEASUREMENTS_FROM_SYSTEM,
		TECHNICAL_SYSTEM_STATE_EVALUATION,
		PHONE_BOOK
	}
	
	/** The Enumeration of ControlledSystemType's. */
	public static enum ControlledSystemType {
		None,
		TechnicalSystem,
		TechnicalSystemGroup
	}
	
	/**
	 * Possible logging modes.
	 * ON_NEW_MEASUREMENT: Every new measurement will be logged
	 * ON_ANY_CHANGE: New measurements with unchanged values will be ignored
	 * ON_SIGNIFICANT_CHANGE: Changes below a certain threshold will be ignored. Thresholds can be configured using 
	 * the {@link IOListFilterForLogging}'s setGeneralThreshold() and addSpecificThresholdForVariable() methods
	 */
	public static enum LoggingMode{
		ON_NEW_MEASUREMENT,
		ON_VALUE_CHANGE
	}
	
	protected transient AbstractEnergyAgent myAgent;

	private HyGridAbstractEnvironmentModel hyGridAbstractEnvironmentModel;
	private NetworkModel networkModel;
	private NetworkComponent networkComponent;

	private ScheduleController scheduleController;
	private OptionModelController optionModelController;
	private GroupController groupController;
 
	private ControlledSystemType controlledSystemType = ControlledSystemType.None;
	
	private FixedVariableList fixedVariableListMeasurements;
	
	private PhoneBook phoneBook;
	
	private AID centralAgentAID;
	private CeaConfigModel ceaConfigModel;
	
	private IOListFilterForLogging loggingFilter;
	private LoggingMode loggingMode = LoggingMode.ON_NEW_MEASUREMENT;
	
	private ACLMessage fieldDataRequestMessage;
	
	/**
	 * Instantiates the internal data model for an AbstractEnergyAgent.
	 * @param myAgent the actual AbstractEnergyAgent
	 */
	public AbstractInternalDataModel(AbstractEnergyAgent myAgent) {
		this.myAgent = myAgent;
	}
	/**
	 * Sets this object changed and notifies observer.
	 * @param reason the concrete reason why the model changed
	 */
	protected void setChangedAndNotify(Object reason) {
		this.setChanged();
		this.notifyObservers(reason);		
	}
	
	
	/**
	 * Sets the HyGrid abstract environment model.
	 * @param hyGridAbstractEnvironmentModel the new {@link HyGridAbstractEnvironmentModel}
	 */
	public void setHyGridAbstractEnvironmentModel(HyGridAbstractEnvironmentModel hyGridAbstractEnvironmentModel) {
		this.hyGridAbstractEnvironmentModel = hyGridAbstractEnvironmentModel;
	}
	/**
	 * Returns the HyGridAbstractEnvironmentModel.
	 * @return the HyGridAbstractEnvironmentModel 
	 */
	public HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		return hyGridAbstractEnvironmentModel;
	}

	
	/**
	 * Sets the network model and will automatically determine the NetworkComponent.
	 * @param networkModel the new network model
	 */
	public void setNetworkModel(NetworkModel networkModel) {
		this.setNetworkModel(networkModel, true);
	}
	/**
	 * Sets the network model and can additionally determine the NetworkComponent.
	 *
	 * @param networkModel the new network model
	 * @param isDetermineNetworkComponent set true, if the NetworkComponent should be determined automatically
	 */
	public void setNetworkModel(NetworkModel networkModel, boolean isDetermineNetworkComponent) {
		this.networkModel = networkModel;
		this.setChangedAndNotify(CHANGED.NETWORK_MODEL);
		if (isDetermineNetworkComponent==true && this.networkModel!=null) {
			this.setNetworkComponent(this.networkModel.getNetworkComponent(this.myAgent.getLocalName()));
		}
	}
	/**
	 * Returns the network model.
	 * @return the network model
	 */
	public NetworkModel getNetworkModel() {
		return networkModel;
	}
	
	/**
	 * Sets the NetworkComponent.
	 * @param newNetworkComponent the new NetworkComponent
	 */
	public void setNetworkComponent(NetworkComponent newNetworkComponent) {
		if (this.networkComponent!=newNetworkComponent) {
			this.networkComponent = newNetworkComponent;
			this.setChangedAndNotify(CHANGED.NETWORK_COMPONENT);	
		}
	}
	/**
	 * Returns the current NetworkComponent.
	 * @return the NetworkComponent
	 */
	public NetworkComponent getNetworkComponent() {
		if (this.networkComponent==null && this.networkModel!=null) {
			this.setNetworkComponent(this.networkModel.getNetworkComponent(this.myAgent.getLocalName()));
		}
		return this.networkComponent;
	}
	
	/**
	 * Returns the ScheduleController for the underlying system.
	 * @return the ScheduleController
	 */
	public ScheduleController getScheduleController() {
		if (scheduleController == null) {
			scheduleController = new ScheduleController();
			this.controlledSystemType = ControlledSystemType.None;
		}
		return scheduleController;
	}
	/**
	 * Returns the OptionModelController for the underlying system.
	 * @return the OptionModelController
	 */
	public OptionModelController getOptionModelController() {
		if (optionModelController == null) {
			optionModelController = new OptionModelController();
			optionModelController.setControllingAgent(this.myAgent);
			if (this.getBundleModelForTechnicalSystem()!=null) {
				this.loadTechnicalSystemFromBundle(optionModelController, this.getBundleModelForTechnicalSystem());
			}
			this.controlledSystemType = ControlledSystemType.TechnicalSystem;
		}
		return optionModelController;
	}
	/**
	 * Returns the bundle model specification. Overwrite, if you want to assign a 
	 * bundle located EOM-TechnicalSystem file to the current agent.
	 * @return the bundle model description
	 */
	protected BundleModel getBundleModelForTechnicalSystem() {
		return null;
	}
	/**
	 * Loads a TechnicalSystem as specified into the specified {@link OptionModelController}.
	 *
	 * @param omc the {@link OptionModelController} with which the 
	 * @param bundleMode the bundle mode
	 * @return the technical system
	 */
	public TechnicalSystem loadTechnicalSystemFromBundle(OptionModelController omc, BundleModel bundleModel) {
		if (omc==null || bundleModel==null) return null;
		omc.loadTechnicalSystemFromBundle(bundleModel.getBundleName(), bundleModel.getFileReference(), null);
		return omc.getTechnicalSystem();
	}
	
	/**
	 * Returns the GroupController for the underlying system.
	 * @return the GroupController
	 */
	public GroupController getGroupController() {
		if (groupController == null) {
			groupController = new GroupController();
			groupController.getGroupOptionModelController().setControllingAgent(this.myAgent);
			this.controlledSystemType = ControlledSystemType.TechnicalSystemGroup;
		}
		return groupController;
	}
	
	/**
	 * Returns the type of controlled system.
	 * @return the type of controlled system
	 */
	public ControlledSystemType getTypeOfControlledSystem() {
		return controlledSystemType;
	}
	
	/**
	 * Returns the specified file or directory.<br> 
	 * <b>Notice:</b> For the case {@link DirectoryType#SystemMonitoringFile}, the path and file name will be adjusted 
	 * to the current system time. The directory will correspond to the current month, while for each day a file is 
	 * considered (e.g. ./[projectBaseDir]/[localAgentName]/log/03/DAY_07_SystemMonitoring.bin for the 7th of March
	 * of a year). Thus, a ring memory for one year will be constructed.
	 *
	 * @param type the type of the directory or file
	 * @return the file or directory
	 */
	public File getFileOrDirectory(DirectoryType type) {
		return DirectoryHelper.getFileOrDirectory(type, this.myAgent.getAID());
	}
	
	
	/**
	 * Sets the measurements, coming from the underlying technical system.
	 * @param measurementVector the new measurements
	 */
	public void setMeasurementsFromSystem(FixedVariableList measurementVector) {
		this.fixedVariableListMeasurements = measurementVector;
		this.setChangedAndNotify(CHANGED.MEASUREMENTS_FROM_SYSTEM);
	}
	
	/**
	 * Returns the current measurements from the underlying system.
	 * @return the measurements
	 */
	public FixedVariableList getMeasurementsFromSystem() {
		return this.fixedVariableListMeasurements;
	}
	
	
	/**
	 * Returns the agent's local phone book.
	 * @return the phone book
	 */
	public PhoneBook getPhoneBook() {
		if (phoneBook==null) {
			// --- For the real application of the energy agent -----
			if (this.myAgent.getAgentOperatingMode()==AgentOperatingMode.RealSystem) {
				phoneBook = PhoneBook.loadPhoneBook(this.getFileOrDirectory(DirectoryType.PhoneBookFile));
			}
			// --- Backup solution, or in all other modes -----------
			if (phoneBook==null) {
				// --- Create temporary PhoneBook instance ---------- 
				phoneBook = new PhoneBook();
				if (this.myAgent.getAgentOperatingMode()==AgentOperatingMode.RealSystem) {
					System.out.println("[" + this.myAgent.getLocalName() + "] Created temporary phonebook!");
				}
			}
		}
		return phoneBook;
	}
	/**
	 * Gets an AID from the phone book.
	 * @param localName the local name of the agent to look up
	 * @return the agent's AID, null if not found
	 */
	public AID getAidFromPhoneBook(String localName) {
		return this.getPhoneBook().getAgentAID(localName);
	}
	/**
	 * Adds an AID to the phone book.
	 * @param aid the AID
	 */
	public void addAidToPhoneBook(AID aid) {
		this.getPhoneBook().addAgentAID(aid);
		this.setChangedAndNotify(CHANGED.PHONE_BOOK);
	}
	
	/**
	 * Adds a single entry to phone book.
	 * @param entry the entry
	 */
	public void addEntryToPhoneBook(PhoneBookEntry entry) {
		this.getPhoneBook().addPhoneBookEntry(entry);
		this.setChangedAndNotify(CHANGED.PHONE_BOOK);
	}
	
	/**
	 * Adds a list of entries to phone book.
	 * @param entries the entries
	 */
	public void addEntriesToPhoneBook(List<PhoneBookEntry> entries) {
		for (int i=0; i<entries.size(); i++) {
			this.getPhoneBook().addPhoneBookEntry(entries.get(i));
		}
		this.setChangedAndNotify(CHANGED.PHONE_BOOK);
	}
	
	/**
	 * Removes an AID from the phone book.
	 * @param aid the aid
	 */
	public void removeAIDFromPhoneBook(AID aid) {
		this.getPhoneBook().removeAgentAID(aid);
		this.setChangedAndNotify(CHANGED.PHONE_BOOK);
	}
	
	/**
	 * Gets the agent's own phone book entry.
	 * @return the agent's own phone book entry
	 */
	public PhoneBookEntry getMyPhoneBookEntry() {
		PhoneBookEntry ownPhoneBookEntry = new PhoneBookEntry();
		ownPhoneBookEntry.setAID(myAgent.getAID());
		ownPhoneBookEntry.setComponentType(this.getNetworkComponent().getType());
		ownPhoneBookEntry.setControllable(false);
		return ownPhoneBookEntry;
	}
	
	/**
	 * Returns the {@link CeaConfigModel} if available. This is used to set the update behaviour 
	 * for the platform (e.g for repository locations or the update interval)
	 * @return the current CeaConfigModel (may be <code>null</code> also)
	 */
	public CeaConfigModel getCeaConfigModel() {
		return ceaConfigModel;
	}
	/**
	 * Sets the {@link CeaConfigModel}. Using this method, will also set the update sites (p2 and for the current project).
	 * Additionally, the {@link PlatformUpdateBehaviour} will be started according to set defined interval.
	 * @param ceaConfigModel the new CeaConfigModel
	 */
	public void setCeaConfigModel(CeaConfigModel ceaConfigModel) {
		if (ceaConfigModel!=null && (this.ceaConfigModel==null || this.ceaConfigModel.equals(ceaConfigModel)==false)) {
			// --- Set the new configuration model ------------------
			this.ceaConfigModel = ceaConfigModel;
			// --- Set update sites --------------------------------- 
			PlatformUpdater.getInstance().setUpdateSites(this.ceaConfigModel);
			if (PlatformUpdater.DEBUG_PLATFORM_UPDATE==true) {
				// --- Start the regular update behaviour ---------------
				this.myAgent.startPlatformUpdateBehaviourNow();
			} else {
				// --- Start the regular update behaviour ---------------
				this.myAgent.startNewPlatformUpdateBehaviour();
			}
		}
	}
	/**
	 * Returns the agent id of the CEA from the current NetworkModel.
	 * @return the agent id of CEA
	 */
	public String getAgentIdOfCea() {
		String agentIdCea = null;
		Vector<NetworkComponent> netCompVector = this.getNetworkModel().getNetworkComponentVectorSorted();
		for (int i = 0; i < netCompVector.size(); i++) {
			NetworkComponent netComp = netCompVector.get(i);
			String agentClassName = this.getNetworkModel().getAgentClassName(netComp);
			if (agentClassName!=null && agentClassName.equals(CeaConfigModel.CLASS_NAME_OF_CENTRAL_EXECUTIVE_AGENT)) {
				// --- Found the required NetworkComponent ------
				agentIdCea = netComp.getId();
				break;
			}
		}
		return agentIdCea;
	}
	
	/**
	 * Gets the central agent AID.
	 * @return the central agent AID
	 */
	public AID getCentralAgentAID() {
		if (centralAgentAID==null) {
		
			String agentIdOfCEA = this.getAgentIdOfCea();
			SetupExtension setEx = this.getHyGridAbstractEnvironmentModel().getSetupExtension();
			AgentSpecifier ceaSpecifier = this.getHyGridAbstractEnvironmentModel().getDeploymentSettingsModel().getCentralAgentSpecifier();
			
			if (setEx!=null) {
				DeploymentGroupsHelper dgh = setEx.getDeploymentGroupsHelper();
				boolean isDeployedCea = dgh.isAgentDeployed(agentIdOfCEA) && dgh.isDeploymentActivated(agentIdOfCEA);
				boolean isDeployedEnergyAgent = this.myAgent.getAgentOperatingMode()!=AgentOperatingMode.Simulation;
				boolean isAgentIdOfCeaNull = agentIdOfCEA==null; 
				if (isDeployedCea==false & isDeployedEnergyAgent==false & isAgentIdOfCeaNull==false) {
					// --- CEA and local agent were NOT deployed --------
					centralAgentAID = new AID(agentIdOfCEA, AID.ISLOCALNAME);
					
				} else if (isDeployedCea==false & isDeployedEnergyAgent==true) {
					// --- NOT sure where to find the CEA ---------------
					// --- Possibly take the setter later on ------------
					// --- => Current fallback solution -----------------
					centralAgentAID = ceaSpecifier.getAID();
					
				} else {
					// --- The CEA is deployed - take AgentSpecifier ----
					centralAgentAID = ceaSpecifier.getAID();
				}
			} else {
				centralAgentAID = ceaSpecifier.getAID();
			}
		}
		return centralAgentAID;
	}
	/**
	 * Sets the central agent AID.
	 * @param centralAgentAID the new central agent AID
	 */
	public void setCentralAgentAID(AID centralAgentAID) {
		this.centralAgentAID = centralAgentAID;
	}
	
	
	// -----------------------------------------------------
	// --- Methods for handling the logging mode -----------
	// -----------------------------------------------------
	
	/**
	 * Gets the logging mode.
	 * @return the logging mode
	 */
	public LoggingMode getLoggingMode() {
		return loggingMode;
	}
	/**
	 * Sets the logging mode.
	 * @param loggingMode the new logging mode
	 */
	public void setLoggingMode(LoggingMode loggingMode) {
		this.loggingMode = loggingMode;
	}
	
	/**
	 * Gets the logging filter. This implementation provides a default filter
	 * that reacts on any change of IO values. It can be further refined by defining
	 * general or variable-specific thresholds.
	 * @return the logging filter
	 */
	public IOListFilterForLogging getLoggingFilter() {
		if (loggingFilter==null && loggingMode==LoggingMode.ON_VALUE_CHANGE) {
			loggingFilter = new IOListFilterForLogging();
		}
		return loggingFilter;
	}
	
	/**
	 * Gets the field data request message.
	 * @return the field data request message
	 */
	public ACLMessage getFieldDataRequestMessage() {
		return fieldDataRequestMessage;
	}
	
	/**
	 * Sets the field data request message.
	 * @param fieldDataRequestMessage the new field data request message
	 */
	public void setFieldDataRequestMessage(ACLMessage fieldDataRequestMessage) {
		this.fieldDataRequestMessage = fieldDataRequestMessage;
	}
	
	
}
