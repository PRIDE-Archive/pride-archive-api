package uk.ac.ebi.pride.ws.pride.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideFileMongoService;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;

/**
 * @author ypriverol
 */

@RestController
public class FileController {

    final PrideFileMongoService mongoFileService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    @Autowired
    public FileController(PrideFileMongoService mongoFileService, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.mongoFileService = mongoFileService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }
}
