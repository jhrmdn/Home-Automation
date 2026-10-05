/**
 * 
 */
package mdn.jh.automation.storage;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author jhrib
 * 
 */
public interface Storeable {

	/**
	 * Initialize the component with the stored XML
	 * @param doc
	 */
	public boolean initDataComponent(Node node) throws Exception;
	
	public Node getStorageXML(Document doc);

	
}
