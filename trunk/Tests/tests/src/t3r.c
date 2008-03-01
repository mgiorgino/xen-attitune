#include <stdio.h>
#include <unistd.h>

int main(int argc, char* argv[]){
	int m, n, ret;
	char* filename = "result.txt";
	FILE* file;

	if(argc == 2){
		filename = argv[1];
	}else{
		printf("Syntaxe :\n $ t3r < nom de fichier > \n");
		return 1;
	}

	file = fopen(filename, "r");

	ret = fscanf(file, "%d\n", &m);
	n=m;
	while(ret != -1){
		if(m != n){
			if(m == n-1){
				printf("Manque %d\n", m);
			}else{
				printf("Manque de %d à %d\n", m, n-1);
			}
			m=n;
		}
		ret = fscanf(file, "%d", &n);
		m++;
	}
	printf("Ok jusqu'à %d\n", n);
	return 0;
}
