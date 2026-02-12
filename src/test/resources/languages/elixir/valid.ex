defmodule MyApp.User do
  defstruct [:id, :name, :email]

  def new(id, name, email) do
    %__MODULE__{id: id, name: name, email: email}
  end

  def greet(%__MODULE__{name: name}) do
    "Hello, #{name}!"
  end
end

defmodule MyApp.Main do
  def run do
    user = MyApp.User.new(1, "Elixir Dev", "dev@example.com")
    IO.puts MyApp.User.greet(user)
  end
end