import 'package:flutter/material.dart';
import 'HomePage.dart';

void main() {
  runApp(MaterialApp(
    debugShowCheckedModeBanner: false,
    color: Colors.teal,
    themeMode: ThemeMode.light,
    theme: ThemeData(primarySwatch: Colors.teal),
    home: const Homepage(),
  ));
}


