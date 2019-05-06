package br.com.ottimizza.springbotstorageservice.controllers;

import br.com.ottimizza.springbotstorageservice.models.Bucket;
import br.com.ottimizza.springbotstorageservice.services.BucketService;
import java.util.List;
import javax.inject.Inject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Lucas Martins (dev.lucasmartins@gmail.com)
 */
@RestController
@RequestMapping("/buckets")
public class BucketController {

    @Inject
    private BucketService bucketService;

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public ResponseEntity<List<Bucket>> index() {
        return ResponseEntity.ok(bucketService.get());
    }

    @RequestMapping(value = {"/exists/{bucket_id}"}, method = RequestMethod.GET)
    public ResponseEntity<String> index(@PathVariable("bucket_id") String bucketId) {
        return ResponseEntity.ok(String.valueOf(bucketService.exists(bucketId)));
    }

}
