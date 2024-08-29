package edu.tcu.cs.hogwartsartifactsonline.client.imagestorage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

@Service
public class FirebaseImageStorageClient implements ImageStorageClient {

    private static final String BUCKET_NAME = "hogwarts-b677f.appspot.com";

    @Value("${firebase.storage.url}")
    private String storageUrl;

    private final RestClient.Builder restClientBuilder;

    public FirebaseImageStorageClient(RestClient.Builder restClientBuilde) throws IOException {
        this.restClientBuilder = restClientBuilde;

        // Configure the Firebase Cloud credentials
        FileInputStream serviceAccount = new FileInputStream("./src/main/resources/serviceAccountKey.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket(BUCKET_NAME)
                .build();

        // Avoid initializing multiple times
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }

    @Override
    public String uploadImage(String containerName, String originalImageName, InputStream data, long length) throws IOException {
        // Get the default cloud bucket
        Bucket bucket = StorageClient.getInstance().bucket();

        // Rename the image file to a unique name
        String newImageName = UUID.randomUUID() + "." + StringUtils.getFilenameExtension(originalImageName);

        // Upload the image file to the firebase storage bucket
        bucket.create(newImageName, data, "image/jpeg");

        return getImageUrl(newImageName);
    }

    private String getImageUrl(String imageName) {

        String baseUrl = storageUrl + "/" + imageName;

        RestClient restClient = restClientBuilder
                .baseUrl(baseUrl).build();
        String token = Objects.requireNonNull(restClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DownloadToken.class)).downloadTokens;
        return baseUrl + "?alt=media&token=" + token;
    }

    private record DownloadToken(String downloadTokens){}
}
