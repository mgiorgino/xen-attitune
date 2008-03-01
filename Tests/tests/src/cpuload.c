#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <signal.h>
#include <string.h>


void signaux(int sig){
	signal(sig,signaux);
	alarm(0);
}

int main(int argc, char* argv[]){
	int n, nbcpu, cpu, lastcpu, i, time;
	FILE* file;
	char* line = NULL;
 	pid_t   pid;

	signal(SIGALRM, signaux);

	file = fopen("/proc/stat", "r");
	getline(&line, &n, file);
	sscanf(line, "%*s %lu %*s\n", &cpu);

	nbcpu=0;
	getline(&line, &n, file);

	while(strncmp(line, "cpu", 3)==0){
		getline(&line, &n, file);
		nbcpu++;
	}
	fclose(file);

	printf("%d cpus.\n", nbcpu);

	lastcpu = cpu;

	n=0;
	while(1){
		alarm(1);
		pause();
		file = fopen("/proc/stat", "r");
		fscanf(file, "%*s %lu", &cpu);
		fclose(file);
		printf("%d\n", (cpu-lastcpu)/nbcpu);
		lastcpu = cpu;
	}
	return EXIT_SUCCESS;
}

