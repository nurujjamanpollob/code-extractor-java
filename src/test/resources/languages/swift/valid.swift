import Foundation

struct User {
    let id: Int
    var name: String
}

class UserManager {
    private var users: [User] = []

    func addUser(_ user: User) {
        users.append(user)
    }

    func findUser(byID id: Int) -> User? {
        return users.first { $0.id == id }
    }
}

let manager = UserManager()
manager.addUser(User(id: 1, name: "Swift Developer"))

if let user = manager.findUser(byID: 1) {
    print("Found user: \(user.name)")
}