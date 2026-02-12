class Malformed:
    def method_with_bad_indentation(self):
    print("This line is incorrectly indented")
        print("This one too")

def unclosed_string():
    s = "This string is never closed

def unclosed_docstring():
    """This docstring starts but never ends
    
def incomplete_def():
    def 
