
import 'image_picker_platform_interface.dart';
import './model/SelectImageConfig.dart';
import './model/XFile.dart';
class ImagePicker {
  Future<List<XFile>> pickImage({SelectedImageConfig? config}) {
    return ImagePickerPlatform.instance.pickImage(config:config);
  }

  Future<List<XFile>> takePhoto({SelectedImageConfig? config}) {
    return ImagePickerPlatform.instance.takePhoto(config:config);
  }
}
