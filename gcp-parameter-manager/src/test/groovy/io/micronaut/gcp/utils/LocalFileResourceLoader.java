package io.micronaut.gcp.utils;

import io.micronaut.core.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocalFileResourceLoader {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileResourceLoader.class);

    public static String loadParameter(String projectId, String name, String version)
        throws IOException {
        String path =
            String.format("projects/%s/locations/global/parameters/%s/versions/%s", projectId, name,
                version);
        logger.debug("Resolving file at " + path);
        InputStream in = LocalFileResourceLoader.class.getClassLoader().getResourceAsStream(path);
        return IOUtils.readText(new BufferedReader(new InputStreamReader(in)));
    }

    public static String loadRegionalParameter(String projectId, String location, String name,
                                               String version) throws IOException {
        String path =
            String.format("projects/%s/locations/%s/parameters/%s/versions/%s", projectId, location,
                name, version);
        logger.debug("Resolving file at " + path);
        InputStream in = LocalFileResourceLoader.class.getClassLoader().getResourceAsStream(path);
        return IOUtils.readText(new BufferedReader(new InputStreamReader(in)));
    }
}
