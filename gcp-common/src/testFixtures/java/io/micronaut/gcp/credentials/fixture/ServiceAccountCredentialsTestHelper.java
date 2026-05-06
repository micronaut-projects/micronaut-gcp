package io.micronaut.gcp.credentials.fixture;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import io.micronaut.core.annotation.Internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Base64;

/**
 * An internal test fixture for generating mock service account credentials for tests.
 *
 * @author Jeremy Grelle
 * @since 5.2.0
 */
@Internal
public class ServiceAccountCredentialsTestHelper {

    public static PrivateKey generatePrivateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        return kp.getPrivate();
    }

    public static File writeServiceCredentialsToTempFile(PrivateKey pk) throws IOException {
        return writeServiceCredentialsToTempFile(pk, null);
    }

    public static File writeServiceCredentialsToTempFile(PrivateKey pk, String tokenUri) throws IOException {
        File tmpSACredentials = File.createTempFile("GoogleCredentialsFactorySpec-", ".json");
        tmpSACredentials.deleteOnExit();
        GenericJson fileContents = buildServiceAccountJson(pk, tokenUri);
        writeJsonToOutputStream(new FileOutputStream(tmpSACredentials), fileContents);
        return tmpSACredentials;
    }

    public static String encodeServiceCredentials(PrivateKey pk) throws IOException {
        return encodeServiceCredentials(pk, null);
    }

    public static String encodeServiceCredentials(PrivateKey pk, String tokenUri) throws IOException {
        GenericJson serviceAccountCredentials = buildServiceAccountJson(pk, tokenUri);
        ByteArrayOutputStream serviceAccountCredentialsByteStream = new ByteArrayOutputStream();
        writeJsonToOutputStream(serviceAccountCredentialsByteStream, serviceAccountCredentials);
        return Base64.getEncoder().encodeToString(serviceAccountCredentialsByteStream.toByteArray());
    }

    public static void writeJsonToOutputStream(OutputStream outputStream, GenericJson jsonContent) throws IOException {
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        try (JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, StandardCharsets.UTF_8)) {
            jsonGenerator.serialize(jsonContent);
        }
    }

    public static GenericJson buildServiceAccountJson(PrivateKey pk) throws IOException {
        return buildServiceAccountJson(pk, null);
    }

    public static GenericJson buildServiceAccountJson(PrivateKey pk, String tokenUri) throws IOException {
        String jsonPrivateKey = """
            -----BEGIN PRIVATE KEY-----
            %s
            -----END PRIVATE KEY-----
            """.formatted(Base64.getEncoder().encodeToString(pk.getEncoded()));

        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        try (InputStream templateKeyFile = ServiceAccountCredentialsTestHelper.class.getResourceAsStream("/sa-fake-private-key.json")) {
            JsonObjectParser parser = new JsonObjectParser(jsonFactory);
            GenericJson fileContents =
                parser.parseAndClose(templateKeyFile, StandardCharsets.UTF_8, GenericJson.class);
            fileContents.set("private_key", jsonPrivateKey);
            if (tokenUri != null) {
                fileContents.set("token_uri", tokenUri);
            }
            return fileContents;
        }
    }
}
