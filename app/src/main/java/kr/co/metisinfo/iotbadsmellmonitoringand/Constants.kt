package kr.co.metisinfo.iotbadsmellmonitoringand

object Constants {

    //Server Url
    //const val serverUrl = "http://172.30.1.61:8080" // 로컬 서버 IP
    //const val serverUrl = "http://14.49.221.213:9999" // 개발 서버 IP
    const val serverUrl = "http://49.50.172.217:8080" // 운영 서버 IP

    //Weather API
    const val weatherApiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/" // 날씨 API 주소
    const val serviceKey = "aDVsltIrJTOtDLpTA6qnVPhVhaT/aciIUGI30aiipGikIAAZOI4KxfVFBqW9q3s+3xgVzKx6c3gJdUVGaNJ9Bg==" // 날씨 API serviceKey
    const val dataType = "JSON" // 응답 자료 형식
    const val numOfRows = "1000" // 한 페이지당 결과 수

    //배경색 구분을 위한 시간
    const val TIME_00 = "00:00"
    const val TIME_06 = "06:00"
    const val TIME_09 = "09:00"
    const val TIME_18 = "18:00"
    const val TIME_21 = "21:00"

    //푸시 시간
    const val PUSH_TIME_00 = "00:00:00"
    const val PUSH_TIME_06 = "06:00:00"
    const val PUSH_TIME_11 = "11:00:00"
    const val PUSH_TIME_19 = "19:00:00"
    const val PUSH_TIME_22 = "22:00:00"
    const val TEST_TIME = "16:35:20"
}