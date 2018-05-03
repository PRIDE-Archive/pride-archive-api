package uk.ac.ebi.pride.ws.pride.mappers;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;
import uk.ac.ebi.pride.ws.pride.models.dataset.ResourceDataset;

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
 * Created by ypriverol (ypriverol@gmail.com) on 03/05/2018.
 */
public class DatasetResourceMapper extends ResourceAssemblerSupport<PrideSolrDataset, ResourceDataset> {


    public DatasetResourceMapper(Class<?> controllerClass, Class<ResourceDataset> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public ResourceDataset toResource(PrideSolrDataset prideSolrDataset) {
        return null;
    }
}
