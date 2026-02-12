#include <stdio.h>

int main() {
    if (1 {
        printf("Missing paren\n");
    }
    return 0;
}

void broken(int x {
    printf("%d\n", x);
}