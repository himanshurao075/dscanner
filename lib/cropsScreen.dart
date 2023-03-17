import 'dart:async';
import 'dart:io';
import 'dart:math';
import 'dart:ui';
import 'package:dscanner/ImageService.dart';
import 'package:dscanner/SizeUtils.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:ui' as ui;
import 'package:image_picker/image_picker.dart';

class CropScreen extends StatefulWidget {
  const CropScreen({
    super.key,
  });

  @override
  State<CropScreen> createState() => _CropScreenState();
}

class _CropScreenState extends State<CropScreen> {
  Future<ui.Image> getImage() async {
    final imgCompletor = Completer<ui.Image>();
    Size size = MediaQuery.of(context).size;
    final bytes = await (ImageService().displayImageFile!).readAsBytes();
    final m = MemoryImage(bytes);
    // var decodedImage = await decodeImageFromList(bytes);

    // final resizedImage =
    // ResizeImage(m, width: size.width.toInt(), height:(size.height*0.9).toInt());
    // resizedImage
    m
        .resolve(const ImageConfiguration(size: Size(100, 100)))
        .addListener(ImageStreamListener((image, synchronousCall) {
      imgCompletor.complete(image.image);
    }));

    return imgCompletor.future;
  }

  Offset touchPointer1 = const Offset(50, 50);
  Offset touchPointer2 = const Offset(300, 50);
  Offset touchPointer3 = const Offset(50, 400);
  Offset touchPointer4 = const Offset(300, 400);
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

  double widPer = 1, hitPer = 1;
  bool isPotrait = true;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();

