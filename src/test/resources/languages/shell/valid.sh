#!/bin/bash

# A sample bash script
NAME="User"
echo "Hello, $NAME!"

if [ -f "config.txt" ]; then
    echo "Config file found"
else
    echo "Config file missing"
fi

for i in {1..5}; do
    echo "Iteration $i"
done

function greet() {
    local name=$1
    echo "Greeting $name"
}

greet "World"