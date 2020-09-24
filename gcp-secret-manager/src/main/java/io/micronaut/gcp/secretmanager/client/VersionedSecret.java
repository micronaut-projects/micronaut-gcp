package io.micronaut.gcp.secretmanager.client;

import java.util.Map;

/**
 * A wrapper class around {@link com.google.cloud.secretmanager.v1.Secret} and {@link com.google.cloud.secretmanager.v1.AccessSecretVersionResponse} that contains the secret's content, version and its labels.
 *
 * @author Vinicius Carvalho
 * @since 3.2.0
 */
public class VersionedSecret {

    private final String name;
    private final byte[] contents;
    private final String version;
    private final Map<String, String> labels;

    public VersionedSecret(String name, byte[] contents, String version, Map<String, String> labels) {
        this.name = name;
        this.contents = contents;
        this.version = version;
        this.labels = labels;
    }

    /**
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return contents
     */
    public byte[] getContents() {
        return contents;
    }

    /**
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @return labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }
}
