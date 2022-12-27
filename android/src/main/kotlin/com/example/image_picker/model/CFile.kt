package com.example.image_picker.model
enum class FileType{
    Image,
    Video,
}
class CFile(val path:String, val height:Int, val width:Int, val size:Int, val type: FileType = FileType.Image){
    fun toMap():Map<String,String>{
        return mutableMapOf<String,String>(Pair("path",path.toString(),),
            Pair("height",height.toString(),),
            Pair("width",width.toString(),),
            Pair("size", size.toString()),
            Pair("type",type.name)
        )
    }
}