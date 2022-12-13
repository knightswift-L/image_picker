class SelectedImageConfig{
  CropImageOption? cropOption;
  int maxQuality;

  SelectedImageConfig({this.cropOption,this.maxQuality = 1});
  Map<String,dynamic> toJson(){
    Map<String,dynamic> map = {};
    if(cropOption != null) map["cropOption"] = cropOption!.toJson();
    if(maxQuality != null) map["maxQuality"] = maxQuality.toString();
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