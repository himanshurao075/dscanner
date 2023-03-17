import 'dart:io';
import 'dart:typed_data';

import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import 'package:open_app_file/open_app_file.dart';


class PdfFileModel {
  PdfFileModel({this.path= '',this.name='',this.size="",required this.bytes});

  String name = '';
  String size = '';
  Uint8List? bytes  ;
  String path = '';
}

//ignore: must_be_immutable
class ExcelFilesList extends StatefulWidget {
  Function(String)? selectedPath;

  ExcelFilesList({Key? key, this.selectedPath}) : super(key: key);

  @override
  State<ExcelFilesList> createState() => _ExcelFilesListState();
}

class _ExcelFilesListState extends State<ExcelFilesList> {
  List<PdfFileModel> _folders =[];

  @override
  initState() {
    super.initState();
    getExcelList();
  }

  getExcelList() async {
   final files = await excelFileList();
   for (var element in files) {
     final bytes  = File(element.path).readAsBytesSync();
      final name = element.path.split("/").last;
      double length = (bytes.length/1024);
      String unit = "kB";
      if(length>1023) {
        length = length/1024;
        unit = "MB";
      }
      if(length>1023) {
        length = length/1024;
        unit = "GB";
      }

      final size= "${length.toStringAsFixed(2)} $unit";

      final path = element.path;
      _folders.add(PdfFileModel(path:path , bytes: bytes,name: name,size: size));
   }
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Generated Excel Files'),
      ),
      body: _folders == null
          ? const Center(
              child: CircularProgressIndicator(),
            )
          : _folders!.isEmpty
              ? const Text('No File Available')
              : Container(
                  padding: const EdgeInsets.all(10),
                  decoration: const BoxDecoration(
                      borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(10),
                    topRight: Radius.circular(10),
                  )),
                  child: ListView.builder(
                    itemCount: _folders!.length,
                    itemBuilder: (BuildContext context, int index) {

                      final file =  _folders[index];


                      return Card(
                        child: ListTile(
                          onTap: () {
                            // widget.selectedPath!(_folders![index].path);
                          },
                          title: Text(
                            file.name,
                            style: Theme.of(context).textTheme.bodyText1,
                          ),
                          subtitle: Row(
                            mainAxisAlignment: MainAxisAlignment.end,
                            children: [Text(file.size),
                              IconButton(
                                icon: Icon(
                                  Icons.remove_red_eye_sharp,
                                  color:
                                      Theme.of(context).colorScheme.secondary,
                                ),
                                onPressed: () {
                                  OpenAppFile.open(_folders![index].path);
                                },
                              ),
                              IconButton(
                                icon: Icon(
                                  Icons.share,
                                  color: Theme.of(context).colorScheme.primary,
                                ),
                                onPressed: () {
                                  Share.shareXFiles(
                                      [XFile(_folders![index].path)]);
                                },
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  )),
    );
  }
}

Future<List<FileSystemEntity>> excelFileList({type = ".pdf"}) async {
  final String path = await createFolderInAppDocDir();
  final String pdfDirectory = '$path/';
  final myDir = Directory(pdfDirectory);
  var list = myDir.listSync(recursive: true, followLinks: false);

  /// sorting the only the specific file in the folder.
  list = list.where((element) => element.path.contains(type)).toList();
  return list;
}

Future<String> createFolderInAppDocDir({String folderName = 'dScanner'}) async {
  final Directory appDocDir = await getApplicationDocumentsDirectory();
  final Directory appDocDirFolder = Directory('${appDocDir.path}/$folderName');
  if (await appDocDirFolder.exists()) {
    return appDocDirFolder.path;
  } else {
    final Directory appDocDirNewFolder =
        await appDocDirFolder.create(recursive: true);
    return appDocDirNewFolder.path;
  }
}
