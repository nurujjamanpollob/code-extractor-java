// Valid JavaScript Sample
const PI = 3.14159;

class Sample {
    constructor(name) {
        this.name = name;
    }

    process() {
        console.log("Processing: " + this.name);
        return true;
    }
}

function topLevel() {
    const s = new Sample("test");
    s.process();
}

const arrow = (x) => x * 2;
