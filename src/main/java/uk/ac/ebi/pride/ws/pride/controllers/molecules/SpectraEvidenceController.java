package uk.ac.ebi.pride.ws.pride.controllers.molecules;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.spectra.model.ArchiveSpectrum;
import uk.ac.ebi.pride.archive.spectra.services.S3SpectralArchive;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.SpectraResourceAssembler;
import uk.ac.ebi.pride.ws.pride.controllers.project.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectResource;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class SpectraEvidenceController {


    S3SpectralArchive spectralArchive;
    PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public SpectraEvidenceController(S3SpectralArchive spectralArchive, PrideMoleculesMongoService moleculesMongoService){
        this.spectralArchive = spectralArchive;
        this.moleculesMongoService = moleculesMongoService;
    }

    @ApiOperation(notes = "Get an Spectrum by the specific usi", value = "molecules", nickname = "getSpectrum", tags = {"molecules"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/spectra/{usi}", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getSpectrum(@PathVariable(value = "usi") String usi){

        Optional<PSMProvider> spectrumOptional = Optional.empty();
        SpectraResourceAssembler assembler = new SpectraResourceAssembler(SpectraEvidenceController.class,
                SpectrumEvidenceResource.class);
        try {
            PSMProvider evidence = spectralArchive.readPSM(usi);
            if(evidence != null)
                spectrumOptional = Optional.of(evidence);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return spectrumOptional.<ResponseEntity<Object>>map( spectrum ->
                new ResponseEntity<>(assembler.toResource(spectrum), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PROTEIN_NOT_FOUND
                        + usi + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));
    }

    @ApiOperation(notes = "Get an Spectrum by the Project Accession or Assay Accession usi", value = "molecules", nickname = "getSpectrumBy", tags = {"molecules"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/spectra", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    //Todo: All the spectra retrieve methods should be done using java.util.concurrent.CompletableFuture from Spring.
    public HttpEntity<Object> getSpectrumBy(@RequestParam(value = "projectAccession") String projectAccession,
                                            @RequestParam(value="page", defaultValue = "0" ,  required = false) int page,
                                            @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
                                            @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.EXTERNAL_PROJECT_ACCESSION,  required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, 50);
        page = pageParams.getKey();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<PrideMongoPeptideEvidence> peptides = moleculesMongoService.findPeptideEvidencesByProjectAccession(projectAccession,
                PageRequest.of(page, 50, direction, sortFields.split(",")));

        ConcurrentLinkedQueue<PSMProvider> psms = new ConcurrentLinkedQueue<>();
        peptides.getContent().parallelStream().forEach( peptideEvidence -> {
            peptideEvidence.getPsmAccessions().parallelStream().forEach( psm -> {
                try {
                    psms.add(spectralArchive.readPSM(psm.getUsi()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        });

        SpectraResourceAssembler assembler = new SpectraResourceAssembler(SpectraEvidenceController.class, SpectrumEvidenceResource.class);
        long totalPages = peptides.getTotalPages();
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(50, page, psms.size(), totalPages);

        PagedResources<SpectrumEvidenceResource> pagedResources = new PagedResources<>(assembler.toResources(psms), pageMetadata,
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(projectAccession, page, sortDirection, sortFields)).withSelfRel(),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(projectAccession, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(projectAccession, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(projectAccession, 0, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(SpectraEvidenceController.class).getSpectrumBy(projectAccession,  (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }



}
