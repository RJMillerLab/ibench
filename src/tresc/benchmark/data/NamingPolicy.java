package tresc.benchmark.data;

public class NamingPolicy {
	
	public static  final int SAME = 1;
	public static  final int LowerUpper = 2;
	public static  final int Capitalize = 3;
	
	public String[] getSrcAndTrgName(String name, int policy){
		String[] names = new String[2];
		
		switch (policy){
		case SAME : { names[0] = new String(name);
					  names[1] = new String(name);	
					  break;
					}
		case LowerUpper :{	names[0] = new String(name.toLowerCase());
							names[1] = new String(name.toUpperCase());
							break;
						 }
		case Capitalize :{	char[] letters = name.toCharArray();
	    					letters[0] = Character.toLowerCase(letters[0] );
	    					names[0] = new String(letters);
	    					letters[0] = Character.toUpperCase(letters[0] );
	    					names[1] = new String(letters);
	    					break;
						  }
		}	
		
		return names;
	}

}
