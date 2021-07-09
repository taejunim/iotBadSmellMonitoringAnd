package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class UserModel(
    val userId: String,
    val userPassword: String,
    val userAge: String,
    val userName: String,
    val userSex: String,
    val userSexName: String,
    val userType: String,
    val userTypeName: String,
    val userRegion: String
)
