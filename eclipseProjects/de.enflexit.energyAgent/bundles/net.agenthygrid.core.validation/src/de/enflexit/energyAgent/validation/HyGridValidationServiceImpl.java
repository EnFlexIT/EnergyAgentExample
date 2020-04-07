package de.enflexit.energyAgent.validation;

import java.util.ArrayList;

import de.enflexit.energyAgent.core.validation.HyGridValidationAdapter;
import de.enflexit.energyAgent.core.validation.HyGridValidationService;

/**
 * The Class HygridValidationService provides specific setup tests.
 */
public class HyGridValidationServiceImpl implements HyGridValidationService {

	/* (non-Javadoc)
	 * @see net.agenthygrid.core.validation.HyGridValidationService#getHyGridValidationChecks(boolean)
	 */
	@Override
	public ArrayList<HyGridValidationAdapter> getHyGridValidationChecks(boolean isHeadlessOperation) {
		
		// --- Provide the list of individual checks for this bundle components
		ArrayList<HyGridValidationAdapter> checkList = new ArrayList<>();
		checkList.add(new ValidateCable());
		return checkList;
	}

}
