package edu.tcu.cs.hogwartsartifactsonline.client.imagestorage;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FirebaseImageStorageClient implements ImageStorageClient {

    private static final String BUCKET_NAME = "hogwarts-b677f.appspot.com";

    public FirebaseImageStorageClient() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("./src/main/resources/serviceAccountKey.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket(BUCKET_NAME)
                .build();

        if (FirebaseApp.getApps().isEmpty()) { // Avoid initializing multiple times
            FirebaseApp.initializeApp(options);
        }
    }

    @Override
    public String uploadImage(String containerName, String originalImageName, InputStream data, long length) throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Bucket bucket = storage.get(BUCKET_NAME);

        // Rename the image file to a unique name
        String newImageName = UUID.randomUUID() + originalImageName.substring(originalImageName.lastIndexOf("."));

        Blob blob = bucket.create(newImageName, data, "image/*");

        return blob.getMediaLink(); // Returns the public download URL
    }
}
