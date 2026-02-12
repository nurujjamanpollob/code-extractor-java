package malformed

func UnclosedBrace() {
	if true {
		fmt.Println("No closing brace")
}

func UnclosedString() {
	s := "never ends
}

/* Unclosed comment
