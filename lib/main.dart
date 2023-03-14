import 'dart:io';
import 'dart:ui';

import 'package:camera/camera.dart';
import 'package:dscanner/cropsScreen.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

// late List<CameraDescription> _cameras;
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // _cameras = await availableCameras();
  runApp(MaterialApp(
    debugShowCheckedModeBanner: false,
    color: Colors.teal,
    themeMode: ThemeMode.light,
    theme: ThemeData(primarySwatch: Colors.teal),
    home: MainApp(),
  ));
}

class MainApp extends StatefulWidget {
  MainApp({super.key});

  @override
  State<MainApp> createState() => _MainAppState();
}

class _MainAppState extends State<MainApp> {
  List<XFile> pickedImages = [];
  XFile? pickImage;

  // Future<List<XFile>> getImages() async {
  //   pickedImages = await picker.pickMultiImage();
  //   return pickedImages;
  // }
  showDailogFunc({int index = -1}) {
    final ImagePicker picker = ImagePicker();
    showDialog(
      context: context,
      builder: (context) {
        return SimpleDialog(
          title: Text("Choose how do you want image"),
          children: [
            ButtonBar(
              children: [
                IconButton(
                    onPressed: () async {
                      pickImage = await picker.pickImage(
                        source: ImageSource.camera,
                        maxHeight: 1080,
                        maxWidth: 720,
                      );

                      if (index == -1)
                        pickedImages.add(pickImage!);
                      else {
                        pickedImages[index] = pickImage!;
                      }

                      Navigator.pop(context);
                      setState(() {});
                    },
                    icon: Icon(Icons.camera)),
                IconButton(
                    onPressed: () async {
                      if (index == -1) {
                        final image = await picker.pickMultiImage();
                        pickedImages.addAll(image);
                      } else {
                        XFile? image =
                            await picker.pickImage(source: ImageSource.gallery);

                        pickedImages[index] = image!;
                      }
                      setState(() {});
                      Navigator.pop(context);
                    },
                    icon: Icon(Icons.image)),
              ],
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Document Scanner"),
      ),
      floatingActionButton: pickedImages.isNotEmpty
          ? null
          : FloatingActionButton(
              onPressed: () async {
                showDailogFunc();
                // await getImages();
                setState(() {});
              },
              child: const Icon(Icons.add),
            ),
      body: pickedImages.isEmpty
          ? const Center(
              child: Text('Press + to add images'),
            )
          : Padding(
              padding: const EdgeInsets.all(8.0),
              child: SingleChildScrollView(
                child: Column(
                  children: [
                    GridView.builder(
                      shrinkWrap: true,
                      physics: const NeverScrollableScrollPhysics(),
                      itemCount: pickedImages.length + 1,
                      gridDelegate:
                          const SliverGridDelegateWithFixedCrossAxisCount(
                              mainAxisSpacing: 5,
                              crossAxisSpacing: 5,
                              crossAxisCount: 2,
                              childAspectRatio: 0.73),
                      itemBuilder: (context, index) {
                        return index == pickedImages.length
                            ? Card(
                                elevation: 0,
                                shape: RoundedRectangleBorder(
                                    borderRadius: BorderRadius.circular(5),
                                    side: const BorderSide(color: Colors.teal)),
                                child: Padding(
                                    padding: const EdgeInsets.all(2.0),
                                    child: Column(
                                      mainAxisAlignment:
                                          MainAxisAlignment.center,
                                      children: [
                                        Center(
                                            child: CircleAvatar(
                                          child: InkWell(
                                            onTap: () async {
                                              // final tempImages = await getImages();
                                              // pickedImages.addAll(tempImages);
                                              // setState(() {});
                                              showDailogFunc();
                                            },
                                            child: const Icon(
                                              Icons.add,
                                              color: Colors.white,
                                            ),
                                          ),
                                        )),
                                        const SizedBox(
                                          height: 5,
                                        ),
                                        const Text(
                                          "Add more images",
                                          style: TextStyle(
                                              color: Colors.teal,
                                              fontWeight: FontWeight.w500),
                                        )
                                      ],
                                    )),
                              )
                            : Card(
                                elevation: 5,
                                color: Colors.teal,
                                child: Padding(
                                  padding: const EdgeInsets.all(2.0),
                                  child: Column(
                                    children: [
                                      Row(
                                        mainAxisAlignment:
                                            MainAxisAlignment.spaceBetween,
                                        children: [
                                          Text(
                                            "Image ${index + 1}",
                                            style: const TextStyle(
                                                color: Colors.white,
                                                fontWeight: FontWeight.w700),
                                          ),
                                          ButtonBar(
                                            children: [
                                              InkWell(
                                                onTap: () async {
                                                  await edit(index);
                                                },
                                                child: const Icon(
                                                  Icons.edit,
                                                  color: Colors.white,
                                                ),
                                              ),
                                              InkWell(
                                                onTap: () async {
                                                  await retake(index);
                                                },
                                                child: const Icon(
                                                  Icons.replay,
                                                  color: Colors.white,
                                                ),
                                              ),
                                              InkWell(
                                                onTap: () {
                                                  pickedImages.removeAt(index);
                                                  setState(() {});
                                                },
                                                child: const Icon(
                                                  Icons.delete,
                                                  color: Colors.white,
                                                ),
                                              )
                                            ],
                                          )
                                        ],
                                      ),
                                      const Spacer(),
                                      Image.file(
                                        File(
                                          pickedImages[index].path,
                                        ),
                                        height: 200,
                                        width: double.maxFinite,
                                        fit: BoxFit.fill,
                                      )
                                    ],
                                  ),
                                ),
                              );
                      },
                    )
                  ],
                ),
              ),
            ),
    );
  }

  // Future<XFile?> getImage() async {
  //   // final result = await Navigator.push(
  //   //     context, MaterialPageRoute(builder: (context) => CameraView(),));
  //
  //   final img = picker.pickImage(
  //     source: ImageSource.camera,
  //     maxHeight: 1080,
  //     maxWidth: 720,
  //   );
  //
  //   return img;
  // }

  retake(int index) async {
    Size size = MediaQuery.of(context).size;
    final tempImage = await showDailogFunc(index: index);
    if (tempImage != null) pickedImages[index] = tempImage;
    setState(() {});
  }

  edit(int index) async {
    String result = await Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => ImageEditScreen(
                  img: pickedImages[index],
                )));

    pickedImages[index] = XFile(result);
    setState(() {});
  }
}

// class CameraView extends StatefulWidget {
//   const CameraView({Key? key}) : super(key: key);
//
//   @override
//   State<CameraView> createState() => _CameraViewState();
// }
//
// class _CameraViewState extends State<CameraView> {
//   late CameraController _camera;
//   bool _cameraInitialized = false;
//   late CameraImage _savedImage;
//
//   void _initializeCamera() async {
//     // Get list of cameras of the device
//     List<CameraDescription> cameras = await availableCameras();
// // Create the CameraController
//     _camera = CameraController(
//       cameras[0],
//       ResolutionPreset.veryHigh,
//     );
// // Initialize the CameraController
//     _camera.initialize().then((_) async {
//       // Start ImageStream
//       await _camera
//           .startImageStream((CameraImage image) => _processCameraImage(image));
//       setState(() {
//         _cameraInitialized = true;
//       });
//     });
//   }
//
//   void _processCameraImage(CameraImage image) async {
//     dynamic rowdata = image.planes;
//
//     final String result =
//         await platform.invokeMethod('scanner', {"rowdata": rowdata});
//
//     debugPrint("Method Channel Result : $result");
//     setState(() {
//       _savedImage = image;
//     });
//   }
//
//   @override
//   void initState() {
//     super.initState();
//     _initializeCamera();
//   }
//
//   static const platform = MethodChannel('samples.flutter.dev/cropImage');
//
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//         body: Column(
//       children: [
//         (_cameraInitialized)
//             ? Expanded(child: CameraPreview(_camera))
//             : CircularProgressIndicator(),
//         ButtonBar(
//           alignment: MainAxisAlignment.center,
//           children: [
//             IconButton(
//                 onPressed: () async {
//                   try {
//                     // final result=  await _camera.takePicture();
//                     //
//                     //   Navigator.pop(context,result);
//                     // // _camera.dispose();
//                   } catch (e) {
//                     debugPrint("Exp : $e");
//                   }
//                 },
//                 icon: Icon(
//                   Icons.camera,
//                   size: 40,
//                 ))
//           ],
//         )
//       ],
//     ));
//   }
// }

class ImageEditScreen extends StatefulWidget {
  const ImageEditScreen({super.key, required this.img});

