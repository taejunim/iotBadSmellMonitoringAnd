package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class HistoryResult (
    val result: String,
    val data: MutableList<HistoryModel>
)