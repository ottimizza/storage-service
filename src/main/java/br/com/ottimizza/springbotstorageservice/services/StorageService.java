package br.com.ottimizza.springbotstorageservice.services;

import br.com.ottimizza.springbotstorageservice.models.Bucket;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.inject.Inject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    @Inject
    private BucketService bucketService;

    private final Path fileStorageLocation;

    @Autowired
    public StorageService(@Value("${storage.path}") String storagePath) throws Exception {
        this.fileStorageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new Exception("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String store(MultipartFile file, String applicationId, String accountingId) {
        JSONObject response = new JSONObject();

        try {
            InputStream fileInputStream = file.getInputStream();
            String filename = UUID.randomUUID().toString() + "__" + StringUtils.cleanPath(file.getOriginalFilename());

            if (applicationId == null || applicationId.equals("")) {
                throw new Exception(String.format(
                        "Sorry! Could not find bucket for '%s'.", applicationId
                ));
            } else if (!this.bucketService.exists(applicationId)) {
                throw new Exception(String.format(
                        "Sorry! Could not find bucket for '%s'.", applicationId
                ));
            }
            if (accountingId == null || accountingId.equals("")) {
                throw new Exception(String.format(
                        "Sorry! Could not find accouting path for '%s'.", accountingId
                ));
            }
            if (filename.contains("..")) {
                throw new Exception(String.format(
                        "Sorry! Filename contains invalid path sequence '%s'.", filename
                ));
            }

            Bucket bucket = bucketService.get(applicationId);

            String pathApp = (bucket.getRoot() + "/" + bucket.getName());
            String pathAcc = accountingId;
            String pathDt = dateToString("yyyy/MM/dd");

            Path location = resolve(pathApp, pathAcc, pathDt);

            location = location.resolve(filename);

            Files.copy(fileInputStream, location, StandardCopyOption.REPLACE_EXISTING);

            String externalId = Base64.getEncoder().encodeToString(location.toString().getBytes());

            JSONObject record = new JSONObject();
            record.put("id", externalId);

            response.put("status", "success");
            response.put("record", record);

        } catch (Exception ex) {
            response.put("status", "error");
            response.put("message", ex.getMessage());
            ex.printStackTrace();
        }

        return response.toString();
    }

    public Resource loadFileAsResource(String fileName) throws Exception {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new Exception("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new Exception("File not found " + fileName, ex);
        }
    }

    private Path resolve(String pathApp, String pathAcc, String pathDt) throws Exception {
        Path resolvedPath = Paths.get(String.format(
                "%s/%s/%s/%s", this.fileStorageLocation.toString(), pathApp, pathAcc, pathDt
        )).toAbsolutePath().normalize();
        try {
            Files.createDirectories(resolvedPath);
        } catch (IOException ex) {
            throw new Exception("Could not create the directory where the uploaded files will be stored.", ex);
        }
        return resolvedPath;
    }

    private String dateToString(Date dt, String ft) { // throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(ft);
        return sdf.format(dt);
    }

    private String dateToString(String ft) {
        return dateToString(new Date(System.currentTimeMillis()), ft);
    }

}
