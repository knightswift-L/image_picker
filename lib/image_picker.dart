
import 'image_picker_platform_interface.dart';

class ImagePicker {
  Future<List<String>?> pickImage() {
    return ImagePickerPlatform.instance.pickImage();
  }

  Future<List<String>?> takePhoto() {
    return ImagePickerPlatform.instance.takePhoto();
  }
}
