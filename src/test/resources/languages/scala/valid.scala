package com.example

object Main {
  def main(args: Array[String]): Unit = {
    val user = User(1, "Scala Developer")
    println(user.greet())
  }
}

case class User(id: Int, name: String) {
  def greet(): String = s"Hello, I am $name"
}

class Processor {
  def process[T](items: List[T]): Int = {
    items.length
  }
}