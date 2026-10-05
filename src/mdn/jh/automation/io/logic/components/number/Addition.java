package mdn.jh.automation.io.logic.components.number;

/** Adds up to six integer or floating-point inputs. */
public class Addition extends NumericOperation {
	private static final long serialVersionUID = 1L;

	public Addition() {
		super("Addition");
	}

	@Override
	protected double calculateResult() {
		double result = 0;
		for (int i = 0; i < getInputDefinitions().length; i++) if (hasInput(i)) result += inputValue(i);
		return result;
	}
}
