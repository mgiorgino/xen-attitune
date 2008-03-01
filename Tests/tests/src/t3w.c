#include <stdio.h>
#include <unistd.h>

int main(int argc, char* argv[]){
	long pause, n, niter;
	char* filename;
	FILE* file;

	if(argc == 4){
		niter = atoi(argv[1]);
		filename = (argv[2]);
		pause = atoi(argv[3]);
	}else if(argc == 3){
		niter = atoi(argv[1]);
		filename = (argv[2]);
		pause = 0;
	}else{
		printf("Syntaxe :\n $ t3w < nombre d'itération > < nom de fichier > [< temps de pause en µs >]\n");
		return 1;
	}

	file = fopen(filename, "w");

	n=0;
	while(n <= niter){
		fprintf(file, "%d\n", n);
		fflush(file);
		if(pause != 0){
			usleep(pause);
		}
		n++;
	}
	return 0;
}
