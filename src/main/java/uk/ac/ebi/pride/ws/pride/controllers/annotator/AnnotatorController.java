package uk.ac.ebi.pride.ws.pride.controllers.annotator;

//import com.sun.security.auth.module.UnixLoginModule;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.sample.ISampleMSRunRow;
import uk.ac.ebi.pride.archive.dataprovider.sample.SampleProvider;
import uk.ac.ebi.pride.mongodb.archive.model.sample.MongoISampleMSRunRow;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.samples.PrideSampleMongoService;
import uk.ac.ebi.pride.utilities.annotator.MSRunAttributes;
import uk.ac.ebi.pride.utilities.annotator.SampleAttributes;
import uk.ac.ebi.pride.utilities.annotator.SampleClass;
import uk.ac.ebi.pride.utilities.annotator.TypeAttribute;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.config.OLSWsConfigProd;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.model.PTM;
import uk.ac.ebi.pride.utilities.pridemod.model.UniModPTM;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.Triple;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.sample.Sample;
import uk.ac.ebi.pride.ws.pride.models.sample.SampleMSRunRow;
import uk.ac.ebi.pride.ws.pride.models.sample.SampleMSRunTable;
import uk.ac.ebi.pride.ws.pride.transformers.Transformer;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.util.*;
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

    private static ModReader modReader = ModReader.getInstance();



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
    @RequestMapping(value = "/annotator/sampleAttributes", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Triple<SampleClass, TypeAttribute, CvParam>>> getSampleAttributes() {

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
    @RequestMapping(value = "/annotator/valuesByAttribute", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<CvParam>> getSampleAttributes(@RequestParam(value = "attributeAccession") String attributeAccession,
                                                             @RequestParam(value = "ontologyAccession") String ontologyAccession,
                                                             @RequestParam(value = "keyword") String keyword) {

        List<CvParam> valueAttributes = new ArrayList<>();
        if(attributeAccession.equalsIgnoreCase(CvTermReference.PRIDE_VARIABLE_MODIFICATION.getAccession()) || attributeAccession.equalsIgnoreCase(CvTermReference.PRIDE_FIXED_MODIFICATION.getAccession())){

            if(keyword == null || keyword.isEmpty())
                keyword = "";

            List<PTM> ptms;

            if(keyword.equalsIgnoreCase(""))
                ptms = modReader.getUnimodPTMs();
            else{
                ptms = modReader.getPTMListByPatternName(keyword);
                ptms.addAll(modReader.getPTMListByPatternDescription(keyword));
            }

            valueAttributes = ptms.stream().filter( x-> (x instanceof UniModPTM))
                    .map( x -> new CvParam(x.getCvLabel(), x.getAccession(), x.getName() + " " + "(mass:" + x.getMonoDeltaMass() + ")", String.valueOf(x.getMonoDeltaMass())))
                    .collect(Collectors.toList());

        }else{

            Term term =  olsClient.getTermById(new Identifier(attributeAccession, Identifier.IdentifierType.OBO), ontologyAccession);
            List<Term> terms = olsClient.getTermsByNameFromParent(keyword, term.getOntologyPrefix().toLowerCase(),false, term.getIri().getIdentifier());

            valueAttributes = terms.stream()
                    .map( x-> new CvParam(x.getOntologyName(), x.getOboId().getIdentifier(), x.getName(), null))
                    .collect(Collectors.toList());
        }


        return new ResponseEntity<>(valueAttributes, HttpStatus.OK);

    }

    @ApiOperation(notes = "Get Labeling values", value = "annotator", nickname = "getLabelingValues", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/labelingValues", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<CvParam>> getLabelingValues(@RequestParam(value = "keyword", required = false) String keyword) {

        List<CvParam> valueAttributes = new ArrayList<>();
        valueAttributes.add( new CvParam(CvTermReference.MS_LABEL_FREE_SAMPLE.getCvLabel(), CvTermReference.MS_LABEL_FREE_SAMPLE.getAccession(), CvTermReference.MS_LABEL_FREE_SAMPLE.getName(), null));

        List<Term> terms =  olsClient.getTermChildren(new Identifier(CvTermReference.MS_LABELING_MSRUN.getAccession(), Identifier.IdentifierType.OBO),
                CvTermReference.MS_LABELING_MSRUN.getCvLabel().toLowerCase(), 3);

        if(keyword !=null && !keyword.isEmpty())
            terms = terms.stream().filter(x -> x.getName().toLowerCase().contains(keyword.trim().toLowerCase())).collect(Collectors.toList());

        valueAttributes.addAll(terms.stream().map( x-> new CvParam(x.getOntologyName(), x.getOboId().getIdentifier(), x.getName(), null))
                .collect(Collectors.toList()));

        return new ResponseEntity<>(valueAttributes, HttpStatus.OK);

    }

    /**
     * It would be great if this function read from UNIMOD OLS. However, the current implementation needs to read from UNIMOD directly.
     *
     * @param keyword Keyword to search reagent
     * @return List of Terms
     */
    @ApiOperation(notes = "Get Reagent Values", value = "annotator", nickname = "reagentValues", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/reagentValues", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<CvParam>> getReagentValues(@RequestParam(value = "keyword", required = false) String keyword) {
        if(keyword == null || keyword.isEmpty())
            keyword = "";

        List<PTM> terms;
        if(keyword.equalsIgnoreCase(""))
            terms = modReader.getUnimodPTMs();
        else{
            terms = modReader.getPTMListByPatternName(keyword);
            terms.addAll(modReader.getPTMListByPatternDescription(keyword));
        }

        List<CvParam> valueAttributes = terms.stream().filter(x-> (x instanceof UniModPTM))
                .filter( x-> (((UniModPTM)x).getClassifications().contains("isotopic label") || ((UniModPTM)x).getClassifications().contains("multiple")))
                .map( x -> new CvParam(x.getCvLabel(), x.getAccession(), x.getName() + " " + "(mass:" + x.getMonoDeltaMass() + ")", String.valueOf(x.getMonoDeltaMass())))
                .collect(Collectors.toList());

        return new ResponseEntity<>(valueAttributes, HttpStatus.OK);

    }

    @ApiOperation(notes = "Get the default values for each property", value = "annotator", nickname = "getDefaultValuesByProperty", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/defaultValuesByProperty", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Tuple<CvParam, CvParam>>> getDefaultValuesByProperty() {

        List<Tuple<CvParam, CvParam>> defaultValues = new ArrayList<>();
        for(SampleAttributes sampleAttribute: SampleAttributes.values()){
            if(sampleAttribute.getDefaultValue() != null){
                defaultValues.add(new Tuple<>(new CvParam(sampleAttribute.getEfoTerm().getCvLabel(), sampleAttribute.getEfoTerm().getAccession(), sampleAttribute.getEfoTerm().getName(), null), new CvParam(sampleAttribute.getDefaultValue().getCvLabel(), sampleAttribute.getDefaultValue().getAccession(),
                        sampleAttribute.getDefaultValue().getName(), null)));
            }
        }

        return new ResponseEntity<>(defaultValues, HttpStatus.OK);

    }

    @ApiOperation(notes = "Get Default Characteristics for Sample - MSRun  ", value = "annotator", nickname = "getSampleMSRunAttributes", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/defaultSampleMSRunAttributes", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Tuple<CvParam, CvParam>>> getSampleMSRunAttributes() {

        List<Tuple<CvParam, CvParam>> listAttributes = new ArrayList<>();
        for(MSRunAttributes attribute: MSRunAttributes.values()){
            CvParam key = new CvParam(attribute.getCvTerm().getCvLabel(), attribute.getCvTerm().getAccession(), attribute.getCvTerm().getName(), null);
            CvParam defaultValue = null;
            if(attribute.getDefaultTerm() != null){
                defaultValue = new CvParam(attribute.getDefaultTerm().getCvLabel(), attribute.getDefaultTerm().getAccession(), attribute.getDefaultTerm().getName(), null);
            }
            listAttributes.add(new Tuple<>(key, defaultValue));
        }
        return new ResponseEntity<>(listAttributes, HttpStatus.OK);

    }

    @ApiOperation(notes = "Get Samples - MSRun Table", value = "annotator", nickname = "getSampleMSRuns", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/{accession}/sampleMsRuns", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SampleMSRunRow>> getSampleMSRuns(@PathVariable( value = "accession") String accession) {

        List<MongoISampleMSRunRow> mongoSamples = sampleMongoService.getSamplesMRunProjectAccession(accession);
        if(mongoSamples != null){

            List<SampleMSRunRow> samples = mongoSamples.stream()
                    .map(Transformer::transformSampleMSrun)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(samples, HttpStatus.OK);
        }
        return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.NO_CONTENT);

    }

    @ApiOperation(notes = "Update Sample - MSRun Table", value = "annotator", nickname = "updateSampleMSRuns", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/{accession}/updateSampleMsRuns", method = RequestMethod.PUT, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SampleMSRunRow>> updateSampleMSRuns(@PathVariable( value = "accession") String accession,
                                                                   @RequestBody SampleMSRunTable sampleMSRunTable) {

        List<SampleMSRunRow> sampleMSRuns = sampleMSRunTable.getSampleMSRunRows();

        Collection<? extends ISampleMSRunRow> mongoSamples = sampleMongoService.updateSamplesMRunProjectAccession(accession, sampleMSRuns);
        if(mongoSamples != null){

            sampleMSRuns = mongoSamples.stream()
                    .map(Transformer::transformSampleMSrun).collect(Collectors.toList());

            return new ResponseEntity<>(sampleMSRuns, HttpStatus.OK);
        }


        return new ResponseEntity<>(sampleMSRuns, HttpStatus.ACCEPTED);

    }

    @ApiOperation(notes = "Get Samples for Project Accession", value = "annotator", nickname = "getSamples", tags = {"annotator"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/annotator/{accession}/samples", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Sample>> getSamples(@PathVariable( value = "accession") String accession) {

        Collection<? extends SampleProvider> mongoSamples = sampleMongoService.getSamplesByProjectAccession(accession);
        if(mongoSamples != null){

            List<Sample> samples = mongoSamples.stream().map(Transformer::transformSample).collect(Collectors.toList());

            return new ResponseEntity<>(samples, HttpStatus.OK);
        }

        return new ResponseEntity<>(Collections.EMPTY_LIST, HttpStatus.NO_CONTENT);

    }


}
