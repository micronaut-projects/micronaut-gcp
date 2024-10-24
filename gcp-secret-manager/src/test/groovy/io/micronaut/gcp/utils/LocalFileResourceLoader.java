package io.micronaut.gcp.utils;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.protobuf.ByteString;
import io.micronaut.context.env.yaml.YamlPropertySourceLoader;
import io.micronaut.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocalFileResourceLoader {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileResourceLoader.class);

    public static String loadSecret(String projectId, String name) throws IOException {
        String path = String.format("projects/%s/secrets/%s", projectId, name);
        logger.debug("Resolving file at " + path);
        InputStream in = LocalFileResourceLoader.class.getClassLoader().getResourceAsStream(path);
        return IOUtils.readText(new BufferedReader(new InputStreamReader(in)));
    }

    public static String loadLocationSecret(String projectId, String location, String name) throws IOException {
        String path = String.format("projects/%s/locations/%s/secrets/%s", projectId, location, name);
        logger.debug("Resolving file at " + path);
        InputStream in = LocalFileResourceLoader.class.getClassLoader().getResourceAsStream(path);
        return IOUtils.readText(new BufferedReader(new InputStreamReader(in)));
    }


    public static void main(String[] args) {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        YamlPropertySourceLoader locationLoader = new YamlPropertySourceLoader();
        try {
            String contents = LocalFileResourceLoader.loadSecret("first-gcp-project", "application");
            AccessSecretVersionResponse response = AccessSecretVersionResponse.newBuilder()
                    .setName("foo")
                    .setPayload(SecretPayload.newBuilder().setData(ByteString.copyFrom(contents.getBytes())).build())
                    .build();

            loader.read("foo", contents.getBytes());
            loader.read("foo", response.getPayload().getData().toByteArray());

            String locationContents = LocalFileResourceLoader.loadLocationSecret("first-gcp-project", "us-central1","application");
            AccessSecretVersionResponse locationResponse = AccessSecretVersionResponse.newBuilder()
                .setName("foo")
                .setPayload(SecretPayload.newBuilder().setData(ByteString.copyFrom(contents.getBytes())).build())
                .build();

            locationLoader.read("foo", locationContents.getBytes());
            locationLoader.read("foo", locationResponse.getPayload().getData().toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
