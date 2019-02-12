package uk.ac.ebi.pride.ws.pride.controllers.file;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.msrun.MongoPrideMSRun;
import uk.ac.ebi.pride.mongodb.archive.service.msruns.PrideMsRunMongoService;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectMSRunResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.file.MSRunMetadata;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.file.PrideMSRunResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.util.List;
import java.util.Optional;

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

    final PrideMsRunMongoService mongoMSRunService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    @Autowired
    public MSRunController(PrideFileMongoService mongoFileService, CustomPagedResourcesAssembler customPagedResourcesAssembler,PrideMsRunMongoService mongoMSRunService) {
        this.mongoFileService = mongoFileService;
        this.mongoMSRunService = mongoMSRunService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }

    /* The following end-points are related with MSRuns */

    @ApiOperation(notes = "Update MSRun metadata partly", value = "msruns", nickname = "updateMetadataParts", tags = {"msruns"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
    })
    @RequestMapping(value = "/msruns/{accession}/updateMetadataParts", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<PrideMSRunResource> updateMetadataParts(@PathVariable(value = "accession") String accession,
                                                                  @RequestParam(name = "fieldName") String fieldName,
                                                                  @RequestBody MSRunMetadata msRunMetadata

    ) {
        Optional<MongoPrideMSRun> file = mongoMSRunService.updateMSRunMetadataParts(fieldName,msRunMetadata, accession);
        ProjectMSRunResourceAssembler assembler = new ProjectMSRunResourceAssembler(MSRunController.class, PrideMSRunResource.class);
        PrideMSRunResource resource;
        if(!file.isPresent())
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);

        resource = assembler.toResource(file.get());
        return new ResponseEntity(resource, HttpStatus.OK);

    }


    @ApiOperation(notes = "Update MSRun metadata", value = "msruns", nickname = "updateMetadata", tags = {"msruns"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
    })
    @RequestMapping(value = "/msruns/{accession}/updateMetadata", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<PrideMSRunResource> updateMetadata(@PathVariable(value = "accession") String accession,
                                                            @RequestBody MSRunMetadata msRunMetadata

    ) {
        Optional<MongoPrideMSRun> file = mongoMSRunService.updateMSRunMetadata(msRunMetadata, accession);
        ProjectMSRunResourceAssembler assembler = new ProjectMSRunResourceAssembler(MSRunController.class, PrideMSRunResource.class);
        PrideMSRunResource resource;
        if(!file.isPresent())
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);

        resource = assembler.toResource(file.get());
        return new ResponseEntity(resource, HttpStatus.OK);

    }

    @ApiOperation(notes = "Get a MSRun from PRIDE database", value = "msruns", nickname = "getMSRun", tags = {"msruns"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/msruns/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<PrideMSRunResource> getMSRun(@PathVariable(value="accession") String accession) {

        Optional<MongoPrideMSRun> file = mongoMSRunService.findMSRunByAccession(accession);

        ProjectMSRunResourceAssembler assembler = new ProjectMSRunResourceAssembler(MSRunController.class, PrideMSRunResource.class);
        PrideMSRunResource resource = null;
        if(file.isPresent()){
            resource = assembler.toResource(file.get());
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return new ResponseEntity<>(resource, HttpStatus.NO_CONTENT);
    }

    @ApiOperation(notes = "Get a MSRun for an specific PRIDE Project", value = "msruns", nickname = "getMSRunByProject", tags = {"msruns"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/msruns/byProject", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<PrideMSRunResource>> getMSRunByProject(@RequestParam(value="accession") String accession) {

        List<MongoPrideMSRun> files = mongoMSRunService.getMSRunsByProject(accession);

        ProjectMSRunResourceAssembler assembler = new ProjectMSRunResourceAssembler(MSRunController.class, PrideMSRunResource.class);
        List<PrideMSRunResource> resource = null;
        if(files != null && !files.isEmpty()){
            resource = assembler.toResources(files);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return new ResponseEntity<>(resource, HttpStatus.NO_CONTENT);
    }




}
