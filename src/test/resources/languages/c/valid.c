#include <stdio.h>
#include <stdlib.h>

struct Point {
    int x;
    int y;
};

void print_point(struct Point p) {
    printf("Point(%d, %d)\n", p.x, p.y);
}

int main(int argc, char *argv[]) {
    struct Point p1 = {10, 20};
    print_point(p1);

    int *arr = malloc(5 * sizeof(int));
    for (int i = 0; i < 5; i++) {
        arr[i] = i * i;
    }

    free(arr);
    return 0;
}