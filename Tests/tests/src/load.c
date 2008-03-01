#include <stdio.h>
#include <unistd.h>

int main(int argc, char* argv[]){
	int n, m;

	n =	atoi(argv[1]);
	n = n*n*500000;

	while(1){
		m++;
		if(m%n == 0){
			usleep(1);
		}
	}
	return 0;
}
