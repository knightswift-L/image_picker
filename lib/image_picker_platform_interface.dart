import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'image_picker_method_channel.dart';

abstract class ImagePickerPlatform extends PlatformInterface {
  /// Constructs a ImagePickerPlatform.
  ImagePickerPlatform() : super(token: _token);

  static final Object _token = Object();

  static ImagePickerPlatform _instance = MethodChannelImagePicker();

  /// The default instance of [ImagePickerPlatform] to use.
  ///
  /// Defaults to [MethodChannelImagePicker].
  static ImagePickerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [ImagePickerPlatform] when
  /// they register themselves.
  static set instance(ImagePickerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<List<String>?> takePhoto() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<List<String>?> pickImage() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
