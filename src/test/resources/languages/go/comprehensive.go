package comprehensive

import (
	"fmt"
	"sync"
)

// Global variable
var GlobalCounter int

/*
Comprehensive struct.
*/
type Comprehensive struct {
	Name string
}

// Method for Comprehensive
func (c *Comprehensive) Process(items []int) {
	// Goroutine and closure (functional paradigm)
	var wg sync.WaitGroup
	for _, item := range items {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			fmt.Printf("Processing %d\n", i)
		}(item)
	}
	wg.Wait()
}

// Interface
type Processor interface {
	Process(items []int)
}

// Top-level function
func Compute(val int) int {
	// First-class function
	adder := func(x int) int {
		return x + val
	}
	return adder(10)
}
