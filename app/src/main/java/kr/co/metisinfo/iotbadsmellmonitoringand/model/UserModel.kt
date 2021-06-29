package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class UserModel(
    private val userId: String,
    private val userPassword: String,
    private val userAge: String,
    private val userName: String,
    private val userSex: String,
    private val userSexName: String,
    private val userType: String,
    private val userTypeName: String,
    private val userRegion: String
)
