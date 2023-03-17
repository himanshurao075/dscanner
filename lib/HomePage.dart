import 'dart:io';
import 'dart:math';
import 'package:dscanner/ImageService.dart';
import 'package:flutter/material.dart';
import 'package:flutter_native_image/flutter_native_image.dart';
import 'package:image_picker/image_picker.dart';
import 'package:share_plus/share_plus.dart';

import 'ImageEditPage.dart';
import 'package:pdf/widgets.dart' as pw;

import 'excel_list_screen.dart';

class Homepage extends StatefulWidget {
  const Homepage({super.key});

  @override
  State<Homepage> createState() => _HomepageState();
}

class _HomepageState extends State<Homepage> {
  final ImagePicker picker = ImagePicker();

  showDialogFunc({int index = -1}) async {
    return await showDialog(
      context: context,
      builder: (context) {
        return SimpleDialog(
          title: const Text("How do you want choose an image"),
          children: [
            ButtonBar(
              children: [
                TextButton(
                    onPressed: () async {
                      await pickImageFromCamera(index: index);
                      // ignore: use_build_context_synchronously
                      Navigator.pop(context);
                      setState(() {});
                    },
                    child: const Text("Camera")),
                TextButton(
                    onPressed: () async {
                      await pickImageFromGallery(index: index);
                      // ignore: use_build_context_synchronously
                      Navigator.pop(context);
                      setState(() {});
                    },
                    child: const Text("Gallery")),
              ],
            ),
          ],
        );
      },
    );
  }

  pickImageFromCamera({int index = -1}) async {
    ImageService().loading = true;
    setState(() {});
    if (index == -1) {
      final file = await picker.pickImage(source: ImageSource.camera);
      if (file != null) ImageService().pickedImages.add(file);
    } else {
      final file = await picker.pickImage(source: ImageSource.camera);
      if (file != null) {
        ImageService().pickedImages[index] = file;
      }
    }
    ImageService().loading = false;
    setState(() {});
  }

  pickImageFromGallery({int index = -1}) async {
    if (index == -1) {
      final files = await picker.pickMultiImage();
      ImageService().pickedImages.addAll(files);
    } else {
      final file = await picker.pickImage(source: ImageSource.gallery);
      if (file != null) {
        ImageService().pickedImages[index] = file;
      }
    }
  }

  imagePdfBuilder() async {
    String pdfName = '';
    final pdf = pw.Document();
    var dir = await createFolderInAppDocDir();

    for (int i = 0; i <= ImageService().pickedImages.length - 1; i++) {
      // var data = await rootBundle.load('assets/OpenSans-Bold.ttf');
      // var myFont = pw.Font.ttf(data);
      final compressImageBytes  = await FlutterNativeImage.compressImage(ImageService().pickedImages[i].path,quality: 50,percentage: 50);
      final image =
          pw.MemoryImage(compressImageBytes.readAsBytesSync());
      pdf.addPage(
        pw.Page(
          build: (pw.Context context) => pw.Center(
            child: pw.Image(
              image,
            ),
          ),
        ),
      );
    }

    ///popup to ask pdf name
    await showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: TextFormField(
            decoration: const InputDecoration(
              hintText: 'Enter Pdf name',
            ),
            onChanged: (val) {
              pdfName = val;
              setState(() {});
            },
          ),
          actions: [
            TextButton(
              onPressed: () async {
                var random =  Random(99999999);
                final file = File(
                    pdfName.isEmpty ? "$dir/$random.pdf" : "$dir/$pdfName.pdf");

                ImageService().newFile =
                    await file.writeAsBytes(await pdf.save());
                print(ImageService().newFile);
                // Share.shareXFiles([XFile(newFile.path)]);
                Navigator.pop(context);
              },
              child: const Text(
                'Save File',
              ),
            ),
            TextButton(
              onPressed: () async {
                var random = Random(99999999);
                final file = File(
                    pdfName.isEmpty ? "$dir/$random.pdf" : "$dir/$pdfName.pdf");
                ImageService().newFile =
                    await file.writeAsBytes(await pdf.save());

                print(ImageService().newFile);
                Share.shareXFiles([XFile(ImageService().newFile?.path ?? '')]);
                Navigator.pop(context);
              },
              child: const Text(
                'Share file',
              ),
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
        actions: [
          IconButton(
            onPressed: () async {
              ImageService().pickedImages.isEmpty
                  ? ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('No image is selected')))
                  : imagePdfBuilder();
            },
            icon: const Icon(Icons.picture_as_pdf),
          ),
          if (ImageService().pickedImages.isNotEmpty)
            IconButton(
              onPressed: () async {
                ImageService().pickedImages = List.empty(growable: true);
                setState(() {});
              },
              icon: const Icon(Icons.clear),
            ),
          IconButton(
            onPressed: () async {
              Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => ExcelFilesList(),
                  ));
            },
            icon: const Icon(Icons.save),
          ),
        ],
      ),
      floatingActionButton: ImageService().pickedImages.isNotEmpty
          ? null
          : FloatingActionButton(
              onPressed: () async {
                await showDialogFunc();
              },
              child: const Icon(Icons.add),
            ),
      body: ImageService().loading
          ? const Center(
              child: CircularProgressIndicator(
              color: Colors.blue,
            ))
          : ImageService().pickedImages.isEmpty
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
                          itemCount: ImageService().pickedImages.length + 1,
                          gridDelegate:
                              const SliverGridDelegateWithFixedCrossAxisCount(
                                  mainAxisSpacing: 5,
                                  crossAxisSpacing: 5,
                                  crossAxisCount: 2,
                                  childAspectRatio: 0.73),
                          itemBuilder: (context, index) {
                            return index == ImageService().pickedImages.length
                                ? InkWell(
                                    onTap: () async {
                                      await showDialogFunc();
                                    },
                                    child: Card(
                                      elevation: 0,
                                      shape: RoundedRectangleBorder(
                                          borderRadius:
                                              BorderRadius.circular(5),
                                          side: const BorderSide(
                                              color: Colors.teal)),
                                      child: Padding(
                                          padding: const EdgeInsets.all(2.0),
                                          child: Column(
                                            mainAxisAlignment:
                                                MainAxisAlignment.center,
                                            children: const [
                                              Center(
                                                  child: CircleAvatar(
                                                child: Icon(
                                                  Icons.add,
                                                  color: Colors.white,
                                                ),
                                              )),
                                              SizedBox(
                                                height: 5,
                                              ),
                                              Text(
                                                "Add more images",
                                                style: TextStyle(
                                                    color: Colors.teal,
                                                    fontWeight:
                                                        FontWeight.w500),
                                              )
                                            ],
                                          )),
                                    ),
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
                                                    fontWeight:
                                                        FontWeight.w700),
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
                                                      ImageService()
                                                          .pickedImages
                                                          .removeAt(index);
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
                                              ImageService()
                                                  .pickedImages[index]
                                                  .path,
                                            ),
                                            height: 200,
                                            width: double.maxFinite,
                                            fit: BoxFit.contain,
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
    await showDialogFunc(index: index);
  }

  edit(int index) async {
    ImageService().displayImageFile = ImageService().pickedImages[index];
    ImageService().originalImageFile = ImageService().pickedImages[index];
    ImageService().selectImageIndex = index;
    await Navigator.push(context,
        MaterialPageRoute(builder: (context) => const ImageEditScreen()));
    ImageService().pickedImages[ImageService().selectImageIndex] =
        ImageService().originalImageFile!;
    setState(() {});
  }
}
