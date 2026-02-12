import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Dart/Flutter Test')),
        body: const Center(child: Text('Hello World')),
      ),
    );
  }
}

class User {
  final String name;
  final int age;

  User(this.name, this.age);
}