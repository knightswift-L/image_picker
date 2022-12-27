package com.example.image_picker.model

class ZhLanguage :MultiLanguage {
    override fun getCancel(): String {
        return "取消"
    }

    override fun getConfirm(): String {
        return "确认"
    }

    override fun getCrop(): String {
        return "剪切"
    }
}