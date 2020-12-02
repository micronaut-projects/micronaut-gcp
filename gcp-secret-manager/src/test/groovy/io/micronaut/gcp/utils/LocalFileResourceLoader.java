package io.micronaut.gcp.utils;

import io.micronaut.core.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocalFileResourceLoader {

    public static String loadSecret(String projectId, String name) throws IOException {
        String path = String.format("projects/%s/secrets/%s", projectId, name);
        InputStream in = LocalFileResourceLoader.class.getClassLoader().getResourceAsStream(path);
        return IOUtils.readText(new BufferedReader(new InputStreamReader(in)));
    }

}
