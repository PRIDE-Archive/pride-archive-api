package uk.ac.ebi.pride.ws.pride.models.file;

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
 *
 * @author ypriverol on 24/10/2018.
 */
public class PrideMSRunResource extends Resource<PrideMSRun> {

    /**
     * Default constructor for Pride File including hateoas links.
     * @param content Object that would be represented
     * @param links links.
     */
    public PrideMSRunResource(PrideMSRun content, Iterable<Link> links) {
        super(content, links);

    }
}
