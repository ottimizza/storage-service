package br.com.ottimizza.springbotstorageservice.controllers;

import br.com.ottimizza.springbotstorageservice.services.BucketService;
import br.com.ottimizza.springbotstorageservice.services.StorageService;
import java.io.IOException;
import java.util.Base64;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Lucas Martins (dev.lucasmartins@gmail.com)
 */
@RestController
@RequestMapping("/storage")
public class StorageController {

    @Inject
    private StorageService storageService;

    @Inject
    private BucketService bucketService;

    @GetMapping("/")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("");
    }

    @PostMapping(
            path = "/{application_id}/accounting/{accounting_id}/store",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> upload(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("application_id") String applicationId,
            @PathVariable("accounting_id") String accountingId,
            @RequestParam("file") MultipartFile file) throws Exception {

        String response = "";

        if (this.bucketService.authorize(bucketService.get(applicationId), authorization)) {
            response = storageService.store(file, applicationId, accountingId);
        } else {
            JSONObject json = new JSONObject();
            json.put("status", "error");
            json.put("error", "invalid_token");
            json.put("message", "Token was not recognised");
            response = json.toString();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{resource_id}")
    public ResponseEntity<Resource> getResource(@PathVariable("resource_id") String externalId, HttpServletRequest request)
            throws Exception {
        Resource resource = storageService.loadFileAsResource(
                new String(Base64.getDecoder().decode(externalId)));

        String contentDisposition = getContentDisposition(resource);
        String contentType = getContentType(resource, request);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    @GetMapping("/{resource_id}/download")
    public ResponseEntity<Resource> downloadResource(@PathVariable("resource_id") String externalId, HttpServletRequest request)
            throws Exception {
        Resource resource = storageService.loadFileAsResource(
                new String(Base64.getDecoder().decode(externalId)));

        String contentDisposition = getContentDisposition(resource, "attachment");
        String contentType = getContentType(resource, request);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    //      
    public String getContentType(Resource resource, HttpServletRequest request) throws Exception {
        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        return (contentType == null) ? "application/octet-stream" : contentType;
    }

    public String getContentDisposition(Resource resource, String contentDisposition) throws Exception {
        return String.format("%s;filename=\"%s\"", contentDisposition, resource.getFilename());
    }

    public String getContentDisposition(Resource resource) throws Exception {
        return getContentDisposition(resource, "inline");
    }
}
