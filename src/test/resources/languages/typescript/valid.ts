interface User {
    id: number;
    name: string;
    email?: string;
}

enum Role {
    Admin,
    User,
    Guest
}

class Account<T extends User> {
    private users: T[] = [];

    addUser(user: T): void {
        this.users.push(user);
    }

    getUsers(): T[] {
        return this.users;
    }
}

const admin: User = { id: 1, name: "Admin User" };
const account = new Account<User>();
account.addUser(admin);

type Point = { x: number; y: number };
const p: Point = { x: 10, y: 20 };