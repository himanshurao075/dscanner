import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:io' as Io;
import 'package:image/image.dart' as Image;
import 'dart:ui' as ui;

import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';

class CropScreen extends StatefulWidget {
  const CropScreen({super.key, required this.img});
  final XFile img;
  @override
  State<CropScreen> createState() => _CropScreenState();
}

class _CropScreenState extends State<CropScreen> {
  Future<ui.Image> getImage() async {
    final imgCompletor = Completer<ui.Image>();
    Size size = MediaQuery.of(context).size;

    final m = MemoryImage(await widget.img.readAsBytes());
    final resizedImage =
        ResizeImage(m, width: size.width.toInt(), height: size.height.toInt());

    resizedImage
        // m
        .resolve(const ImageConfiguration(size: Size(100, 100)))
        .addListener(ImageStreamListener((image, synchronousCall) {
      imgCompletor.complete(image.image);
    }));

    return imgCompletor.future;
  }

  Offset touchPointer1 = Offset(50, 50);
  Offset touchPointer2 = Offset(300, 50);
  Offset touchPointer3 = Offset(50, 400);
  Offset touchPointer4 = Offset(300, 400);
  Function(void Function())? state;

  checkPointer(Offset pointer) {
    if (pointer.dx < touchPointer1.dx + 20 &&
        pointer.dx > touchPointer1.dx - 20 &&
        pointer.dy < touchPointer1.dy + 20 &&
        pointer.dy > touchPointer1.dy - 20) {
      return 1;
    } else if (pointer.dx < touchPointer2.dx + 20 &&
        pointer.dx > touchPointer2.dx - 20 &&
        pointer.dy < touchPointer2.dy + 20 &&
        pointer.dy > touchPointer2.dy - 20) {
      return 2;
    } else if (pointer.dx < touchPointer3.dx + 20 &&
        pointer.dx > touchPointer3.dx - 20 &&
        pointer.dy < touchPointer3.dy + 20 &&
        pointer.dy > touchPointer3.dy - 20) {
      return 3;
    } else if (pointer.dx < touchPointer4.dx + 20 &&
        pointer.dx > touchPointer4.dx - 20 &&
        pointer.dy < touchPointer4.dy + 20 &&
        pointer.dy > touchPointer4.dy - 20) {
      return 4;
    } else {
      return -1;
    }
  }

  bool checkOverlapping(int temp, Offset pointer, Offset center) {
    bool result = true;
    switch (temp) {
      case 1:
        {
          if ((pointer.dx + 40) >= center.dx ||
              (pointer.dy + 40) >= center.dy) {
            result = false;
          }
          // if ((pointer.dx + 20) >= touchPointer2.dx ||
          //     (pointer.dy + 20) >= touchPointer4.dy) {
          //   result = false;
          // }
          break;
        }
      case 2:
        {
          if ((pointer.dx - 40) <= center.dx ||
              (pointer.dy + 40) >= center.dy) {
            result = false;
          }
          // if ((pointer.dx - 20) <= touchPointer1.dx ||
          //     (pointer.dy + 20) >= touchPointer4.dy) {
          //   result = false;
          // }
          break;
        }
      case 3:
        {
          if ((pointer.dx + 40) >= center.dx ||
              (pointer.dy - 40) <= center.dy) {
            result = false;
          }
          //  if ((pointer.dx + 20) >= touchPointer4.dx ||
          //     (pointer.dy - 20) <= touchPointer1.dy) {
          //   result = false;
          // }
          break;
        }
      case 4:
        {
          if ((pointer.dx - 40) <= center.dx ||
              (pointer.dy - 40) <= center.dy) {
            result = false;
          }
          // if ((pointer.dx - 20) <= touchPointer3.dx ||
          //     (pointer.dy - 20) <= touchPointer2.dy) {
          //   result = false;
          // }
          break;
        }
      default:
        result = true;
        break;
    }
    return result;
  }

  static const platform = MethodChannel('samples.flutter.dev/cropImage');
  String croppedImageString = '';
  callMethodChannel() async {
    final size = MediaQuery.of(context).size;
    // final image = await getImage();
    //  final byte = await image.toByteData();
    //
    //  // final fileImg=  FileImage(File(widget.img.path));
    //  // Uint8List m =  File(widget.img.path).readAsBytesSync();
    //  // // ui.Image x = await decodeImageFromList(m);
    //  // // ByteData? bytes = await x.toByteData();
    //  // Image.Image decodedImage = Image.decodeImage(m) as Image.Image;
    //  // Image.Image thumbnail = Image.copyResize(decodedImage, width: 60);
    //  // List<int> resizedIntList = thumbnail.getBytes();
    // Directory docDir = await getApplicationDocumentsDirectory();
    // String cahcePath = docDir.path;
    //  final fout =await File(cahcePath).writeAsBytes(byte!.buffer.asUint8List());
    //  String imgPath = fout.path ;

    // double x1 =  (touchPointer1.dx/  size.width) *100 * decodedImage.width;

    final file = File(widget.img.path);
    var decodedImage = await decodeImageFromList(file.readAsBytesSync());

    try {
      final String result = await platform.invokeMethod('cropImage', {
        "imgPath":widget.img.path ,
        "x1": ((touchPointer1.dx / size.width)) * decodedImage.width,
        "x2": ((touchPointer2.dx / size.width)) * decodedImage.width,
        "x3": ((touchPointer3.dx / size.width)) * decodedImage.width,
        "x4": ((touchPointer4.dx / size.width)) * decodedImage.width,
        "y1": ((touchPointer1.dy / size.height)) * decodedImage.height,
        "y2": (touchPointer2.dy / size.height) * decodedImage.height,
        "y3": (touchPointer3.dy / size.height) * decodedImage.height,
        "y4": (touchPointer4.dy / size.height) * decodedImage.height,
        // "imgPath":widget.img.path ,
        // "x1": touchPointer1.dx,
        // "x2": touchPointer2.dx,
        // "x3": touchPointer3.dx,
        // "x4": touchPointer4.dx,
        // "y1": touchPointer1.dy,
        // "y2": touchPointer2.dy,
        // "y3": touchPointer3.dy,
        // "y4": touchPointer4.dy,
        "height": decodedImage.height,
        "width": decodedImage.width
      });
      // batteryLevel = 'Battery level at $result % .';
      debugPrint("Method Channel Result : $result");
      croppedImageString = result;
    } on PlatformException catch (e) {
      debugPrint("Failed to get battery level: '${e.message}'.");
    }
  }

