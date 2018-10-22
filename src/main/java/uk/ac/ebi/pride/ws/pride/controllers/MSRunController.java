package uk.ac.ebi.pride.ws.pride.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 22/10/2018.
 */

@RestController
public class MSRunController {

    final PrideFileMongoService mongoFileService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    @Autowired
    public MSRunController(PrideFileMongoService mongoFileService, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.mongoFileService = mongoFileService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }




}
