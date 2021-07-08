package kr.co.metisinfo.iotbadsmellmonitoringand.model

/**
 * @ Class Name   : HistoryModel.kt
 * @ Modification : HISTORY MODEL CLASS.
 * @
 * @ 최초 생성일      최초 생성자
 * @ ---------    ---------
 * @ 2021.06.23.    고재훈
 * @
 * @  수정일          수정자
 * @ ---------    ---------
 * @
 **/

data class HistoryModel(

    var smellRegisterNo            : String = "",                                                   // 냄새_접수_번호(SR+YYYYMMDDHHMISS+SEQ(2자리))
    var smellType                  : String = "",                                                   // 냄새 타입(코드 테이블 참고)
    var smellValue                 : String = "",                                                   // 냄새 강도(코드 테이블 참고)
    var weatherState                : String = "",                                                   // 날씨 상태(코드 테이블 참고)
    var temperatureValue           : String = "",                                                   // 온도
    var humidityValue              : String = "",                                                   // 습도
    var wind_directionValue        : String = "",                                                   // 풍향
    var wind_speedValue            : String = "",                                                   // 풍속
    var gpsX                       : String = "",                                                   // 좌표 X
    var gpsY                       : String = "",                                                   // 좌표 Y
    var smellComment               : String = "",                                                   // 냄새 설명
    var smellRegisterTime          : String = "",                                                   // 냄새 접수 시간대(코드 테이블 참조)
    var regId                      : String = "",                                                   // 사용자 아이디
    var regDt                      : String = "",                                                   // 등록 일시
    var isExpanded                 : Boolean                                                        // 열림상태

)
