package mdn.jh.automation.devices.fritz.datasink;

import java.util.logging.Level;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.devices.fritz.FritzBoxDevice;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceHandler;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.sink.DataSink;

public abstract class FritzDataSink extends DataSink {

	private static final long serialVersionUID = 1713055089354061241L;
	FritzBoxDevice fritzBox = null;
	/**
	 * Identifier (AIN) without blanks
	 */
	private String identifierWebRequest = null;
	/**
	 * The AIN unmodified
	 */
	private String identifierOriginal = null;

	@Override
	public JPanel getSpecificDetailsPanel() {
		return new FritzActionDetailsPanel(this);
	}

	protected FritzDataSink(InputDefinition[] inputDefinitions) {
		super(inputDefinitions);
	}

	protected FritzBoxDevice getFritzBox() {
		return fritzBox;
	}

	public FritzBoxDevice getFritzBoxDevice() {
		return fritzBox;
	}

	public String getIdentifierOriginal() {
		return identifierOriginal;
	}

	public void setFritzBox(FritzBoxDevice fritzBox) {
		this.fritzBox = fritzBox;
		setSinkName("Fritz Box: " + fritzBox.getMyIPAddress());
	}

	@Override
	public boolean isDataValid() {

		FritzDataSourceHandler f = (FritzDataSourceHandler) getFritzBox().getDataSourceHandler();

		String status = f.getNodeValue("//devicelist/device[@identifier='" + getIdentifierOriginal() + "']/present");
		if ("1".equals(status)) {
			return true;
		}
		return false;
	}

	/**
	 * The functionbits which the device must have to be controlled by this action
	 * 
	 * @return
	 */
	public abstract int[] getActionBits();

	public void setIdentifier(String identifier) {
		if (identifier == null)
			return;
		this.identifierOriginal = identifier;
		this.identifierWebRequest = identifier.replaceAll(" ", "");
	}

	protected String execute(String switchcmd, String param) {
		if (fritzBox == null) {
			Main.getLogger().log(Level.WARNING, "Fritz Action. FritzBox is null");
			return null;
		}
		if (identifierWebRequest == null) {

			Main.getLogger().log(Level.WARNING, "Fritz Action. Identifier is null");
			return null;
		}

		// Main.getLogger().log(Level.INFO,"Fritz Action. Execute: "+switchcmd+" -
		// "+param);

		String urlGet = "ain=" + identifierWebRequest + "&" + "switchcmd=" + switchcmd;

		if (param != null) {
			urlGet = urlGet + "&" + param;
		}
		Main.getLogger().log(Level.INFO, "Fritz Action. Execute: " + urlGet);

		String response = fritzBox.getReplyFromFritzBoxText(urlGet);
		if (response == null) {
			Main.getLogger().log(Level.SEVERE, "Fritz Action failed: no response");
		} else if ("inval".equalsIgnoreCase(response)) {
			Main.getLogger().log(Level.SEVERE, "Fritz Action failed: invalid request");
		} else {
			Main.getLogger().log(Level.FINER, "Fritz Action response: " + response);
		}
		return response;
	}

	public void initSpecific(Node node) {
		if (node == null)
			return;

		NodeList ndp = node.getChildNodes();
		Node temp = null;
		for (int k = 0; k < ndp.getLength(); k++) {
			temp = ndp.item(k);

			if ("Attributes".equals(temp.getNodeName())) {

				this.identifierOriginal = getAttribute(temp, "identifierOriginal");
				this.identifierWebRequest = getAttribute(temp, "identifierWebRequest");
				setName(getAttribute(temp, "name"));
				
				setStatusMessage(getName());
				setSinkName(getAttribute(temp, "sinkName"));
			}
		}
	}

	private String getAttribute(Node source, String attributeName) {
		Node n = source.getAttributes().getNamedItem(attributeName);
		if (n != null) {
			return n.getNodeValue();
		}
		return null;
	}

	public Node getStorageXML(Document doc) {
		Element rootElement = null;
		rootElement = doc.createElement("DataSink");
		rootElement.setAttribute("id", "" + getId());
		rootElement.setAttribute("class", this.getClass().getCanonicalName());

		rootElement.appendChild(getDataComponentBaseXML(doc));

		Element attributes = doc.createElement("Attributes");
		attributes.setAttribute("identifierOriginal", this.identifierOriginal);
		attributes.setAttribute("identifierWebRequest", this.identifierWebRequest);
		attributes.setAttribute("name", getName());
		attributes.setAttribute("sinkName", getSinkName());

		rootElement.appendChild(attributes);

		return rootElement;
	}

}
