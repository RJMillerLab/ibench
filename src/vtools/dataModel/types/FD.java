package vtools.dataModel.types;

public class FD 
{
	private String[] _from;
	private String[] _to;
	
	public String[] getFrom()
	{
		return this._from;
	}
	
	public String[] getTo()
	{
		return this._to;
	}
	
	public void setFrom(String[] from)
	{
		this._from = from;
	}
	
	public void setTo(String[] to)
	{
		this._to = to;
	}
}
