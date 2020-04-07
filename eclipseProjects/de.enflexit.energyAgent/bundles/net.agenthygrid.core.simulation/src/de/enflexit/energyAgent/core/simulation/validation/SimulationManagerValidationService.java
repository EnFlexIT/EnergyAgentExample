package de.enflexit.energyAgent.core.simulation.validation;

import java.util.ArrayList;

import de.enflexit.energyAgent.core.validation.HyGridValidationAdapter;
import de.enflexit.energyAgent.core.validation.HyGridValidationService;

/**
 * HyGridValidationService implementation for checking if the SimulationManager is configured correctly
 * @author Nils Loose - DAWIS - ICB - University of Duisburg - Essen
 */
public class SimulationManagerValidationService extends HyGridValidationAdapter implements HyGridValidationService {

	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationService#getHyGridValidationChecks(boolean)
	 */
	@Override
	public ArrayList<HyGridValidationAdapter> getHyGridValidationChecks(boolean isHeadlessOperation) {
		ArrayList<HyGridValidationAdapter> validationChecks = new ArrayList<>();
		validationChecks.add(new SimulationManagerValidationAdapter());
		return validationChecks;
	}


}
