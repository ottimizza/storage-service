package br.com.ottimizza.springbotstorageservice.services;

import br.com.ottimizza.springbotstorageservice.models.Bucket;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

@Service
public class BucketService {

    private LinkedHashMap<String, Bucket> buckets = new LinkedHashMap<>();

    @Autowired
    public BucketService() {
        Yaml yml = new Yaml();
        try (InputStream inputStream = new FileInputStream(new File("conf/bucket-config.yml"))) {
            Map<String, Object> obj = yml.load(inputStream);
            ArrayList<LinkedHashMap> bucketsList = (ArrayList) obj.get("buckets");
            for (LinkedHashMap map : bucketsList) {
                for (Object k : map.keySet()) {
                    Map v = (Map) map.get((String) k);

                    Bucket bucket = new Bucket((String) v.get("name"), (String) v.get("root"));
                    this.buckets.put((String) k, bucket);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public List<Bucket> get() {
        return Arrays.asList(this.buckets.values().toArray(new Bucket[]{}));
    }

    public Bucket get(String bucketId) throws Exception {
        try {
            if (this.buckets.containsKey(bucketId)) {
                return this.buckets.get(bucketId);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new Exception(String.format("Bucket not found '%s'.", bucketId));
        }
    }

    public boolean exists(String bucketName) {
        try {
            return this.buckets.containsKey(bucketName);
        } catch (NullPointerException nullPointer) {
            return false;
        }
    }

}
