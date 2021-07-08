package kr.co.metisinfo.iotbadsmellmonitoringand

object Constants {

    //Server Url
    const val serverUrl = "http://101.101.219.152:8080" // 주소

    //Weather API
    const val weatherApiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService/" // 날씨 API 주소
    const val serviceKey = "dFNlgyX4FFci5kW2VH%2FnG6IIFGt8NR2vvkjUw3C5RfN8IOUY1xE9D0HzzraWWPJpPfMUgjc55LHj4NCsQVRxwQ%3D%3D" // 날씨 API serviceKey
    const val dataType = "JSON" // 응답 자료 형식
    const val numOfRows = "1000" // 한 페이지당 결과 수
    const val nx = "48" //지역 X
    const val ny = "36" //지역 Y

    const val TIME_00 = "00:00"
    const val TIME_06 = "06:00"
    const val TIME_09 = "09:00"
    const val TIME_18 = "18:00"
    const val TIME_21 = "21:00"
}