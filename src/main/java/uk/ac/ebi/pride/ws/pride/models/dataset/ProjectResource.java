package uk.ac.ebi.pride.ws.pride.models.dataset;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

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
 * Created by ypriverol (ypriverol@gmail.com) on 25/05/2018.
 */
public class ProjectResource extends Resource<PrideProject> {

    /**
     * Default constructor for Resource Dataset including hateoas links.
     * @param content Object that would be represented
     * @param links links.
     */
    public ProjectResource(PrideProject content, Iterable<Link> links) {
        super(content, links);

    }
}