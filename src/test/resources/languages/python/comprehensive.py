"""
Module level docstring.
This covers multiple lines.
"""

import os
from datetime import datetime

# A global variable
GLOBAL_VAR = 10

@decorator_one
@decorator_two(param="value")
class Comprehensive:
    """Class docstring."""

    def __init__(self, name):
        # Instance attribute
        self.name = name

    @property
    def formatted_name(self):
        return f"Name: {self.name}"

    def process_data(self, items):
        # Lambda expression
        sorter = lambda x: x.lower()
        
        # List comprehension (structural pattern)
        results = [self.name + str(i) for i in items if i > 0]
        
        def nested_helper(val):
            """Nested function with docstring."""
            return val * 2
            
        return [nested_helper(r) for r in results]

def top_level_function():
    """Function docstring."""
    try:
        pass
    except Exception as e:
        print(f"Error: {e}")
    finally:
        print("Done")

if __name__ == "__main__":
    c = Comprehensive("Test")
    print(c.process_data([1, 2, 3]))
