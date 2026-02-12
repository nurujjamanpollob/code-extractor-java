interface Broken {
    name: string
    age: number;

function oops(x: number) : string {
    return x; // Type error but syntactically valid for some parsers, let's make it syntactically invalid
    if (x > 0 {
        return "positive";
    }
}