package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class WeatherModel(
    var temperature : String,
    var humidity : String,
    var windDirection : String,
    var windSpeed : String,
    var precipitationStatus : String,
    var skyStatus : String
)