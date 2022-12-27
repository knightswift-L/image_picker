import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import './model/SelectImageConfig.dart';
import 'image_picker_platform_interface.dart';
import './model/XFile.dart';
/// An implementation of [ImagePickerPlatform] that uses method channels.
class MethodChannelImagePicker extends ImagePickerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('image_picker');

  @override
  Future<List<XFile>> takePhoto({SelectedImageConfig? config}) async {
    config = config ?? SelectedImageConfig();
    final result = await methodChannel.invokeMethod<List>('takePhoto',config.toJson());
    return result?.map((e) => XFile.fromJson(Map<String,String>.from(e))).toList() ?? <XFile>[];
  }

  @override
  Future<List<XFile>> pickImage({SelectedImageConfig? config}) async {
    config = config ?? SelectedImageConfig();
    final result = await methodChannel.invokeMethod<List>('pickImage',config.toJson());
    return result?.map((e) => XFile.fromJson(Map<String,String>.from(e))).toList() ?? <XFile>[];
  }
}
