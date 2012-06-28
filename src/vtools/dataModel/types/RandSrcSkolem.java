package vtools.dataModel.types;

public class RandSrcSkolem 
{
	private int _position;
	private String _attr;
	private String[] _skolemArgs;
	private String _attrVar;
	private String[] _skolemVars;
	
	public int getPosition()
	{
		return _position;
	}
	
	public String getAttr()
	{
		return _attr;
	}
	
	public String[] getSkolemArgs()
	{
		return _skolemArgs;
	}
	
	public String getAttrVar()
	{
		return _attrVar;
	}
	
	public String[] getSkolemVars()
	{
		return _skolemVars;
	}
	
	public void setPosition(int position)
	{
		_position = position;
	}
	
	public void setAttr (String attr)
	{
		_attr = attr;
	}
	
	public void setSkolemArgs (String[] skolemArgs)
	{
		_skolemArgs = skolemArgs;
	}

	public void setAttrVar (String attrVar)
	{
		_attrVar = attrVar;
	}
	
	public void setSkolemVars (String[] skolemVars)
	{
		_skolemVars = skolemVars;
	}
}