    getCanvasSizeDetails();
  }

  static const platform = MethodChannel('samples.flutter.dev/dscanner');
  String croppedImageString = '';
  double imageAspectRatio = 1;

  callMethodChannel() async {
    try {
      ImageService().loading = true;

      final data = await getOutputImageDimesion();
      setState(() {});
      final String result = await platform.invokeMethod('cropImage', data);
      ImageService().loading = false;
      setState(() {});
      debugPrint("Method Channel Result : $result");
      ImageService().displayImageFile = XFile(result);
      String displayImagePath = ImageService().displayImageFile?.path ?? '';
      final size = MediaQuery.of(context).size;
      final file = File(displayImagePath);
      var decodedImage = await decodeImageFromList(file.readAsBytesSync());
      debugPrint("Output Image Size");
    } on PlatformException catch (e) {
      debugPrint("Platform Exception ========> PlatformException : $e");
    }
  }

  final paintKey = GlobalKey();
  final canvasConstraintsKey = GlobalKey();

  double canvasHeight = 0, canvasWidth = 0;

  getCanvasSizeDetails() async {
    final bytes = await (ImageService().displayImageFile!).readAsBytes();
    var decodedImage = await decodeImageFromList(bytes);
    imageAspectRatio = decodedImage.width / decodedImage.height;
    final size = MediaQuery.of(context).size;

    if (decodedImage.width > decodedImage.height) {
      widPer = 1;
      hitPer = (decodedImage.height / decodedImage.width);
      isPotrait = false;
    } else {
      hitPer = 1;
      widPer = (decodedImage.width / decodedImage.height);
      isPotrait = true;
    }
    if (imageAspectRatio > 1) {
      canvasWidth = size.width;
      canvasHeight = size.width * hitPer;
    } else {
      canvasWidth = size.height * 0.9 * widPer;
      canvasHeight = size.height * 0.9;
    }

    if (canvasWidth > size.width || canvasHeight > (size.height * 0.9)) {
      final canvasAspectRatio = canvasWidth / canvasHeight;
      if (canvasWidth > size.width) {
        canvasWidth = size.width;
        canvasHeight = canvasWidth / canvasAspectRatio;
      }
      else {
        canvasHeight = size.height * 0.9;
        canvasWidth = canvasHeight / canvasAspectRatio;
      }
    }

    setState(() {});
  }

  getOutputImageDimesion() async {
    String displayImagePath = ImageService().displayImageFile?.path ?? '';
    final size = MediaQuery.of(context).size;
    final file = File(displayImagePath);
    var decodedImage = await decodeImageFromList(file.readAsBytesSync());
    double x1 = ((touchPointer1.dx / canvasWidth)) * decodedImage.width;
    double x2 = ((touchPointer2.dx / canvasWidth)) * decodedImage.width;
    double x3 = ((touchPointer3.dx / canvasWidth)) * decodedImage.width;
    double x4 = ((touchPointer4.dx / canvasWidth)) * decodedImage.width;
    double y1 = ((touchPointer1.dy / canvasHeight)) * decodedImage.height;
    double y2 = (touchPointer2.dy / canvasHeight) * decodedImage.height;
    double y3 = (touchPointer3.dy /canvasHeight) * decodedImage.height;
    double y4 = (touchPointer4.dy / canvasHeight) * decodedImage.height;

    List<double> tempX = [x1, x2, x3, x4];
    List<double> tempY = [y1, y2, y3, y4];
    final minX = tempX.reduce(min);
    final maxX = tempX.reduce(max);
    final minY = tempY.reduce(min);
    final maxY = tempY.reduce(max);
    double outputImgWidth = maxX - minX;
    double outputImgHeight = maxY - minY;
    double inputImgWidth = decodedImage.width.toDouble();
    double inputImgHeight = decodedImage.height.toDouble();

    final data = {
      "imgPath": displayImagePath,
      "x1": x1,
      "x2": x2,
      "x3": x3,
      "x4": x4,
      "y1": y1,
      "y2": y2,
      "y3": y3,
      "y4": y4,
      // "imgPath":widget.img.path ,
      // "x1": touchPointer1.dx,
      // "x2": touchPointer2.dx,
      // "x3": touchPointer3.dx,
      // "x4": touchPointer4.dx,
      // "y1": touchPointer1.dy,
      // "y2": touchPointer2.dy,
      // "y3": touchPointer3.dy,
      // "y4": touchPointer4.dy,

      "inputWidth": inputImgWidth,
      "inputHeight": inputImgHeight,
      "outputWidth": outputImgWidth,
      "outputHeight": outputImgHeight
      // "height":(((touchPointer2.dx / size.width)) * decodedImage.width - ((touchPointer1.dx / size.width)) * decodedImage.width),
      // "width": ((touchPointer3.dy / size.height) * decodedImage.height - ((touchPointer1.dy / size.height)) * decodedImage.height)
    };
    return data;
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    final screenSize = MediaQueryData.fromWindow(window).size;
    // double canvasHeight =  isPotrait ? size.height*0.9 *hitPer: size.width*hitPer ;
    // double canvasWidth = isPotrait ?  size.height*0.9*widPer   :  size.width*widPer;
    // debugPrint(size.aspectRatio.toString());
    // if( imageAspectRatio > size.aspectRatio ) {
    //   canvasHeight = canvasHeight * imageAspectRatio;
    //   canvasWidth = canvasWidth * imageAspectRatio;
    //   widPer = widPer *imageAspectRatio;
    // }

    // canvasWidth = size.width / imageAspectRatio;
    // canvasHeight = size.height/imageAspectRatio;

    touchPointer1 = Offset(20, 20);
    touchPointer2 = Offset(canvasWidth - 20, 20);
    touchPointer3 = Offset(20, canvasHeight - 20);
    touchPointer4 = Offset(canvasWidth - 20, canvasHeight - 20);
    debugPrint(hitPer.toString());
    debugPrint(widPer.toString());

    return WillPopScope(
      onWillPop: () async {
        ImageService().displayImageFile = ImageService().originalImageFile;

        return true;
      },
      child: Scaffold(
        // backgroundColor: Colors.teal,
        appBar: AppBar(
          title: const Text("Image Cropping"),
          actions: [
            IconButton(
                onPressed: () async {
                  await callMethodChannel();
                  ImageService().originalImageFile =
                      XFile(ImageService().displayImageFile?.path ?? '');
                  Navigator.pop(context);
                },
                icon: const Icon(Icons.check))
          ],
        ),
        body: ImageService().loading
            ? Center(
                child: CircularProgressIndicator(
                color: Colors.blue,
              ))
            : Center(
                child: AspectRatio(
                  aspectRatio: imageAspectRatio,
                  child: Container(
                    // color: Colors.orange,
                    // height: canvasHeight,
                    // width: canvasWidth,
                    child: FutureBuilder<ui.Image>(
                        future: getImage(),
                        builder: (context, snap) {
                          if (snap.data == null) {
                            return const LinearProgressIndicator();
                          } else {
                            return GestureDetector(
                              onPanStart: (detalis) {
                                // debugPrint(detalis.localPosition.toString());
                              },
                              onPanEnd: (details) {},
                              onPanUpdate: (dragDetails) {
                                debugPrint(
                                    "Local  :   ${dragDetails.localPosition}");

                                // if ()
                                final temp =
                                    checkPointer(dragDetails.localPosition);
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

                                if (checkOverlapping(
                                    temp,
                                    dragDetails.localPosition,
                                    paintKey.currentContext!.size!
                                        .center(Offset.zero))) {
                                  if (dragDetails.localPosition.dx < 0 ||
                                      dragDetails.localPosition.dy < 0 ||
                                      dragDetails.localPosition.dx >
                                          canvasWidth ||
                                      dragDetails.localPosition.dy >
                                          canvasHeight) {
                                    return;
                                  }

                                  if (temp == 1) {
                                    touchPointer1 = dragDetails.localPosition;
                                  } else if (temp == 2) {
                                    touchPointer2 = dragDetails.localPosition;
                                  } else if (temp == 3) {
                                    touchPointer3 = dragDetails.localPosition;
                                  } else if (temp == 4) {
                                    touchPointer4 = dragDetails.localPosition;
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

                                      // Size(screenSize.width*0.9, screenSize.height*0.8),
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
                ),
              ),
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
    if (size.width > 1.0 && size.height > 1.0) {
      SizeUtil.size = size;
    }

    var paint = Paint()
      ..style = PaintingStyle.fill
      ..color = Colors.blue
      ..isAntiAlias = true;

    canvas.drawRect(Rect.fromLTRB(0, 0, size.width, size.height), paint);

    paintImage(
        canvas: canvas,
        rect: Rect.fromCenter(
            center: size.center(Offset.zero),
            width: size.width,
            height: size.height),
        image: img);
    paint.color = Colors.blueAccent;
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
