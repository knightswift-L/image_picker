package com.example.image_picker.model

interface MultiLanguage {
    fun getCancel():String
    fun getConfirm():String
    fun getCrop():String
}

enum class Language {
    ZH,
    EN,
    TH
}


object GlobalLanguage{
    private var globalCurrentLanguage = Language.ZH
    private val zh by lazy { ZhLanguage() }
    private val en by lazy { EnLanguage() }
    private val th by lazy { ThLanguage() }
    fun switch(language: String){
        val languageType = when(language){
            "zh" -> Language.ZH
            "en" -> Language.EN
            else -> Language.TH
        }
        globalCurrentLanguage = languageType
    }
    fun getCurrentLanguage():MultiLanguage{
        return when(globalCurrentLanguage){
            Language.ZH -> zh
            Language.EN -> en
            else -> th
        }
    }
}