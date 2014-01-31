#include <stdio.h>

int 
main()
{
	int c;

	while ((c = getchar()) != EOF)
	{
		if (c == '&') 
		{
			while ((c = getchar()) != ';')
				;
		}
		else	
			putchar(c);
	}
return 0;
}

