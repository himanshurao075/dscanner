import 'package:camera/camera.dart';
import 'package:flutter/material.dart';


class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late CameraController _controller;
  late List<CameraDescription> cameras;

  @override
  void initState() {
    super.initState();
    _initializeCamera();
  }

  Future<void> _initializeCamera() async {
    cameras = await availableCameras();
    final CameraController controller = CameraController(
      cameras[0],
      ResolutionPreset.high,
    );
    await controller.initialize();
    if (!mounted) {
      return;
    }
    setState(() {
      _controller = controller;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_controller == null || !_controller.value.isInitialized) {
      return Container();
    }
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: Text('Camera View with Frames'),
        ),
        body: Stack(
          children: [
            CameraPreview(_controller),
            CustomPaint(painter: FramePainter()),
          ],
        ),
      ),
    );
  }
}

class FramePainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    // Define the size and position of the frames
    final double borderWidth = 5;
    final double frameWidth = size.width - borderWidth * 2;
    final double frameHeight = size.height * 0.6;
    final double frameX = borderWidth;
    final double frameY = (size.height - frameHeight) / 2;

    // Draw the frames
    final Paint paint = Paint()
      ..color = Colors.green
      ..strokeWidth = borderWidth
      ..style = PaintingStyle.fill;
    final Rect frameRect = Rect.fromLTWH(frameX, frameY, frameWidth, frameHeight);
    canvas.drawRect(frameRect, paint);
  }

  @override
  bool shouldRepaint(FramePainter oldDelegate) => false;
}
