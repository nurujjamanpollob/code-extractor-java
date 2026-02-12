class Malformed {
    constructor() {
        this.value = "Unclosed string;
    }

    methodMissingBrace() {
        if (true) {
            console.log("Missing closing brace");
    }

    /* Unclosed block comment
}

function incomplete() {
    const x = 
