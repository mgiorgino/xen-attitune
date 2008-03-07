#include <stdio.h>

int main(int argc, char **argv)
{
	char *pid_to_check=argv[1];
	char *tubeAddr=argv[2];
	char *probedEntity=argv[3];
	char *notificationName=argv[4];

	char commande[100];
	
	if(argc<4)
	{
		printf("Usage: %s pid_to_check tubeAddr probedEntity notificationName\n",argv[0]);
		return -1;
	}
	
	sprintf(commande, "ps -p %s -o pid > /dev/null || echo \"%s;this;%s\" > %s",pid_to_check,notificationName,probedEntity,tubeAddr);
	printf("Running GenericProbeC on pid %s, with failed notification signal %s in tube %s on entity %s\n",pid_to_check, notificationName,tubeAddr,probedEntity);	
	
	while(1)
	{
		system(commande);
		sleep(1);		
	}
	
}
