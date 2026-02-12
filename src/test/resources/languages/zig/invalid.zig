const std = @import("std");

pub fn main() !void {
    if (true {
        // Missing paren
    }
}

fn broken() i32
    return 1;
}