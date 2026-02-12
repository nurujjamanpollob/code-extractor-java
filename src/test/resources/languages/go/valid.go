package main

import (
	"fmt"
	"net/http"
)

type Sample struct {
	Port int
}

func (s *Sample) Start() error {
	fmt.Printf("Starting server on port %d\n", s.Port)
	return http.ListenAndServe(fmt.Sprintf(":%d", s.Port), nil)
}

func main() {
	s := &Sample{Port: 8080}
	if err := s.Start(); err != nil {
		fmt.Printf("Error: %v\n", err)
	}
}