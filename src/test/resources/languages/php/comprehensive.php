<?php

namespace App\Examples;

/**
 * Comprehensive PHP Test File
 * This file contains various PHP structures for testing the extractor.
 */

interface LoggerInterface {
    public function log(string $message): void;
}

trait TimestampTrait {
    public function getTimestamp(): int {
        return time();
    }
}

# Shell-style comment
class BaseService implements LoggerInterface {
    use TimestampTrait;

    // Single-line comment
    private string $id;

    public function __construct(string $id) {
        $this->id = $id;
    }

    public function log(string $message): void {
        echo "[$this->id] " . $this->getTimestamp() . ": $message\n";
    }
}

/* 
 * Standalone function 
 */
function helper_function($data) {
    if (empty($data)) {
        return null;
    }
    return base64_encode($data);
}

// Anonymous block/scope test
{
    $temp = "test";
    echo $temp;
}

?>
