package uk.ac.ebi.pride.ws.pride.controllers.molecules;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.services.S3SpectralArchive;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.PeptideEvidenceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.SpectraResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidenceResource;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
public class PeptideEvidenceController {

    final PrideMoleculesMongoService moleculesMongoService;
    final S3SpectralArchive spectralArchive;

    @Autowired
    public PeptideEvidenceController(PrideMoleculesMongoService moleculesMongoService, S3SpectralArchive spectralArchive) {
        this.spectralArchive = spectralArchive;
        this.moleculesMongoService = moleculesMongoService;
    }

    @ApiOperation(notes = "Get all the peptide evidences for an specific protein evidence",
            value = "molecules", nickname = "getPeptideEvidencesByProteinEvidence", tags = {"molecules"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptideevidences", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPeptideEvidencesByProteinEvidence(
            @RequestParam(value = "proteinAccession", required = false) String proteinAccession,
            @RequestParam(value = "projectAccession", required = false) String projectAccession,
            @RequestParam(value = "assayAccession"  , required = false) String assayAccession,
            @RequestParam(value="pageSize", defaultValue = "100", required = false) Integer pageSize,
            @RequestParam(value="page", defaultValue = "0" ,  required = false) Integer page,
            @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
            @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.EXTERNAL_PROJECT_ACCESSION,  required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<PrideMongoPeptideEvidence> mongoPeptides = moleculesMongoService.findPeptideEvidences(projectAccession,
                assayAccession, null, proteinAccession,
                PageRequest.of(page, pageSize, direction, sortFields.split(",")));

        PeptideEvidenceAssembler assembler = new PeptideEvidenceAssembler(PeptideEvidenceController.class,
                PeptideEvidenceResource.class);

        List<PeptideEvidenceResource> resources = assembler.toResources(mongoPeptides);

        long totalElements = mongoPeptides.getTotalElements();
        long totalPages = totalElements / pageSize;

        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize,
                page, totalElements, totalPages);

        PagedResources<PeptideEvidenceResource> pagedResources = new PagedResources<>(resources,
                pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideEvidenceController.class)
                        .getPeptideEvidencesByProteinEvidence(proteinAccession,
                                projectAccession, assayAccession,
                                pageSize, page, sortDirection, sortFields)).withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                        .methodOn(PeptideEvidenceController.class).getPeptideEvidencesByProteinEvidence(proteinAccession,
                                projectAccession, assayAccession, pageSize, (int)
                                        WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideEvidenceController.class).getPeptideEvidencesByProteinEvidence(proteinAccession, projectAccession, assayAccession, pageSize,
                        (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideEvidenceController.class).getPeptideEvidencesByProteinEvidence(proteinAccession, projectAccession, assayAccession, pageSize, 0,
                        sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideEvidenceController.class).getPeptideEvidencesByProteinEvidence(proteinAccession, projectAccession, assayAccession, pageSize, (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Get psms by peptide envidence usi ",
            value = "molecules", nickname = "getPsmsByPeptideEvidence", tags = {"molecules"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptideevidences/{usi}/psms", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPsmsByPeptideEvidence(@PathVariable(value = "usi") String usi) {

        Optional<PrideMongoPeptideEvidence> mongoPeptide = Optional.empty();
        SpectraResourceAssembler assembler = new SpectraResourceAssembler(SpectraEvidenceController.class, SpectrumEvidenceResource.class);
        ConcurrentLinkedQueue<PSMProvider> psms = new ConcurrentLinkedQueue<>();

        if(usi != null && ! usi.isEmpty()){
            try {
                String[] values = WsUtils.parsePeptideEvidenceAccession(usi);
                mongoPeptide = moleculesMongoService.findPeptideEvidence(values[0], values[1], values[2], values[3]);
                if(mongoPeptide.isPresent()){
                    mongoPeptide.get().getPsmAccessions().parallelStream().forEach( x-> {
                        try {
                            psms.add(spectralArchive.readPSM(x.getUsi()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(!psms.isEmpty()){
            return new ResponseEntity<>(assembler.toResources(psms), HttpStatus.OK);
        }

        return new ResponseEntity<>(WsContastants.PEPTIDE_USI_NOT_FOUND
                        + usi + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ApiOperation(notes = "Get Peptide Evidence by usi ",
            value = "molecules", nickname = "getPeptideEvidence", tags = {"molecules"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptideevidences/{usi}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPeptideEvidence(@PathVariable(value = "usi") String usi) {

        Optional<PrideMongoPeptideEvidence> mongoPeptide = Optional.empty();
        PeptideEvidenceAssembler assembler = new PeptideEvidenceAssembler(PeptideEvidenceController.class, PeptideEvidenceResource.class);

        if(usi != null && ! usi.isEmpty()){
            try {
                String[] values = WsUtils.parsePeptideEvidenceAccession(usi);
                mongoPeptide = moleculesMongoService.findPeptideEvidence(values[0], values[1], values[2], values[3]);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mongoPeptide.<ResponseEntity<Object>>map(mongoPrideProject ->
                new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PROTEIN_NOT_FOUND
                        + usi + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));
    }


}