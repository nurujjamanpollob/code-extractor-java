class User
  attr_accessor :name, :email

  def initialize(name, email)
    @name = name
    @email = email
  end

  def to_s
    "#{@name} <#{@email}>"
  end
end

users = [
  User.new("Alice", "alice@example.com"),
  User.new("Bob", "bob@example.com")
]

users.each do |user|
  puts user
end

def greet(name)
  "Hello, #{name}!"
end

puts greet("Rubyist")