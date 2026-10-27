package mdn.jh.automation.io.logic.components.number;

/** Multiplies up to six integer or floating-point inputs. */
public class Multiplication extends NumericOperation {
	private static final long serialVersionUID = 1L;

	public Multiplication() {
		super("Multiplication");
	}

	@Override
	protected double calculateResult() {
		double result = 1;
		boolean connected = false;
		for (int i = 0; i < getInputDefinitions().length; i++) if (hasInput(i)) {
			result *= inputValue(i);
			connected = true;
		}
		return connected ? result : 0;
	}
}
