import 'dart:io';

import 'package:dscanner/ImageService.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

import 'cropsScreen.dart';

class ImageEditScreen extends StatefulWidget {
  const ImageEditScreen({super.key, });

  @override
  State<ImageEditScreen> createState() => _ImageEditScreenState();
}

class _ImageEditScreenState extends State<ImageEditScreen> {
  static const platform = MethodChannel('samples.flutter.dev/dscanner');
  String croppedImage = '';
  var rotatedBytes;
  int buttonIndex = -1;
  List filteredImages = [];

  double angle = 0;

  getFilteredImages(String originalImagPath) async {
    final result = await platform
        .invokeMethod("filtersImages", {"imgPath":originalImagPath });
    debugPrint(result.toString());
    filteredImages = result;
  }

  // String displayImg = "";
  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    // displayImg = widget.img.path;
  }


  @override
  Widget build(BuildContext context) {
  final displayImage = ImageService().displayImageFile?.path??'';

    return Scaffold(
      backgroundColor: Colors.teal,
      appBar: AppBar(title: const Text("Edit Image")),
      body: Column(
        children: [
          Expanded(
            child: Image.file(
              File(displayImage),
              // fit: BoxFit.contain,
              // height: 1080,
              // width: 720,
            ),
          ),

          if (croppedImage.isNotEmpty) const Spacer(),
          if (buttonIndex == 0)
            Container(
              color: Colors.black,
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: List.generate(3, (index) {
                    final label =
                    ["Original", "Whiteboard", "Grayscale"][index];
                    return Column(
                      children: [
                        filteredImages.isEmpty
                            ? Container(
                          height: 100,
                          width: 80,
                          color: Colors.white,
                        )
                            : InkWell(
                            onTap: () {
                              ImageService().displayImageFile  = XFile(filteredImages[index]);
                              setState(() {});
                            },
                            child: Image.file(
                              File(filteredImages[index]),
                              height: 100,
                              width: 80,
                            )),
                        Text(
                          label,
                          style: TextStyle(color: Colors.white),
                        )
                      ],
                    );
                  }),
                ),
              ),
            ),
          Container(
            color: Colors.black,
            child: ButtonBar(
              alignment: MainAxisAlignment.spaceEvenly,
              children: [
                InkWell(
                  onTap: () async {
                    buttonIndex = 0;
                    await getFilteredImages(ImageService().originalImageFile?.path??'');
                    setState(() {});
                  },
                  child: const Icon(
                    Icons.filter,
                    color: Colors.white,
                  ),
                ),
                InkWell(
                  onTap: () async {
                    buttonIndex = 1;
                    setState(() {});
                    final result = await Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => CropScreen(

                          ),
                        ));
                    // croppedImage = result[0];
                    // displayImg = croppedImage;
                    // final file = File(croppedImage);
                    // var decodedImage =
                    // await decodeImageFromList(file.readAsBytesSync());
                    // setState(() {});
                    // debugPrint("Result DAta  $result");
                    // callMethodChannel();
                  },
                  child: const Icon(
                    Icons.crop,
                    color: Colors.white,
                  ),
                ),

                IconButton(
                    icon: const Icon(Icons.rotate_90_degrees_ccw),
                    onPressed: () async {
                      // String img = widget.img.path;
                      // print("Before rotation $img");
                      buttonIndex =2;
                      String rotateImage = await platform.invokeMethod('rotate', {
                        "imgPath": displayImage,
                        "angle": 90.0,
                      });
                      ImageService().displayImageFile = XFile(rotateImage);
                      ImageService().originalImageFile = XFile(rotateImage);
                      setState(() {

                      });
                      // rotatedBytes = rotateImage;
                      // displayImg = rotatedBytes;
                      // print(rotateImage);
                      // print("After rotation ${rotatedBytes}");
                      // if (angle > 270) {
                      //   angle = 0;
                      //   setState(() {});
                      // } else {
                      //   angle = angle + 90.0;
                      //   setState(() {});
                      // }
                      // rotateImage = await platform.invokeMethod('rotate', {
                      //   "imgPath": croppedImage.isNotEmpty
                      //       ? croppedImage
                      //       : widget.img.path,
                      //   "angle": angle,
                      // });
                      // setState(() {});
                      // rotatedBytes = rotateImage;
                      // setState(() {});
                      // if (kDebugMode) {
                      //   print(rotateImage);
                      // }
                      // croppedImage = XFile.fromData(rotateImage).path;
                    },
                    color: Colors.white)
              ],
            ),
          )
        ],
      ),
    );
  }
}
