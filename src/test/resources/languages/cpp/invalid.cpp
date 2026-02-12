#include <iostream>

class Broken {
public:
    void method() {
        if (true {
            std::cout << "Missing paren" << std::endl;
        }
    }
// Missing closing brace for class

int main() {
    return 0;
}