import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'image_picker_platform_interface.dart';

/// An implementation of [ImagePickerPlatform] that uses method channels.
class MethodChannelImagePicker extends ImagePickerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('image_picker');

  @override
  Future<List<String>?> takePhoto() async {
    final result = await methodChannel.invokeMethod<List>('takePhoto');
    return result?.map((e) => e as String).toList();
  }

  @override
  Future<List<String>?> pickImage() async {
    final result = await methodChannel.invokeMethod<List>('pickImage');
    return result?.map((e) => e as String).toList();
  }
}
