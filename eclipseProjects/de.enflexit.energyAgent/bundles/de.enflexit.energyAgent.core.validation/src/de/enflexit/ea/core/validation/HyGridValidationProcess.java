package de.enflexit.ea.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import org.awb.env.networkModel.NetworkComponent;
import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.controller.GraphEnvironmentController;
import org.awb.env.networkModel.controller.NetworkModelNotification;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentFactory;

import agentgui.core.application.Application;
import agentgui.core.application.ApplicationListener;
import agentgui.core.application.ApplicationListener.ApplicationEvent;
import agentgui.core.environment.EnvironmentController;
import agentgui.core.project.Project;
import agentgui.core.project.setup.SimulationSetup;
import agentgui.core.project.setup.SimulationSetupNotification;
import agentgui.simulationService.environment.AbstractEnvironmentModel;
import de.enflexit.ea.core.globalDataModel.deployment.SetupExtension;
import de.enflexit.ea.core.globalDataModel.ontology.HyGridOntology;
import de.enflexit.ea.core.ops.plugin.OpsPlugin;
import de.enflexit.ea.core.validation.HyGridValidationMessage.MessageType;
import de.enflexit.ea.deployment.plugin.DeploymentPlugIn;
import energy.GlobalInfo;
import energy.optionModel.ScheduleList;
import energy.optionModel.TechnicalSystem;
import energy.optionModel.TechnicalSystemGroup;
import hygrid.env.HyGridAbstractEnvironmentModel;
import hygrid.ops.ontology.OpsOntology;
import hygrid.plugin.HyGridPlugIn;

/**
 * The singleton instance of the HyGridValidationProcess does the actual checks on a Energy Agent / HyGrid setup.
 * 
 * @author Christian Derksen - DAWIS - University Duisburg-Essen
 */
public class HyGridValidationProcess implements ApplicationListener, Observer {

	private enum ListenerInformation {
		Executed,
		MessageReceived,
		Finalized
	}
	
	private enum SingleConfigurationEntity {
		Project,
		Setup,
		NetworkModel,
		HyGridAbstractEnvironmentModel
	}
	
	private boolean isAddTestMessages = false;
	
	private List<HyGridValidationProcessListener> validationListener;
	private List<HyGridValidationMessage> hygridValidationMessages;

	// ----------------------------------------------------
	// --- Singleton construct ----------------------------
	// ----------------------------------------------------
	private static HyGridValidationProcess thisInstance;
	/** Private constructor for the HyGridValidationProcess. */
	private HyGridValidationProcess() { }
	/**
	 * Returns the single instance of the HyGridValidationProcess.
	 * @return single instance of HyGridValidationProcess
	 */
	public static HyGridValidationProcess getInstance() {
		if (thisInstance==null) {
			thisInstance = new HyGridValidationProcess();
			Application.addApplicationListener(thisInstance);
		}
		return thisInstance;
	}
	// ----------------------------------------------------
	
