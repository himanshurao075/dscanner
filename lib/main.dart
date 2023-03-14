import 'dart:io';

import 'package:dscanner/cropsScreen.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';

void main() {
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
                final ImagePicker picker = ImagePicker();
                pickedImages = await picker.pickMultiImage();
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
                                              final ImagePicker picker =
                                                  ImagePicker();
                                              final tempImages =
                                                  await picker.pickMultiImage();
                                              pickedImages.addAll(tempImages);
                                              setState(() {});
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

  retake(int index) async {
    Size size =MediaQuery.of(context).size;
    final ImagePicker picker = ImagePicker();
    final tempImage = await picker.pickImage(source: ImageSource.gallery,maxHeight:1080,maxWidth:720, );
    if (tempImage != null) pickedImages[index] = tempImage;
    setState(() {});
  }

  edit(int index) async {
    Navigator.push(
        context,
        MaterialPageRoute(
            builder: (context) => ImageEditScreen(
                  img: pickedImages[index],
                )));
  }
}

class ImageEditScreen extends StatefulWidget {
  const ImageEditScreen({super.key, required this.img});
  final XFile img;
  @override
  State<ImageEditScreen> createState() => _ImageEditScreenState();
}

class _ImageEditScreenState extends State<ImageEditScreen> {
  
  String croppedImage = '';
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Edit Image")),
      body: Column(
        children: [
          Expanded(
            child: Image.file(
                File(
                  croppedImage.isEmpty?  widget.img.path : croppedImage,

                ),
              fit: BoxFit.contain,
                ),
          ),
          Container(
            color: Colors.black,
            child: ButtonBar(
              alignment: MainAxisAlignment.spaceEvenly,
              children: [
                const Icon(
                  Icons.filter,
                  color: Colors.white,
                ),
                InkWell(
                  onTap: () async {
                    final result = await Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => CropScreen(
                            img: widget.img,
                          ),
                        ));
                    croppedImage = result [0];
                    setState(() {

                    });
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
