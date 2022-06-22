package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class WeatherResponse (
    val response : ResponseObject
)
data class ResponseObject (
    val header : ResponseHeader,
    val body : ResponseBody
)
data class ResponseHeader(
    val resultCode : String,
    val resultMsg : String
)
data class ResponseBody(
    val items : Items
)
data class Items(
    val item : List<Item>
)
data class Item(
    val baseData : Int,
    val baseTime : Int,
    val category : String,
    val fcstTime : String,
    val fcstValue : String
)