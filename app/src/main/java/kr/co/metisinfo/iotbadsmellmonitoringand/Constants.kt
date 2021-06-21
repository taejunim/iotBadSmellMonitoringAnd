package kr.co.metisinfo.iotbadsmellmonitoringand

object Constants {

    //Server Url
    const val serverUrl = "http://101.101.219.152:8080" // 주소

    //Weather API
    const val weatherApiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/" // 날씨 API 주소
    const val serviceKey = "aDVsltIrJTOtDLpTA6qnVPhVhaT%2FaciIUGI30aiipGikIAAZOI4KxfVFBqW9q3s%2B3xgVzKx6c3gJdUVGaNJ9Bg%3D%3D" // 날씨 API serviceKey
    const val dataType = "JSON" // 응답 자료 형식
    const val numOfRows = "1000" // 한 페이지당 결과 수
    const val nx = "48" //지역 X
    const val ny = "36" //지역 Y
}