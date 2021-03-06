package br.com.ottimizza.springbotstorageservice.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
public class Bucket implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String root;

    @Getter
    @Setter
    private String authEndpoint;

    @Getter
    @Setter
    Map<String, String> authHeaders;
}