	/**
	 * Validates the current setup in an individual thread.
	 */
	public void validateCurrentSetupInThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HyGridValidationProcess.this.validateCurrentSetup();
			}
		}, this.getClass().getSimpleName()).start();
	}
	/**
	 * Validates the current setup in the current thread.
	 */
	public void validateCurrentSetup() {
		
		// --- Reset old messages -------------------------
		this.setHygridValidationMessages(null);
		
		// --- Check to access the project ----------------
		if (this.getProject()==null) {
			this.addMessage("Could not access any project !", MessageType.Error);
			return;
		}
		// --- Check to access the graphController --------
		if (this.getGraphController()==null) {
			this.addMessage("Could not access the projects GraphEnvironementController!", MessageType.Error);
			return;
		}

		// --- Execute the specific validation checks -----
		this.doHyGridChecks();
	}
	
	/**
	 * Does the HyGrid checks.
	 */
	private void doHyGridChecks() {
		
		long checkTimeStart = System.currentTimeMillis();
		
		// --- Inform listener about the execution ------------------
		this.informHyGridValidationProcessListener(ListenerInformation.Executed, null);
		
		// --- Get registered services ------------------------------
		List<HyGridValidationService> serviceList = this.getValidationServiceList();
		if (serviceList.size()==0) {
			this.addMessage("No " + HyGridValidationService.class.getSimpleName() + " was registered. - Exit validation process!", MessageType.Information);
			return;
		}
		// --- Get validation checks --------------------------------		
		List<HyGridValidationAdapter> validationList = this.getValidationList(serviceList);
		if (validationList.size()==0) {
			this.addMessage("No validation checks were registered to the " + this.getClass().getSimpleName() + ". - Exit validation process!", MessageType.Information);
			return;
		}
		
		// --- Print validation start message ----------------------- 
		String nServiceText  = serviceList.size() + " service";
		if (serviceList.size()>1) nServiceText += "s";
		String nValidationText = validationList.size() + " validation check";
		if (validationList.size()>1) nValidationText += "s";
		this.addMessage("Execute HyGrid-Checks with " + nServiceText + " and " + nValidationText + " ... ", MessageType.Information);


		// ----------------------------------------------------------
		// --- Add test messages ------------------------------------
		// ----------------------------------------------------------
		this.addTestMessages();
		
		// ----------------------------------------------------------
		// --- Do the single entity checks --------------------------
		// ----------------------------------------------------------
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.Project);
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.Setup);
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.NetworkModel);
		this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.HyGridAbstractEnvironmentModel);
		
		// ----------------------------------------------------------
		// --- Validate the configured EOM models -------------------
		// ----------------------------------------------------------
		this.checkNetworkComponents(validationList);
		
		
		// --- Set the finalized message ----------------------------
		long durationMS = System.currentTimeMillis() - checkTimeStart;
		double durationS = (double)durationMS / 1000.0;
		double durationMin = (double)durationS / 60.0;
		
		String durationText = null;
		if (durationMS<=1000) {
			durationText = durationMS + " ms";
		} else {
			if (durationS<=300) {
				durationText = GlobalInfo.round(durationS, 2) + " s";
			} else {
				durationText = GlobalInfo.round(durationMin , 2) + " min";
			}
		}
		this.addMessage("Done! - Finalized in " + durationText + ".", MessageType.Information);

		// --- Inform listener about the finalization ---------------
		this.informHyGridValidationProcessListener(ListenerInformation.Finalized, null);

	}
	
	/**
	 * Checks the current {@link NetworkComponent}s.
	 * @param validationList the validation list
	 */
	private void checkNetworkComponents(List<HyGridValidationAdapter> validationList) {
		
		NetworkModel networkModel = this.getGraphController().getNetworkModel();
		
		Vector<NetworkComponent> netCompList = networkModel.getNetworkComponentVectorSorted();
		for (int i = 0; i < netCompList.size(); i++) {
			// --- Check each NetworkComponent ----------------------
			NetworkComponent netComp = netCompList.get(i);
			TechnicalSystem ts = null;
			TechnicalSystemGroup tsg = null;
			ScheduleList sl = null;
			
			// --- Run over the list of validation classes ---------- 
			for (int v = 0; v < validationList.size(); v++) {
				
				HyGridValidationAdapter validator = validationList.get(v);
				String validatorClassName = validator.getClass().getName();
				try {
					// --- Call the validation methods --------------
					HyGridValidationMessage message = validator.validateNetworkComponent(netComp);
					this.addMessage(message, validator);

					// -- Check the data model of the component -------------
					Object dataModel = netComp.getDataModel();
					if (dataModel instanceof TechnicalSystem) {
						ts = (TechnicalSystem) dataModel;
					} else if (dataModel instanceof TechnicalSystemGroup) {
						tsg = (TechnicalSystemGroup) dataModel;
					} else if (dataModel instanceof ScheduleList) {
						sl = (ScheduleList)dataModel;
					}
					
					// --- Call validation methods for EOM model? ---
					if (ts!=null) {
						message = validator.validateEomTechnicalSystem(netComp, ts);
						this.addMessage(message, validator);
					}
					if (tsg!=null) {
						message = validator.validateEomTechnicalSystemGroup(netComp, tsg);
						this.addMessage(message, validator);
					}
					if (sl!=null) {
						message = validator.validateEomScheduleList(netComp, sl);
						this.addMessage(message, validator);
					}
					
				} catch (Exception ex) {
					// --- Create a message for the exception -----------
					HyGridValidationMessage errMessage = new HyGridValidationMessage("Exception in validation class '" + validatorClassName + "'", MessageType.Error);
					errMessage.setDescription(ex.getMessage());
					this.addMessage(errMessage);
					ex.printStackTrace();				
				}
			} // end inner for
		} // end outer for
	}
	
	/**
	 * Check single configuration entity.
	 *
	 * @param validationList the validation list
	 * @param configEntity the actual single configuration entity
	 * @param instanceToCheck the instance to check
	 */
	private void checkSingleConfigurationEntity(List<HyGridValidationAdapter> validationList, SingleConfigurationEntity configEntity) {
		this.checkSingleConfigurationEntity(validationList, configEntity, null);
	}
	/**
	 * Check single configuration entity.
	 *
	 * @param validationList the validation list
	 * @param configEntity the actual single configuration entity
	 * @param instanceToCheck the instance to check (e.g. a Project or a SimulationSetup - depends on the 'configEntity')
	 */
	private void checkSingleConfigurationEntity(List<HyGridValidationAdapter> validationList, SingleConfigurationEntity configEntity, Object instanceToCheck) {
		
		for (int i = 0; i < validationList.size(); i++) {
			
			HyGridValidationAdapter validator = validationList.get(i);
			String validatorClassName = validator.getClass().getName();
			try {
				// --- Call the validation method -------------------
				Project project = null;
				HyGridValidationMessage message = null;
				switch (configEntity) {
				case Project:
					project = this.getProject();
					if (instanceToCheck!=null && instanceToCheck instanceof Project) {
						project = (Project) instanceToCheck;
					}
					message = validator.validateProject(project);
					break;
					
				case Setup:
					project = this.getProject();
					SimulationSetup setup = null; 
					if (project!=null) {
						setup = project.getSimulationSetups().getCurrSimSetup();
					}
					if (instanceToCheck!=null && instanceToCheck instanceof SimulationSetup) {
						setup = (SimulationSetup) instanceToCheck;
					}
					message = validator.validateSetup(setup);
					break;
					
				case NetworkModel:
					message = validator.validateNetworkModel(this.getGraphController().getNetworkModel());
					break;
					
				case HyGridAbstractEnvironmentModel:
					message = validator.validateHyGridAbstractEnvironmentModel(this.getHyGridAbstractEnvironmentModel());
					break;
					
				}
				this.addMessage(message, validator);
				
			} catch (Exception ex) {
				// --- Create a message for the exception -----------
				HyGridValidationMessage errMessage = new HyGridValidationMessage("Exception in validation class '" + validatorClassName + "' while checking the " + configEntity.name() + ".", MessageType.Error);
				errMessage.setDescription(ex.getMessage());
				this.addMessage(errMessage);
				ex.printStackTrace();				
			}
		}
	}
	
	/**
	 * Returns the list of validation checks that were provided by the services.
	 *
	 * @param serviceList the service list
	 * @return the list of HyGridValidationAdapter
	 */
	private List<HyGridValidationAdapter> getValidationList(List<HyGridValidationService> serviceList) {
		return this.getValidationList(serviceList, true);
	}
	/**
	 * Returns the list of validation checks that were provided by the services.
	 *
	 * @param serviceList the service list
	 * @param setRuntimeInstancesToValidationAdapter the indicator to set the current runtime instances to the validation adapter
	 * @return the list of HyGridValidationAdapter
	 */
	private List<HyGridValidationAdapter> getValidationList(List<HyGridValidationService> serviceList, boolean setRuntimeInstancesToValidationAdapter) {
		
		List<HyGridValidationAdapter> validatorList = new ArrayList<>();
		
		// --- Walk through the list of specified services --------------------
		for (int i = 0; i < serviceList.size(); i++) {
			// --- Get the checks from the registered service -----------------
			HyGridValidationService validatorService = serviceList.get(i);
			ArrayList<HyGridValidationAdapter> serviceValidatorList = validatorService.getHyGridValidationChecks(this.isHeadlessOperation());
			if (serviceValidatorList==null) continue;
			
			for (int j = 0; j < serviceValidatorList.size(); j++) {
				// --- Add each single validation to list --------------------- 
				HyGridValidationAdapter validationAdapter = serviceValidatorList.get(j);
				if (setRuntimeInstancesToValidationAdapter==true) {
					// --- Set the required project instances to the adapter --
					validationAdapter.setProject(this.getProject());
					validationAdapter.setSetup(this.getProject().getSimulationSetups().getCurrSimSetup());
					validationAdapter.setGraphController(this.getGraphController());
					validationAdapter.setNetworkModel(this.getGraphController().getNetworkModel());
					validationAdapter.setHyGridAbstractEnvironmentModel(this.getHyGridAbstractEnvironmentModel());	
				}
				validatorList.add(validationAdapter);
			}
		}
		return validatorList;
	}
	/**
	 * Returns the validation service list.
	 * @return the validation service list
	 */
	private List<HyGridValidationService> getValidationServiceList() {
		
		List<HyGridValidationService> serviceList = new ArrayList<>();
		
		try {
			// --- Search for the registered validation services -------------- 
			BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
			String serviceFilter = "(" + Constants.OBJECTCLASS + "=" + HyGridValidationService.class.getName() + ")";
			ServiceReference<?>[] serviceReferences = bundleContext.getAllServiceReferences(null, serviceFilter);
			if (serviceReferences!=null) {
				for (int i = 0; i < serviceReferences.length; i++) {
					@SuppressWarnings("unchecked")
					ServiceReference<ComponentFactory<HyGridValidationService>> serviceRef = (ServiceReference<ComponentFactory<HyGridValidationService>>) serviceReferences[i];
					try {
						// --- get the service and add to result list ---------
						HyGridValidationService service = (HyGridValidationService)bundleContext.getService(serviceRef);
						if (serviceList!=null && serviceList.contains(service)==false) {
							serviceList.add(service);	
						}
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				
			} else {
				System.err.println("[" + this.getClass().getSimpleName() + "] : Could not find any " + HyGridValidationService.class.getSimpleName() + ".");
			}
		
		} catch (InvalidSyntaxException isEx) {
			isEx.printStackTrace();
		}
		return serviceList;
	}
	
	// ------------------------------------------------------------------------
	// --- From here, access to required runtime instances will be available --  
	// ------------------------------------------------------------------------
	/**
	 * Returns the current project.
	 * @return the project
	 */
	private Project getProject() {
		return Application.getProjectFocused();
	}
	/**
	 * Returns the current {@link GraphEnvironmentController}.
	 * @return the graph controller
	 */
	private GraphEnvironmentController getGraphController() {
		GraphEnvironmentController graphController = null; 	
		Project project = this.getProject(); 
		if (project!=null) {
			EnvironmentController envController = project.getEnvironmentController();
			if (envController instanceof GraphEnvironmentController) {
				graphController = (GraphEnvironmentController) envController;		
			}
		}
		return graphController;
	}
	/**
	 * Returns the current HyGridAbstractEnvironmentModel.
	 * @return the HyGridAbstractEnvironmentModel
	 */
	private HyGridAbstractEnvironmentModel getHyGridAbstractEnvironmentModel() {
		HyGridAbstractEnvironmentModel hygAbsEnvModel=null;
		GraphEnvironmentController graphController = this.getGraphController();
		if (graphController!=null) {
			AbstractEnvironmentModel absEnvModel = graphController.getAbstractEnvironmentModel();
			if (absEnvModel instanceof HyGridAbstractEnvironmentModel) {
				hygAbsEnvModel = (HyGridAbstractEnvironmentModel)absEnvModel;	
			} else {
				if (absEnvModel!=null) {
					this.addMessage("The current abstract environment model is not of type '" + HyGridAbstractEnvironmentModel.class.getSimpleName() + "'!", MessageType.Error);
				}
			}	
		}
		return hygAbsEnvModel;
	}
	/**
	 * Checks if is headless operation.
	 * @return true, if is headless operation
	 */
	private boolean isHeadlessOperation() {
		return Application.isOperatingHeadless();
	}

	// ------------------------------------------------------------------------
	// --- From here, handling of HyGridValidationProcessListener -------------  
	// ------------------------------------------------------------------------
	/**
	 * Gets the hy grid validation process listener.
	 * @return the hy grid validation process listener
	 */
	private List<HyGridValidationProcessListener> getHyGridValidationProcessListener() {
		if (validationListener==null) {
			validationListener = new ArrayList<>();
		}
		return validationListener;
	}
	/**
	 * Adds the specified HyGridValidationProcessListener.
	 * @param listener the HyGridValidationProcessListener to add
	 */
	public void addHyGridValidationProcessListener(HyGridValidationProcessListener listener) {
		if (listener==null) return;
		if (this.getHyGridValidationProcessListener().contains(listener)==true) return;
		this.getHyGridValidationProcessListener().add(listener);
	}
	/**
	 * Removes the specified HyGridValidationProcessListener.
	 * @param listener the HyGridValidationProcessListener to remove
	 */
	public void removeHyGridValidationProcessListener(HyGridValidationProcessListener listener) {
		if (listener==null) return;
		if (this.getHyGridValidationProcessListener().contains(listener)==false) return;
		this.getHyGridValidationProcessListener().remove(listener);
	}
	/**
	 * Inform HyGridValidationProcessListener about the specified event.
	 *
	 * @param info the kind of information to send to the listener
	 * @param message the message
	 */
	private void informHyGridValidationProcessListener(ListenerInformation info, HyGridValidationMessage message) {
		
		for (int i = 0; i < this.getHyGridValidationProcessListener().size(); i++) {
			HyGridValidationProcessListener listener = this.getHyGridValidationProcessListener().get(i);
			switch (info) {
			case Executed:
				listener.processExecuted();
				break;
			case MessageReceived:
				listener.messageReceived(message);
				break;
			case Finalized:
				listener.processFinalized();
				break;
			}
		}
	}
	
	// ------------------------------------------------------------------------
	// --- From here, handling of HyGridValidationMessage ---------------------  
	// ------------------------------------------------------------------------
	/**
	 * Returns the DefaultListModel with the errors found.
	 * @return the DefaultListModel with errors found.
	 */
	public List<HyGridValidationMessage> getHygridValidationMessages() {
		if (this.hygridValidationMessages==null) {
			this.hygridValidationMessages = new ArrayList<HyGridValidationMessage>();
		}
		return this.hygridValidationMessages;
	}
	/**
	 * Sets the list model messages.
	 * @param newDefaultListModel the new list model messages
	 */
	private void setHygridValidationMessages(ArrayList<HyGridValidationMessage> newDefaultListModel) {
		this.hygridValidationMessages = newDefaultListModel;
	}
	/**
	 * Adds a new {@link HyGridValidationMessage} to the local stack.
	 * @param newMessage the new message
	 * @param messageType the message type
	 */
	private void addMessage(String newMessage, MessageType messageType) {
		if (newMessage!=null) {
			this.addMessage(new HyGridValidationMessage(newMessage, messageType));
		}
	}
	/**
	 * Adds the specified {@link HyGridValidationMessage} to the local stack.
	 * @param newValidationMessage the new validation message
	 */
	private void addMessage(HyGridValidationMessage newValidationMessage) {
		if (newValidationMessage!=null) {
			this.getHygridValidationMessages().add(newValidationMessage);
			this.informHyGridValidationProcessListener(ListenerInformation.MessageReceived, newValidationMessage);
		}
	}
	/**
	 * Adds the specified {@link HyGridValidationMessage} to the local stack.
	 *
	 * @param newValidationMessage the new validation message
	 * @param validator the validator instance that produced the message
	 */
	private void addMessage(HyGridValidationMessage newValidationMessage, HyGridValidationAdapter validator) {
		if (newValidationMessage!=null) {
			newValidationMessage.setFoundByClass(validator.getClass().getName());
			this.getHygridValidationMessages().add(newValidationMessage);
			this.informHyGridValidationProcessListener(ListenerInformation.MessageReceived, newValidationMessage);
		}
	}
	
	/**
	 * Adds the test messages.
	 */
	private void addTestMessages() {
		
		if (isAddTestMessages==false) return;
		
		String description = "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		description +=  "This is a very long text .......\n";
		
		HyGridValidationMessage message = new HyGridValidationMessage("Test Information 1", MessageType.Information);
		message.setDescription(description);
		this.addMessage(message);

		this.addMessage("Test Information 2", MessageType.Information);
		
		this.addMessage("Test Warning 1", MessageType.Warning);
		this.addMessage("Test Warning 2", MessageType.Warning);
		
		this.addMessage("Test Error 1", MessageType.Error);
		this.addMessage("Test Error 2", MessageType.Error);
		
		this.addMessage("Test Information 3", MessageType.Information);
		this.addMessage("Test Warning 3", MessageType.Warning);
		this.addMessage("Test Error 3", MessageType.Error);
	}
	
	
	// ------------------------------------------------------------------------
	// --- From here, the application listener is implemented -----------------  
	// ------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see agentgui.core.application.ApplicationListener#onApplicationEvent(agentgui.core.application.ApplicationListener.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		
		Project project = null;
		switch (event.getApplicationEvent()) {
		case ApplicationEvent.PROJECT_LOADING_PROJECT_XML_FILE_LOADED:
			// --- Check project after XML file was loaded --------------------
			if (event.getEventObject()!=null && event.getEventObject() instanceof Project) {
				project = (Project) event.getEventObject();
				this.adjustProjectUserObjectClassName(project);
				this.adjustProjectPlugInReferences(project);
				this.adjustProjectOntologyReferences(project);
			}
			break;
			
		case ApplicationEvent.PROJECT_LOADING_PROJECT_FILES_LOADED:
			// ----------------------------------------------------------------
			// --- Forward Project check to registered services ---------------
			// ----------------------------------------------------------------
			if (event.getEventObject()!=null && event.getEventObject() instanceof Project) {
				// --- Inform services/validations about Project --------------
				project = (Project) event.getEventObject();
				List<HyGridValidationService> serviceList = this.getValidationServiceList();
				List<HyGridValidationAdapter> validationList = this.getValidationList(serviceList, false);
				for (int i = 0; i < validationList.size(); i++) {
					validationList.get(i).setProject(project);
				}
				this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.Project, project);
			}
			break;
			
		case ApplicationEvent.PROJECT_LOADING_SETUP_XML_FILE_LOADED:
			// --- Check SimulationSetup after XML file was loaded ------------
			if (event.getEventObject()!=null && event.getEventObject() instanceof SimulationSetup) {
				SimulationSetup setup = (SimulationSetup) event.getEventObject();
				this.adjustSetupUserObjectClassName(setup);
			}
			break;
			
		case ApplicationEvent.PROJECT_LOADING_SETUP_USER_FILE_LOADED:
			// ----------------------------------------------------------------
			// --- Forward SimulationSetup check to registered services -------
			// ----------------------------------------------------------------
			if (event.getEventObject()!=null && event.getEventObject() instanceof SimulationSetup) {
				// --- Inform services/validations about SimulationSetup ------
				SimulationSetup setup = (SimulationSetup) event.getEventObject();
				List<HyGridValidationService> serviceList = this.getValidationServiceList();
				List<HyGridValidationAdapter> validationList = this.getValidationList(serviceList, false);
				for (int i = 0; i < validationList.size(); i++) {
					validationList.get(i).setSetup(setup);;
				}
				this.checkSingleConfigurationEntity(validationList, SingleConfigurationEntity.Setup, setup);
			}
			break;
			
		case ApplicationEvent.PROJECT_LOADED:
			// --- Final event when loading a project -------------------------
			if (event.getEventObject()!=null && event.getEventObject() instanceof Project) {
				project = (Project) event.getEventObject();
				if (isHyGridProject(project)==true) {
					// --- Add project observer -------------------------------
					project.addObserver(this);
					// --- Add EnvController observer -------------------------
					EnvironmentController envController = project.getEnvironmentController();
					if (envController!=null) {
						envController.addObserver(this);
					}
				}
			}
			break;

		case ApplicationEvent.PROJECT_CLOSED:
			if (event.getEventObject()!=null) {
				project = (Project) event.getEventObject();
				// --- Remove EnvController observer --------------------------
				if (project.isEnvironmentControllerInitiated()==true) {
					project.getEnvironmentController().deleteObserver(this);
				}
				// --- Remove project observer --------------------------------
				project.deleteObserver(this);
			}
			break;
			
		} 
	}
	
	// ----------------------------------------------------
	// --- Feature rebuild conversions ---------- Start ---
	// ----------------------------------------------------
	/**
	 * Adjusts the projects user object class name according to the structure of the energy Agent feature.
	 * @param project the project to adjust
	 */
	private void adjustProjectUserObjectClassName(Project project) {
		if (project != null && project.getUserRuntimeObjectClassName().equals("hygrid.env.HyGridAbstractEnvironmentModel") == true) {
			project.setUserRuntimeObjectClassName(HyGridAbstractEnvironmentModel.class.getName());
		}
	}
	/**
	 * Adjusts the projects plug in references according to the structure of the energy Agent feature.
	 * @param project the project to adjust
	 */
	private void adjustProjectPlugInReferences(Project project) {
		if (project != null) {
			Vector<String> pluginClassNames = project.getPluginClassNames();
			for (int i = 0; i < pluginClassNames.size(); i++) {
				String pluginClassName = pluginClassNames.get(i);
				if (pluginClassName.equals("hygrid.plugin.HyGridPlugIn") == true) {
					pluginClassNames.set(i, HyGridPlugIn.class.getName());
				} else if (pluginClassName.equals("hygrid.deployment.plugin.DeploymentPlugIn") == true) {
					pluginClassNames.set(i, DeploymentPlugIn.class.getName());
				} else if (pluginClassName.equals("hygrid.ops.plugin.OpsPlugin") == true) {
					pluginClassNames.set(i, OpsPlugin.class.getName());
				}
			}
		}
	}
	/**
	 * Adjusts the projects ontology references according to the structure of the energy Agent feature.
	 * @param project the project to adjust
	 */
	private void adjustProjectOntologyReferences(Project project) {
		if (project != null) {
			Vector<String> ontoReferences = project.getSubOntologies();
			for (int i = 0; i < ontoReferences.size(); i++) {
				String ontoReference = ontoReferences.get(i);
				if (ontoReference.equals("hygrid.globalDataModel.ontology.HyGridOntology") == true) {
					ontoReferences.set(i, HyGridOntology.class.getName());
				} else if (ontoReference.equals("hygrid.ops.ontology.OpsOntology") == true) {
					ontoReferences.set(i, OpsOntology.class.getName());
				}
			}
		}
	}
