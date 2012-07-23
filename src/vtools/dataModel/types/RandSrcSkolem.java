package vtools.dataModel.types;

public class RandSrcSkolem 
{
	private int _attrPosition;
	private int[] _argPositions;
	
	private String _attr;
	private String _attrVar;
	
	private String[] _argAttrs;
	private String[] _argVars;
	
	private String _skId;
	
	public int getAttrPosition()
	{
		return _attrPosition;
	}
	
	public int[] getArgPositions() 
	{
		return _argPositions;
	}
	
	public String getAttr()
	{
		return _attr;
	}
	
	public String[] getArgAttrs()
	{
		return _argAttrs;
	}
	
	public String getAttrVar()
	{
		return _attrVar;
	}
	
	public String[] getArgVars()
	{
		return _argVars;
	}
	
	public String getSkId() 
	{
		return _skId;
	}
	
	public void setAttrPosition(int attrPosition)
	{
		_attrPosition = attrPosition;
	}
	
	public void setArgPositions(int[] argPositions) 
	{
		_argPositions = argPositions;
	}
	
	public void setAttr (String attr)
	{
		_attr = attr;
	}
	
	public void setArgAttrs (String[] argAttrs)
	{
		_argAttrs = argAttrs;
	}

	public void setAttrVar (String attrVar)
	{
		_attrVar = attrVar;
	}
	
	public void setArgVars (String[] argVars)
	{
		_argVars = argVars;
	}

	public void setSkId(String skId) 
	{
		_skId = skId;
	}
}
