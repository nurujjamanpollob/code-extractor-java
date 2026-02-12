#include <iostream>
#include <vector>
#include <algorithm>

/**
 * Block comment for namespace.
 */
namespace extractor {

    // Single line comment
    template <typename T>
    class Comprehensive {
    private:
        T value;

    public:
        Comprehensive(T v) : value(v) {}

        /**
         * Method with lambda inside.
         */
        void process() {
            auto lambda = [this](int x) {
                return this->value + x;
            };

            std::vector<int> vec = {1, 2, 3};
            std::for_each(vec.begin(), vec.end(), [](int& n) {
                n *= 2;
            });
        }

        // Static method
        static void info() {
            std::cout << "Info" << std::endl;
        }
    };

} // namespace extractor

int main() {
    extractor::Comprehensive<int> c(10);
    c.process();
    return 0;
}
