package uk.ac.ebi.pride.ws.pride.controllers.annotator;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.utilities.annotator.SampleAttributes;
import uk.ac.ebi.pride.utilities.annotator.SampleClass;
import uk.ac.ebi.pride.utilities.annotator.SupportedOntologies;
import uk.ac.ebi.pride.utilities.annotator.TypeAttribute;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.pride.utilities.util.Triple;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 26/10/2018.
 */

@RestController
public class AnnotatorController {


    private final PrideFileMongoService mongoFileService;
    private final CustomPagedResourcesAssembler customPagedResourcesAssembler;
    private static OLSClient olsClient = new OLSClient(new OLSWsConfigProd());

    @Autowired
    public AnnotatorController(PrideFileMongoService mongoFileService, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.mongoFileService = mongoFileService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }

    @ApiOperation(notes = "Get Characteristics for Sample ", value = "annotator", nickname = "getSampleAttributes", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/getSampleAttributes", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Triple<SampleClass, TypeAttribute, CvParam>>> getSampleAttributes(@RequestParam(value = "accession", required = false) String accession) {

        List<Triple<SampleClass, TypeAttribute, CvParam>> listAttributes = new ArrayList<>();

        for(SampleAttributes attributeCV: SampleAttributes.values()){
            CvParam param = new CvParam(attributeCV.getEfoTerm().getCvLabel(), attributeCV.getEfoTerm().getAccession(),attributeCV.getEfoTerm().getName(), null);
            if(attributeCV.getRequiredSampleClasses() != null){
                for(SampleClass requiredClass: attributeCV.getRequiredSampleClasses())
                    listAttributes.add(new Triple<>(requiredClass, TypeAttribute.REQUIRED, param));
            }
            if(attributeCV.getOptionalSampleClasses() != null){
                for(SampleClass requiredClass: attributeCV.getOptionalSampleClasses())
                    listAttributes.add(new Triple<>(requiredClass, TypeAttribute.OPTIONAL, param));
            }
        }

        return new ResponseEntity<>(listAttributes, HttpStatus.OK);

    }


    @ApiOperation(notes = "Get Values by Sample Attribute ", value = "annotator", nickname = "getValuesByAttribute", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/getValuesByAttribute", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<CvParam>> getSampleAttributes(@RequestParam(value = "attributeAccession", required = false) String attributeAccession,
                                                             @RequestParam(value = "ontologyAccession" , required = true) String ontologyAccession,
                                                             @RequestParam(value = "keyword", required = true) String keyword) {

        List<CvParam> valueAttributes = new ArrayList<>();
        Term term =  olsClient.getTermById(new Identifier(attributeAccession, Identifier.IdentifierType.OBO), ontologyAccession);
        List<Term> terms = olsClient.getTermsByNameFromParent(keyword, term.getOntologyPrefix().toLowerCase(),false, term.getIri().getIdentifier());

        valueAttributes = terms.stream()
                .map( x-> new CvParam(x.getOntologyName(), x.getOboId().getIdentifier(), x.getName(), null))
                .collect(Collectors.toList());


        return new ResponseEntity<>(valueAttributes, HttpStatus.OK);

    }

}
