package io.micronaut.gcp.pubsub;

public class DataHolder {

    private static DataHolder instance;

    private DataHolder() { }

    private Object data;

    synchronized public static DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
