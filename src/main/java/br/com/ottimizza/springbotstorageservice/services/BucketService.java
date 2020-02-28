package br.com.ottimizza.springbotstorageservice.services;

import br.com.ottimizza.springbotstorageservice.models.Bucket;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

            // get object containing arrays...
            ArrayList<LinkedHashMap> bucketsList = (ArrayList) obj.get("buckets");

            // iterates array..
            for (LinkedHashMap map : bucketsList) {
                for (Object k : map.keySet()) {
                    Map v = (Map) map.get((String) k);

                    // bucket properties.
                    String bucketName = (String) v.get("name");
                    String bucketRoot = (String) v.get("root");
                    String bucketAuthEndpoint = (String) v.get("auth_endpoint");
                    Map<String, String> bucketAuthHeaders = getAuthHeaders(v);

                    Bucket bucket = new Bucket(bucketName, bucketRoot, bucketAuthEndpoint, bucketAuthHeaders);
                    this.buckets.put((String) k, bucket);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getAuthHeaders(Map ymlObject) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (ymlObject.get("auth_headers") != null) {
            ArrayList<LinkedHashMap<String, Map<String, String>>> bucketAuthHeaders = (ArrayList<LinkedHashMap<String, Map<String, String>>>) ymlObject.get("auth_headers");
            for (LinkedHashMap<String, Map<String, String>> keys : bucketAuthHeaders) {
                for (String key : keys.keySet()) {
                    Map<String, String> header = keys.get(key);
                    headers.put(header.get("key"), header.get("value"));
                }
            }
        }
        return headers;
    }

    public List<Bucket> get() {
        return Arrays.asList(this.buckets.values().toArray(new Bucket[]{}));
    }

    public boolean authorize(Bucket bucket, String authorization) {
        String accessToken = removePrefixesFromAuthorizationHeader(authorization);
        final String AUTH_ENDPOINT = String.format(bucket.getAuthEndpoint() + "?token=%s", accessToken);

        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(AUTH_ENDPOINT);


            if (bucket.getAuthHeaders() != null) {
                System.out.println();
                System.out.println(String.format("*** Headers ***"));
                for (Map.Entry<String, String> header : bucket.getAuthHeaders().entrySet()) {
                    System.out.println(String.format("%s: %s", header.getKey(), header.getValue()));
                    httpGet.setHeader(header.getKey(), header.getValue());
                }
            }
            

            // Corpo do Request.
            // httpGet.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
            // httpGet.setHeader("Content-type", MediaType.APPLICATION_JSON_VALUE);
            // httpGet.setHeader("Authorization", String.format("Basic %s", authorization));

            // Response
            HttpResponse httpResponse = httpClient.execute(httpGet);

            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity responseEntity = httpResponse.getEntity();
            String responseBody = EntityUtils.toString(responseEntity, "UTF-8");

            System.out.println();
            System.out.println(String.format("*** Response ***"));
            System.out.println(String.format("URL           -> %s", String.valueOf(AUTH_ENDPOINT)));
            System.out.println();
            System.out.println(String.format("*** Response ***"));
            System.out.println(String.format("Status        -> %s", String.valueOf(statusCode)));
            System.out.println(String.format("Body          -> %s", responseBody));

            if (statusCode == 200) {
                System.out.println("200");
                return true;
            } else if (statusCode == 401) {
                System.out.println("401");
                return false;
            }

        } catch (IOException | ParseException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
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


    private String removePrefixesFromAuthorizationHeader(String authorizationHeader) {
        String authorization = authorizationHeader.replaceAll("Bearer ", "");

        return authorization;
    }
}
