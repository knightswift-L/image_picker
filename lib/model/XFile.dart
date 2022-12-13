

class XFile{
  String? path;
  double? width;
  double? height;
  XFile({this.path,this.width,this.height});
  XFile.fromJson(Map<String,String> json){
    this.path = json["path"];
    this.width = json["width"] != null ? double.parse(json["width"]!) : null;
    this.height = json["height"] != null ? double.parse(json["height"]!) : null;
  }
}