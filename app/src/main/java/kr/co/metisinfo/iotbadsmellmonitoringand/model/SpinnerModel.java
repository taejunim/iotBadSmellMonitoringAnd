package kr.co.metisinfo.iotbadsmellmonitoringand.model;

public class SpinnerModel {
    public String key;       // key가 seq를 의미
    public String value;

    public SpinnerModel(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return value;
    }
}
