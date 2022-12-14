class SelectedImageConfig{
  CropImageOption? cropOption;
  int maxQuality;
  /// only support zh,en,th
  /// default zh
  String language;
  SelectedImageConfig({this.cropOption,this.maxQuality = 1,this.language = "zh"});
  Map<String,dynamic> toJson(){
    Map<String,dynamic> map = {};
    if(cropOption != null) map["cropOption"] = cropOption!.toJson();
    if(maxQuality != null) map["maxQuality"] = maxQuality.toString();
    if(language != null) map["language"] = language;
    return map;
  }
}

class CropImageOption{
  int aspectRatioX;
  int aspectRatioY;

  CropImageOption({required this.aspectRatioX,required this.aspectRatioY});

  Map<String,String> toJson(){
    Map<String,String> map = {};
    map["aspectRatioX"] = aspectRatioX.toString();
    map["aspectRatioY"] = aspectRatioY.toString();
    return map;
  }

}