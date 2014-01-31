package vtools.dataModel.expression;

import java.util.Vector;

public class SKFunction extends Function {

	
	public SKFunction(String name) {
		super(name);
	}

	public SKFunction(String name, Vector<ValueExpression> args) {
		super(name, args);
    }

	
}
