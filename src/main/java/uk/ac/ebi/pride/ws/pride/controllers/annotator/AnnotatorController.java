package uk.ac.ebi.pride.ws.pride.controllers.annotator;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.msrun.MsRunProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.sample.SampleMSRunTuple;
import uk.ac.ebi.pride.archive.dataprovider.sample.SampleProvider;
import uk.ac.ebi.pride.mongodb.archive.model.sample.MongoPrideSample;
import uk.ac.ebi.pride.mongodb.archive.repo.samples.PrideMongoSampleRepository;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.samples.PrideSampleMongoService;
import uk.ac.ebi.pride.utilities.annotator.SampleAttributes;
import uk.ac.ebi.pride.utilities.annotator.SampleClass;
import uk.ac.ebi.pride.utilities.annotator.TypeAttribute;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.Triple;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.file.PrideMSRun;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;
import uk.ac.ebi.pride.ws.pride.models.sample.Sample;
import uk.ac.ebi.pride.ws.pride.models.sample.SampleMSRun;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private final PrideSampleMongoService sampleMongoService;
    private final CustomPagedResourcesAssembler customPagedResourcesAssembler;
    private static OLSClient olsClient = new OLSClient(new OLSWsConfigProd());

    @Autowired
    public AnnotatorController(PrideFileMongoService mongoFileService, PrideSampleMongoService sampleMongoService, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.mongoFileService = mongoFileService;
        this.sampleMongoService = sampleMongoService;
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

        List<CvParam> valueAttributes;
        Term term =  olsClient.getTermById(new Identifier(attributeAccession, Identifier.IdentifierType.OBO), ontologyAccession);
        List<Term> terms = olsClient.getTermsByNameFromParent(keyword, term.getOntologyPrefix().toLowerCase(),false, term.getIri().getIdentifier());

        valueAttributes = terms.stream()
                .map( x-> new CvParam(x.getOntologyName(), x.getOboId().getIdentifier(), x.getName(), null))
                .collect(Collectors.toList());


        return new ResponseEntity<>(valueAttributes, HttpStatus.OK);

    }

    @ApiOperation(notes = "Get Labeling values", value = "annotator", nickname = "getLabelingValues", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/getLabelingValues", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<CvParam>> getLabelingValues() {

        List<CvParam> valueAttributes = new ArrayList<>();
        valueAttributes.add( new CvParam(CvTermReference.MS_LABEL_FREE_SAMPLE.getCvLabel(), CvTermReference.MS_LABEL_FREE_SAMPLE.getAccession(), CvTermReference.MS_LABEL_FREE_SAMPLE.getName(), null));

        List<Term> terms =  olsClient.getTermChildren(new Identifier("MS:1002602", Identifier.IdentifierType.OBO), "ms", 3);
        valueAttributes.addAll(terms.stream()
                .map( x-> new CvParam(x.getOntologyName(), x.getOboId().getIdentifier(), x.getName(), null))
                .collect(Collectors.toList()));


        return new ResponseEntity<>(valueAttributes, HttpStatus.OK);

    }

    @ApiOperation(notes = "Get Samples for Project Accession", value = "annotator", nickname = "getSamples", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/{accession}/samples", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Sample>> getSamples(@PathVariable( value = "accession") String accession) {

        List<MongoPrideSample> mongoSamples = sampleMongoService.getSamplesByProjectAccession(accession);
        if(mongoSamples != null){

            List<Sample> samples = mongoSamples.stream().map( x-> Sample.builder().accession((String) x.getAccession())
                    .sampleProperties(x.getSampleProperties())
                    .build()).collect(Collectors.toList());

            return new ResponseEntity<>(samples, HttpStatus.OK);
        }


        return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.NO_CONTENT);

    }

    @ApiOperation(notes = "Get Samples - MSRun Table", value = "annotator", nickname = "getSampleMSRuns", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/{accession}/sampleMsRuns", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SampleMSRun>> getSampleMSRuns(@PathVariable( value = "accession") String accession) {

        Collection<? extends SampleMSRunTuple> mongoSamples = sampleMongoService.getSamplesMRunProjectAccession(accession);
        if(mongoSamples != null){

            List<SampleMSRun> samples = mongoSamples.stream()
                    .map( x ->{
                        SampleMSRunTuple sampleMSRun = (SampleMSRunTuple) x;
                        CvParamProvider fractionMongo = sampleMSRun.getFractionIdentifier();
                        CvParamProvider labelMongo = sampleMSRun.getSampleLabel();
                        CvParamProvider technicalRep = sampleMSRun.getTechnicalReplicateIdentifier();

                        // Capture the Fraction information
                        CvParam fraction = null;
                        if(fractionMongo != null)
                            fraction = new CvParam(fractionMongo.getCvLabel(), fractionMongo.getAccession(),fractionMongo.getName(), fractionMongo.getValue());

                        //Capture the Labeling
                        CvParam label = null;
                        if(labelMongo != null)
                            label = new CvParam(labelMongo.getCvLabel(), labelMongo.getAccession(),labelMongo.getName(), labelMongo.getValue());

                        //Capture the Labeling
                        CvParam rep = null;
                        if(technicalRep != null)
                            rep = new CvParam(technicalRep.getCvLabel(), technicalRep.getAccession(),technicalRep.getName(), technicalRep.getValue());

                        return SampleMSRun.builder()
                                .sampleAccession((String) sampleMSRun.getKey())
                                .msRunAccession((String) sampleMSRun.getValue())
                                .fractionIdentifier(fraction)
                                .sampleLabel(label)
                                .technicalReplicateIdentifier(rep)
                                .msRunAccession((String) x.getValue())
                                .additionalProperies((List<Tuple<CvParam, CvParam>>) ((SampleMSRunTuple) x).getAdditionalProperties())
                                .build();
                    } ).collect(Collectors.toList());

            return new ResponseEntity<>(samples, HttpStatus.OK);
        }


        return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.NO_CONTENT);

    }




}
