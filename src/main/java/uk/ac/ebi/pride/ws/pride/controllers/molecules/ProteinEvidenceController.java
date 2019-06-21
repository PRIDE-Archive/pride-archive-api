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
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.model.protein.PrideMongoProteinEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.PrideProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.ProteinEvidenceAssembler;
import uk.ac.ebi.pride.ws.pride.controllers.project.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
import java.util.Optional;

@RestController
public class ProteinEvidenceController {

    final PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public ProteinEvidenceController(PrideMoleculesMongoService moleculesMongoService) {
        this.moleculesMongoService = moleculesMongoService;
    }


    @ApiOperation(notes = "Get all the protein evidences of an specific Project",
            value = "proteinevidences", nickname = "getProteinEvidencesByProject", tags = {"proteinevidences"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/proteinevidences/{projectAccession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getProteinEvidencesByProject(
            @PathVariable(value = "projectAccession") String projectAccession,
            @RequestParam(value="pageSize", defaultValue = "100", required = false) int pageSize,
            @RequestParam(value="page", defaultValue = "0" ,  required = false) int page,
            @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
            @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.EXTERNAL_PROJECT_ACCESSION,  required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<PrideMongoProteinEvidence> mongoProteins = moleculesMongoService.findProteinsByProjectAccession(projectAccession, PageRequest.of(page, pageSize, direction, sortFields.split(",")));

        ProteinEvidenceAssembler assembler = new ProteinEvidenceAssembler(ProteinEvidenceController.class,
                ProteinEvidenceResource.class);

        List<ProteinEvidenceResource> resources = assembler.toResources(mongoProteins);

        long totalElements = mongoProteins.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize,
                page, totalElements, totalPages);

        PagedResources<ProteinEvidenceResource> pagedResources = new PagedResources<ProteinEvidenceResource>(resources,
                pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class)
                        .getProteinEvidencesByProject(projectAccession, pageSize, page, sortDirection, sortFields)).withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder
                        .methodOn(ProteinEvidenceController.class).getProteinEvidencesByProject(projectAccession, pageSize, (int)
                                WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidencesByProject( projectAccession, pageSize,
                        (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidencesByProject(projectAccession, pageSize, 0,
                        sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidencesByProject(projectAccession, pageSize, (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Get all the protein evidences", value = "proteinevidences", nickname = "getProteinEvidences", tags = {"proteinevidences"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/proteinevidences", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> projects(@RequestParam(value="pageSize", defaultValue = "100", required = false) int pageSize,
                                       @RequestParam(value="page", defaultValue = "0" ,  required = false) int page,
                                       @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
                                       @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.EXTERNAL_PROJECT_ACCESSION,  required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<PrideMongoProteinEvidence> mongoProteins = moleculesMongoService.findAllProteinEvidences(PageRequest.of(page, pageSize, direction, sortFields.split(",")));


        return new HttpEntity<>(mongoProteins);
    }

    @ApiOperation(notes = "Get all the protein evidences", value = "proteinevidences", nickname = "getProteinEvidences", tags = {"proteinevidences"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/proteinevidence", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})

    public ResponseEntity<Object> getProteinEvidence(@RequestParam(value = "reportedAccession") String reportedAccession,
                                                     @RequestParam(value = "projectAccession") String projectAccession,
                                                     @RequestParam(value = "assayAccession") String assayAccession) {

        Optional<PrideMongoProteinEvidence> mongoProteinEvidence = moleculesMongoService.getProteinEvidence(reportedAccession,
                projectAccession, assayAccession);

        ProteinEvidenceAssembler assembler = new ProteinEvidenceAssembler(ProteinEvidenceController.class,
                ProteinEvidenceResource.class);

        return mongoProteinEvidence.<ResponseEntity<Object>>map(mongoPrideProject -> new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + projectAccession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));




    }
}
