package hygrid.env.adapter;

import java.util.Vector;

import org.awb.env.networkModel.NetworkModel;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4Ontology;
import org.awb.env.networkModel.controller.GraphEnvironmentController;

import agentgui.ontology.AgentGUI_BaseOntology;
import agentgui.ontology.TimeSeriesChart;
import hygrid.globalDataModel.ontology.CableWithBreaker;
import hygrid.globalDataModel.ontology.HyGridOntology;
import jade.content.onto.Ontology;

/**
 * The Class CableDataModelAdapter defines the data model 
 * for a cable within the defined {@link NetworkModel}.
 * In this special case an {@link NetworkComponentAdapter4Ontology} was
 * extended that enables to use ontology instances as a data model.
 * 
 * @author Christian Derksen - DAWIS - ICB - University of Duisburg-Essen
 */
public class BreakerDataModelAdapter extends NetworkComponentAdapter4Ontology {

	private Vector<Class<? extends Ontology>> ontologyBaseClasses = null;
	private String[] ontologyClassReferences = null;
	
	/**
	 * The constructor of this class
	 * @param graphController the current graph controller
	 */
	public BreakerDataModelAdapter(GraphEnvironmentController graphController) {
		super(graphController);
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4Ontology#getOntologyBaseClasses()
	 */
	@Override
	public Vector<Class<? extends Ontology>> getOntologyBaseClasses() {
		if (this.ontologyBaseClasses==null) {
			this.ontologyBaseClasses = new Vector<Class<? extends Ontology>>();
			this.ontologyBaseClasses.add(HyGridOntology.class);
			this.ontologyBaseClasses.add(AgentGUI_BaseOntology.class);
		}
		return this.ontologyBaseClasses;
	}
	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter4Ontology#getOntologyClassReferences()
	 */
	@Override
	public String[] getOntologyClassReferences() {
		if (this.ontologyClassReferences==null) {
			this.ontologyClassReferences = new String[2];
			this.ontologyClassReferences[0] = CableWithBreaker.class.getName();
			this.ontologyClassReferences[1] = TimeSeriesChart.class.getName();
		}
		return this.ontologyClassReferences;
	}

}