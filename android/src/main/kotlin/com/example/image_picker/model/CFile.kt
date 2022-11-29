package com.example.image_picker.model
enum class FileType{
    Image,
    Video,
}
class CFile(val path:String, val height:Double, val width:Double, val size:Double, val type: FileType){
    fun toMap():Map<String,String>{
        return mutableMapOf<String,String>(Pair("path",path.toString(),),
            Pair("height",height.toString(),),
            Pair("width",width.toString(),),
            Pair("size", size.toString()),
            Pair("type",type.name)
        )
    }
}