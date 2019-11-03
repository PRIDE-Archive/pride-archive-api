package uk.ac.ebi.pride.ws.pride.controllers.molecules;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.services.S3SpectralArchive;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.solr.indexes.pride.utils.StringUtils;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.SpectraResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@Slf4j
public class SpectraEvidenceController {


    S3SpectralArchive spectralArchive;
    PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public SpectraEvidenceController(S3SpectralArchive spectralArchive, PrideMoleculesMongoService moleculesMongoService){
        this.spectralArchive = spectralArchive;
        this.moleculesMongoService = moleculesMongoService;
    }

    @ApiOperation(notes = "Get an Spectrum by the specific usi", value = "spectra", nickname = "getSpectrum", tags = {"spectra"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/spectrum", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getSpectrum(@RequestParam(value = "usi", required = true) String usi){

        Optional<PSMProvider> spectrumOptional = Optional.empty();
        SpectraResourceAssembler assembler = new SpectraResourceAssembler(SpectraEvidenceController.class,
                SpectrumEvidenceResource.class);
        try {
            PSMProvider evidence = spectralArchive.readPSM(usi);
            if(evidence != null)
                spectrumOptional = Optional.of(evidence);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

        return spectrumOptional.<ResponseEntity<Object>>map( spectrum ->
                new ResponseEntity<>(assembler.toResource(spectrum), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PROTEIN_NOT_FOUND
                        + usi + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));
    }

    @ApiOperation(notes = "Get an Spectrum by the Project Accession or Assay Accession usi", value = "spectra", nickname = "getSpectrumBy", tags = {"spectra"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/spectra", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    //Todo: All the spectra retrieve methods should be done using java.util.concurrent.CompletableFuture from Spring.
    public HttpEntity<Object> getSpectrumBy(@RequestParam(value = "usi", defaultValue = "",
             required = false) List<String> usi,
                                            @RequestParam(value = "projectAccession", defaultValue = "",
                                                    required = false) String projectAccession,
                                            @RequestParam(value = "assayAccession", defaultValue = "",
                                                    required = false) String assayAccession,
                                            @RequestParam(value = "peptideSequence", defaultValue = "",
                                                    required = false) String peptideSequence,
                                            @RequestParam(value = "modifiedSequence", defaultValue = "",
                                                    required = false) String modifiedSequence,
                                            @RequestParam(value ="resultType",
                                                    defaultValue = "COMPACT")WsContastants.ResultType resultType,
                                            @RequestParam(value="page", defaultValue = "0",
                                                    required = false) int page,
                                            @RequestParam(value = "pageSize", defaultValue = "50",
                                                    required = false) int pageSize,
                                            @RequestParam(value="sortDirection", defaultValue = "DESC",
                                                    required = false) String sortDirection,
                                            @RequestParam(value="sortConditions",
                                                    defaultValue = PrideArchiveField.EXTERNAL_PROJECT_ACCESSION,
                                                    required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();

        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<PrideMongoPsmSummaryEvidence> peptides = null;
        if(usi != null && !usi.isEmpty())
            peptides = moleculesMongoService.findPsmSummaryEvidences(usi, PageRequest.of(page, pageSize, direction, sortFields.split(",")));
        else
             peptides = moleculesMongoService.findPsmSummaryEvidences(projectAccession, assayAccession, peptideSequence, modifiedSequence,
                     PageRequest.of(page, pageSize, direction, sortFields.split(",")));

        ConcurrentLinkedQueue<SpectrumEvidenceResource> psms = new ConcurrentLinkedQueue<>();
        SpectraResourceAssembler assembler = new SpectraResourceAssembler(SpectraEvidenceController.class, SpectrumEvidenceResource.class);
        peptides.getContent().parallelStream().forEach( psmEvidence ->  {
            try {
                if(resultType == WsContastants.ResultType.COMPACT)
                    psms.add(assembler.toResource(psmEvidence));
                else
                    psms.add(assembler.toResource(spectralArchive.readPSM(psmEvidence.getUsi())));
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        });


        long totalPages = peptides.getTotalPages();
        PagedResources.PageMetadata pageMetadata = new PagedResources
                .PageMetadata(pageSize, page, peptides.getTotalElements(), totalPages);

        PagedResources<SpectrumEvidenceResource> pagedResources = new PagedResources<>(psms, pageMetadata,
                linkTo(methodOn(SpectraEvidenceController.class)
                        .getSpectrumBy(usi, projectAccession, assayAccession, peptideSequence,
                                modifiedSequence, resultType, page, pageSize, sortDirection, sortFields))
                        .withSelfRel(),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(usi, projectAccession, assayAccession, peptideSequence, modifiedSequence, resultType,
                        (int) WsUtils.validatePage(page + 1, totalPages), pageSize,
                        sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(usi, projectAccession, assayAccession, peptideSequence, modifiedSequence, resultType,
                        (int) WsUtils.validatePage(page - 1, totalPages), pageSize, sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(usi, projectAccession, assayAccession, peptideSequence, modifiedSequence, resultType,0,pageSize,
                        sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(usi, projectAccession, assayAccession, peptideSequence, modifiedSequence, resultType,
                        (int) totalPages,pageSize,  sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

}
