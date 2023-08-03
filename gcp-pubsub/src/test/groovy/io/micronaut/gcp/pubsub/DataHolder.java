package io.micronaut.gcp.pubsub;

public class DataHolder {

    private static DataHolder instance;

    private DataHolder() { }

    private Object data;
    private String projectId;

    synchronized public static DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public Object getData() {
        return data;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public DataHolder setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }
}
