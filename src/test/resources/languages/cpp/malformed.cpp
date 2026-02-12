class Malformed {
public:
    void method() {
        if (true) {
            // Missing closing brace
    }
    
    void unclosedString() {
        const char* s = "never ends;
    }

/* Unclosed comment
