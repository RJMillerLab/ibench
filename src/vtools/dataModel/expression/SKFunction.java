package vtools.dataModel.expression;

import java.util.Vector;

public class SKFunction extends Function {

	private static final String SK_PREFIX = "SK_"; 
	
	public SKFunction (int id) {
		super(SK_PREFIX + id);
	}
	
	public SKFunction(String name) {
		super(SK_PREFIX + name);
	}

	public SKFunction(String name, Vector<ValueExpression> args) {
		super(SK_PREFIX + name, args);
    }

	
}
