package mdn.jh.automation.io;

public class Action {

	String actionInformation=null;
	
	public String getActionInformation() {
		return actionInformation;
	}

	public void setActionInformation(String actionInformation) {
		this.actionInformation = actionInformation;
	}

	public Action() {
		
	}
	public Action(String actionInformation) {
		setActionInformation(actionInformation);
	}

}
