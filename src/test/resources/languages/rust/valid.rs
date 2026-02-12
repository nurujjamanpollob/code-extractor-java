use std::collections::HashMap;

struct Person {
    name: String,
    age: u32,
}

impl Person {
    fn new(name: &str, age: u32) -> Self {
        Self {
            name: name.to_string(),
            age,
        }
    }

    fn greet(&self) {
        println!("Hello, my name is {} and I am {} years old.", self.name, self.age);
    }
}

fn main() {
    let p = Person::new("Rustacean", 5);
    p.greet();

    let mut map = HashMap::new();
    map.insert("key", "value");

    match map.get("key") {
        Some(val) => println!("Found: {}", val),
        None => println!("Not found"),
    }
}