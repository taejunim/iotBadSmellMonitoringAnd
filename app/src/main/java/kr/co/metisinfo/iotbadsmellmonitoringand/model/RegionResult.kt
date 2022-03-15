package kr.co.metisinfo.iotbadsmellmonitoringand.model

data class RegionResult (
    val result : String,
    val data : Data
)
data class Data (
    val region : Master
)
data class Master(
    val master : List<RegionMaster>
)
data class RegionMaster(
    val mCodeId : String,
    val mCodeIdName : String,
    val detail : List<RegionDetail>
)
data class RegionDetail(
    val dCodeId : String,
    val dCodeIdName : String
)
