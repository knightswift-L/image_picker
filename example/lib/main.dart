import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:image_picker/model/SelectImageConfig.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _imagePickerPlugin = ImagePicker();
  String? image;
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
            width: double.infinity,
            height: double.infinity,
            child: Column(
              children: [
                GestureDetector(
                  onTap: () async {
                    try {
                      var config = SelectedImageConfig(cropOption: CropImageOption(aspectRatioX: 3,aspectRatioY: 4));
                      var images = await _imagePickerPlugin.pickImage(config: config);
                      print("==========>${images?.join(",")}");
                      if (images != null && images.isNotEmpty) {
                        setState(() {
                          image = images.first.path;
                        });
                      }
                    } catch (error) {
                    }
                  },
                  child: Text('PickImage'),
                ),
                SizedBox(
                  height: 50,
                ),
                GestureDetector(
                  onTap: () async {
                    try {
                      var config = SelectedImageConfig(cropOption: CropImageOption(aspectRatioX: 3,aspectRatioY: 4));
                      var images = await _imagePickerPlugin.takePhoto(config: config);
                      print("==========>${images?.join(",")}");
                      if (images != null && images.isNotEmpty) {
                        setState(() {
                          image = images.first.path!;
                        });
                      }
                    } catch (error) {
                      print(error.toString());
                    }
                  },
                  child: const Text('Take Photo'),
                ),
                if (image != null)
                  Expanded(
                      child: Image.file(
                    File(image!),
                    fit: BoxFit.fitHeight,
                        width: double.infinity,
                  ))
              ],
            )),
      ),
    );
  }
}
