#include <stdio.h>

int main(int argc, char **argv)
{
	int i;
	printf("bete lancee avec les parametres: ");
	for(i=0;i<argc;i++)
		printf("%s ",argv[i]);
	printf("\n");
	pause();
}
