<?php

function missingSemicolon() {
    echo "Hello"
}

class Incomplete {
    public function __construct() {
        $this->val = 1;
    // Missing closing brace for class
