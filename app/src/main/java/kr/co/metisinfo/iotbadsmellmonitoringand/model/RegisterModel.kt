package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class RegisterModel(

    val resultCode : String,
    val smellType : String,
    val smellValue : String,
    val smellValueName : String,
    val weatherState : String,
    val temperatureValue : String,
    val humidityValue : String,
    val windDirectionValue : String,
    val windSpeedValue : String,
    val gpsX : String,
    val gpsY : String,
    val smellComment : String,
    val smellRegisterTime : String,
    val smellRegisterTimeName : String,
    val regId : String,
    val regDt : String,
    var isExpanded : Boolean                                                        // 열림상태
)