  final XFile img;

  @override
  State<ImageEditScreen> createState() => _ImageEditScreenState();
}

class _ImageEditScreenState extends State<ImageEditScreen> {
  String croppedImage = '';
  int selectedButtonIndex = -1;
  int selectedFilterIndex = 0;
  List<ColorFilter> filters = [
    const ColorFilter.mode(Colors.transparent, BlendMode.saturation),
    const ColorFilter.mode(Colors.black45, BlendMode.colorBurn),
    const ColorFilter.mode(Colors.grey, BlendMode.saturation),
  ];
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Edit Image"),
        actions: [
          IconButton(
              onPressed: () {
                Navigator.pop(context, croppedImage);
              },
              icon: Icon(Icons.check))
        ],
      ),
      body: Column(
        children: [
          // if (croppedImage.isNotEmpty)
          //   Expanded(
          //     child: Image.file(
          //       File(
          //         croppedImage,
          //       ),
          //       fit: BoxFit.contain,
          //       height: 1080,
          //       width: 720,
          //     ),
          //   ),
          // if (croppedImage.isEmpty)
          Expanded(
            child: ImageFiltered(
              imageFilter: filters[selectedFilterIndex],
              child: Image.file(
                File(croppedImage.isEmpty ? widget.img.path : croppedImage),
                fit: BoxFit.fill,
                // height: 1080,
                //   width: 720,
              ),
            ),
          ),
          if (croppedImage.isNotEmpty) const Spacer(),
          if (selectedButtonIndex == 0)
            Container(
              color: Colors.black,
              child:
                  Row(mainAxisAlignment: MainAxisAlignment.center, children: [
                TextButton(
                    onPressed: () {
                      selectedFilterIndex = 0;
                      setState(() {});
                    },
                    child: Text(
                      "Original",
                      style: TextStyle(
                          color: selectedFilterIndex == 0
                              ? Colors.teal
                              : Colors.white),
                    )),
                TextButton(
                    onPressed: () {
                      selectedFilterIndex = 1;
                      setState(() {});
                    },
                    child: Text(
                      "Back & White",
                      style: TextStyle(
                          color: selectedFilterIndex == 1
                              ? Colors.teal
                              : Colors.white),
                    )),
                TextButton(
                    onPressed: () {
                      selectedFilterIndex = 2;
                      setState(() {});
                    },
                    child: Text(
                      "Grayscale",
                      style: TextStyle(
                          color: selectedFilterIndex == 2
                              ? Colors.teal
                              : Colors.white),
                    )),
              ]),
            ),
          Container(
            color: Colors.black,
            child: ButtonBar(
              alignment: MainAxisAlignment.spaceEvenly,
              children: [
                InkWell(
                  onTap: () {
                    selectedButtonIndex = 0;
                    setState(() {});
                  },
                  child: const Icon(
                    Icons.filter,
                    color: Colors.white,
                  ),
                ),
                InkWell(
                  onTap: () async {
                    selectedButtonIndex = 1;
                    setState(() {});
                    final result = await Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => CropScreen(
                            img: widget.img,
                          ),
                        ));
                    croppedImage = result[0];
                    final file = File(croppedImage);
                    var decodedImage =
                        await decodeImageFromList(file.readAsBytesSync());
                    setState(() {});
                    debugPrint("Result DAta  ${result}");
                    // callMethodChannel();
                  },
                  child: const Icon(
                    Icons.crop,
                    color: Colors.white,
                  ),
                ),
                const Icon(
                  Icons.brightness_4_outlined,
                  color: Colors.white,
                ),
                const Icon(Icons.rotate_90_degrees_ccw, color: Colors.white)
              ],
            ),
          )
        ],
      ),
    );
  }
}
