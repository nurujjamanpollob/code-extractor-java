package main

import "fmt"

func broken() 
	fmt.Println("Missing opening brace")
}

func main() {
	if true {
		fmt.Println("Missing closing brace")
	
}