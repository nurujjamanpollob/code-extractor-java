const std = @import("std");

pub fn main() !void {
    const stdout = std.io.getStdOut().writer();
    try stdout.print("Hello, {s}!\n", .{"Zig"});

    const items = [_]i32{ 1, 2, 3, 4, 5 };
    var sum: i32 = 0;
    for (items) |item| {
        sum += item;
    }
    try stdout.print("Sum: {d}\n", .{sum});
}

fn add(a: i32, b: i32) i32 {
    return a + b;
}

test "basic addition" {
    try std.testing.expect(add(1, 2) == 3);
}