  final paintKey = GlobalKey();
  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size.center(Offset.zero);
    touchPointer3 = Offset(50, size.dy + 100);
    touchPointer4 = Offset(300, size.dy + 100);
    return WillPopScope(
      onWillPop: () async {
       await callMethodChannel();
         Navigator.pop(context,
            [
              croppedImageString,
              touchPointer1, touchPointer2, touchPointer3, touchPointer4]);

        return false;
      },
      child: Container(
        height: 1080,
        width: 720,
        child: FutureBuilder<ui.Image>(
            future: getImage(),
            builder: (context, snap) {
              if (snap.data == null) {
                return LinearProgressIndicator();
              } else {
                return GestureDetector(
                  onPanStart: (detalis) {
                    // debugPrint(detalis.localPosition.toString());
                  },
                  onPanUpdate: (dragDetails) {
                    // debugPrint("Local  :   " + dragDetails.localPosition.toString());
                    // debugPrint("Global  :   " + dragDetails.globalPosition.toString());

                    // if ()
                    final temp = checkPointer(dragDetails.localPosition);

                    // if (paintKey.currentContext != null) {
                    //   final box = paintKey.currentContext!.findRenderObject()
                    //       as RenderBox;
                    //   if (Rect.fromCenter(
                    //           center: box.size.center(Offset.zero),
                    //           width: box.size.width * .4,
                    //           height: box.size.width * .4)
                    //       .contains(dragDetails.globalPosition)) {
                    //     return;
                    //   }
                    // }

                    // // checkOverlapping(temp);
                    if (checkOverlapping(temp, dragDetails.localPosition,
                        paintKey.currentContext!.size!.center(Offset.zero))) {
                      if (temp == 1) {
                        touchPointer1 = dragDetails.globalPosition;
                      } else if (temp == 2) {
                        touchPointer2 = dragDetails.globalPosition;
                      } else if (temp == 3) {
                        touchPointer3 = dragDetails.globalPosition;
                      } else if (temp == 4) {
                        touchPointer4 = dragDetails.globalPosition;
                      }
                    }

                    state!(() {});
                  },
                  child: StatefulBuilder(
                    builder: (context, setState2) {
                      state = setState2;
                      return CustomPaint(
                          key: paintKey,
                          willChange: true,
                          size: const Size(720, 1080),
                          painter: CustomCropPainter(
                            img: snap.data!,
                            touchPointer1: touchPointer1,
                            touchPointer2: touchPointer2,
                            touchPointer3: touchPointer3,
                            touchPointer4: touchPointer4,
                          ));
                    },
                  ),
                );
              }
            }),
      ),
    );
  }
}

class CustomCropPainter extends CustomPainter {
  CustomCropPainter({
    required this.img,
    required this.touchPointer1,
    required this.touchPointer2,
    required this.touchPointer3,
    required this.touchPointer4,
  });
  final ui.Image img;
  final Offset touchPointer1;
  final Offset touchPointer2;
  final Offset touchPointer3;
  final Offset touchPointer4;
  @override
  void paint(Canvas canvas, Size size) async {
    var paint = Paint()
      ..style = PaintingStyle.fill
      ..color = Colors.white
      ..isAntiAlias = true;
    paintImage(
        canvas: canvas,
        rect: Rect.fromCenter(
            center: size.center(Offset.zero), width: 720, height: 1080),
        image: img);

    canvas.drawCircle(touchPointer1, 15, paint);
    canvas.drawCircle(touchPointer2, 15, paint);
    canvas.drawCircle(touchPointer3, 15, paint);
    canvas.drawCircle(touchPointer4, 15, paint);
    Path path = Path();
    paint
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;
    path.moveTo(touchPointer1.dx, touchPointer1.dy);
    path.lineTo(touchPointer2.dx, touchPointer2.dy);
    path.lineTo(touchPointer4.dx, touchPointer4.dy);
    path.lineTo(touchPointer3.dx, touchPointer3.dy);
    path.lineTo(touchPointer1.dx, touchPointer1.dy);
    canvas.drawPath(path, paint);
    // canvas.drawRect(
    //     Rect.fromCenter(
    //         center: size.center(Offset.zero),
    //         width: size.width * .5,
    //         height: size.height * .5),
    //     paint);
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) => true;
}
