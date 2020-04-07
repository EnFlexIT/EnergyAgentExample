package hygrid.env.adapter.triPhase;

import java.util.Vector;

import javax.swing.JComponent;
import org.awb.env.networkModel.adapter.NetworkComponentAdapter4DataModel;

import hygrid.env.adapter.CableWithBreakerAdapter;

public class TriPhaseBreakerAdapter extends CableWithBreakerAdapter {
	
	@Override
	public NetworkComponentAdapter4DataModel getNewDataModelAdapter() {
		return new TriPhaseBreakerDataModelAdapter(this.getGraphEnvironmentController());
	}

	/* (non-Javadoc)
	 * @see org.awb.env.networkModel.helper.NetworkComponentAdapter#getJPopupMenuElements()
	 */
	@Override
	public Vector<JComponent> getJPopupMenuElements() {
		return super.getJPopupMenuElements();
	}

}
