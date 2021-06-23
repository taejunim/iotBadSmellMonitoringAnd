package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class RegisterModel(
    val smellRegisterTimeName : String,
    val resultCode : String,
    val regDt : String,
    val smellRegisterTime : String
)