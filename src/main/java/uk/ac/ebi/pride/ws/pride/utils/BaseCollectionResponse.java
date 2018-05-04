package uk.ac.ebi.pride.ws.pride.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Collection;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 04/05/2018.
 */
public abstract class BaseCollectionResponse<T extends Collection<?>> {
    private String root;
    private T collection;

    protected BaseCollectionResponse(String rootName, T aCollection) {
        this.root = rootName;
        this.collection = aCollection;
    }

    public T getCollection() {
        return collection;
    }

    public String serialize() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer().withRootName(root);
        String result = null;
        try {
            result = writer.writeValueAsString(collection);
        } catch (JsonProcessingException e) {
            result = e.getMessage();
        }
        return result;
    }
}