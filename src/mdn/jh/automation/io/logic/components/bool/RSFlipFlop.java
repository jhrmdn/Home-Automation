package mdn.jh.automation.io.logic.components.bool;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBaseBoolean;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/**
 * Reset-dominant RS flip-flop. Set stores {@code true}, Reset stores
 * {@code false}, and two inactive inputs retain the previous output.
 */
public class RSFlipFlop extends LogicBaseBoolean {

	private static final long serialVersionUID = 5434706223720328032L;

	private static final InputDefinition[] inputDefinitions = {
			new InputDefinition("S (Set)", DataComponentStub.TYPE_BOOLEAN_IO, "Set output to true"),
			new InputDefinition("R (Reset)", DataComponentStub.TYPE_BOOLEAN_IO, "Reset output to false")
	};

	public RSFlipFlop() {
		super(DataComponentStub.TYPE_BOOLEAN_IO, inputDefinitions,
				new LogicComponentDescription("RS Flip-Flop"));
	}

	@Override
	public void resetSpecific() {
		myActualOutputState.setValue(false);
		updateMyOutputs();
	}

	@Override
	protected void calculateMyActualState() {
		boolean set = myInputValues[0] != null && myInputValues[0].getOutputAsBoolean();
		boolean reset = myInputValues[1] != null && myInputValues[1].getOutputAsBoolean();

		if (reset) {
			myActualOutputState.setValue(false);
		} else if (set) {
			myActualOutputState.setValue(true);
		}
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element root = (Element) super.getStorageXML(doc);
		Element state = doc.createElement("State");
		state.appendChild(doc.createTextNode(Boolean.toString(getOutputAsBoolean())));
		root.appendChild(state);
		return root;
	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		if (!super.initDataComponent(node)) {
			return false;
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if ("State".equals(children.item(i).getNodeName())) {
				myActualOutputState.setValue(Boolean.parseBoolean(children.item(i).getTextContent()));
				break;
			}
		}
		return true;
	}
}