//		/**
//		 * Adjusts the projects agent start configuration according to the structure of the energy Agent feature.
//		 * @param project the project to adjust
//		 */
//		private void adjustProjectAgentStartConfiguration(Project project) {
//			if (project!=null) {
//				AgentStartConfiguration agentStartConfiguration = project.getAgentStartConfiguration();
//				Vector<AgentStartArguments> startArgumentsVector = agentStartConfiguration.getAgentStartArguments();
//				for (int i = 0; i < startArgumentsVector.size(); i++) {
//					AgentStartArguments startArguments = startArgumentsVector.get(i);
//					String agentClassName = startArguments.getAgentReference();
//					if (agentClassName.equals("hygrid.agent.manager.SimulationManager")==true) {
//						startArguments.setAgentReference(SimulationManagerAgent.class.getName());
//					}
//				}
//			}
//		}

	/**
	 * Adjusts the setups user object class name according to the structure of the energy Agent feature.
	 * @param setup the SimulationSetup to adjust
	 */
	private void adjustSetupUserObjectClassName(SimulationSetup setup) {
		if (setup != null && setup.getUserRuntimeObjectClassName().equals("hygrid.deployment.dataModel.SetupExtension") == true) {
			setup.setUserRuntimeObjectClassName(SetupExtension.class.getName());
		}
	}
	// ----------------------------------------------------
	// --- Feature rebuild conversions ---------- End -----
	// ----------------------------------------------------
		
	
	
	// ----------------------------------------------------------------------------------
	// --- From here, the project, setup and graphController - observer is implemented --  
	// ----------------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable observable, Object updateObject) {
		
		if (updateObject instanceof String) {
			// --- Changes in the project -----------------			
			String projectNotification = (String) updateObject;
			switch (projectNotification) {
			case Project.PREPARE_FOR_SAVING:
				this.validateCurrentSetupInThread();
				break;
			}
			
		} else if (updateObject instanceof SimulationSetupNotification) {
			// --- Changes in the setup -------------------
			SimulationSetupNotification setupNotification = (SimulationSetupNotification) updateObject;
			switch (setupNotification.getUpdateReason()) {
			case SIMULATION_SETUP_LOAD:
				// Exchange with the call below ----------- 
				//this.validateCurrentSetupInThread();
				break;
			default:
				break;
			}
			
		} else if (updateObject instanceof NetworkModelNotification) {
			// --- Changes in the graphController ---------
			NetworkModelNotification netNote = (NetworkModelNotification) updateObject;
			switch (netNote.getReason()) {
			case NetworkModelNotification.NETWORK_MODEL_NetworkElementDataModelReLoaded:
				this.validateCurrentSetupInThread();
				break;
			}
			
		}
		
	}

	
	// ------------------------------------------------------------------------
	// --- From here, some static help methods --------------------------------  
	// ------------------------------------------------------------------------
	/**
	 * Checks if the specified object is a HyGrid / Energy Agent project.
	 *
	 * @param eventObject the event object received by an {@link ApplicationEvent}
	 * @return true, if is HyGrid project
	 */
	public static boolean isHyGridProject(Object eventObject) {
		if (eventObject!=null && eventObject instanceof Project) {
			return isHyGridProject((Project) eventObject);
		}
		return false;
	}
	/**
	 * Checks if is the specified project is a HyGrid / Energy Agent project.
	 *
	 * @param project the project
	 * @return true, if is HyGrid project
	 */
	public static  boolean isHyGridProject(Project project) {
		if (project==null || project.getEnvironmentController()==null || !(project.getEnvironmentController() instanceof GraphEnvironmentController)) {
			return false;
		}
		return true;
	}
	
}
