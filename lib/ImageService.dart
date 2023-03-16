import 'package:image_picker/image_picker.dart';

class ImageService  {
  static final ImageService _instance = ImageService._internal();

  factory ImageService(){
    return _instance;
  }

  ImageService._internal() ;
  // {
    // displayImagePath = imgpath;
    // originalImage = imgpath;
  // }

  XFile? _originalImageFile ;
  XFile? _displayImageFile ;




  XFile? get originalImageFile => _originalImageFile;
  set originalImageFile(XFile? value) {
    _originalImageFile = value;
  }

  XFile? get displayImageFile => _displayImageFile;
  set displayImageFile(XFile? value) {
    _displayImageFile = value;
  }
  List<XFile> _pickedImages = [];

  List<XFile> get pickedImages => _pickedImages;

  set pickedImages(List<XFile> value) {
    _pickedImages = value;
  }
}