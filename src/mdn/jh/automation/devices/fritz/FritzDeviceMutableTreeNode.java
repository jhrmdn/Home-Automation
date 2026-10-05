package mdn.jh.automation.devices.fritz;

import javax.swing.tree.DefaultMutableTreeNode;

public class FritzDeviceMutableTreeNode extends DefaultMutableTreeNode {

	SmartHomeDevice myFritzDevice = null;
	String myXPath = null;
	String name = null;
	String uin = null;
	String functionbitmask=null;
	
	public String getFunctionbitmask() {
		return functionbitmask;
	}

	public void setFunctionbitmask(String functionbitmask) {
		this.functionbitmask = functionbitmask;
	}

	public String getUin() {
		return uin;
	}

	public void setUin(String uin) {
		this.uin = uin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMyXPath() {
		return myXPath;
	}

	public void setMyXPath(String myXPath) {
		this.myXPath = myXPath;
	}

	public SmartHomeDevice getSmartHomeDevice() {
		return myFritzDevice;
	}

	private static final long serialVersionUID = -5014928700055829231L;

	public FritzDeviceMutableTreeNode() {
		// TODO Auto-generated constructor stub
	}

	public FritzDeviceMutableTreeNode(Object userObject) {
		super(userObject);
		// TODO Auto-generated constructor stub
	}

	public FritzDeviceMutableTreeNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
		// TODO Auto-generated constructor stub
	}
	
	public void setSmartHomeDevice(SmartHomeDevice fritzDevice) {
		this.myFritzDevice=fritzDevice;
	}

	
	
	

}
