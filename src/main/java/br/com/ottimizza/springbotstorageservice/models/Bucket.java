package br.com.ottimizza.springbotstorageservice.models;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author Lucas Martins (dev.lucasmartins@gmail.com)
 */
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

}
