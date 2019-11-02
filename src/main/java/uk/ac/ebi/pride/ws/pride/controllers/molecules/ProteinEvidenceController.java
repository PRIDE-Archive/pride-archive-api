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
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.protein.PrideMongoProteinEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Triple;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.ProteinEvidenceAssembler;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
public class ProteinEvidenceController {

    final PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public ProteinEvidenceController(PrideMoleculesMongoService moleculesMongoService) {
        this.moleculesMongoService = moleculesMongoService;
    }


//    @ApiOperation(notes = "Get the protein evidence for the specific accession",
//            value = "proteins", nickname = "getProteinEvidence", tags = {"proteins"} )
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "OK", response = APIError.class),
//            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
//    })
//    @RequestMapping(value = "/proteinevidence", method = RequestMethod.GET,
//            produces = {MediaType.APPLICATION_JSON_VALUE})
//    public HttpEntity<Object> getProteinEvidence(@RequestParam(value = "accession", required = true) String accession){
//
//        Optional<PrideMongoProteinEvidence> mongoProteinEvidence = Optional.empty();
//        ProteinEvidenceAssembler assembler = new ProteinEvidenceAssembler(ProteinEvidenceController.class,
//                ProteinEvidenceResource.class);
//        try {
//            Triple<String, String, String> usi = WsUtils.parseProteinEvidenceAccession(accession);
//             mongoProteinEvidence = moleculesMongoService.findProteinsEvidence(usi.getFirst(),
//                    usi.getSecond(), usi.getThird());
//
//        } catch (Exception e) {
//            log.error(e.getMessage(),e);
//        }
//
//        return mongoProteinEvidence.<ResponseEntity<Object>>map(mongoPrideProject ->
//                new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.OK))
//                .orElseGet(() -> new ResponseEntity<>(WsContastants.PROTEIN_NOT_FOUND
//                        + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));
//    }

    /**
     * In order to retrieve all protein evidences use this function. If you want to retrieve one specific proteinevidence,you can use the following parameters:
     *
     * projectAccession - Project that contains the protein evidence.
     * assayAccession - Assay that contains the protein evidence.
     * reportedAccession - Protein reported Accession
     *
     * @param projectAccession Project Accession
     * @param assayAccession Assay Accession
     * @param reportedAccession Reported Protein Accession
     * @param pageSize Page Size
     * @param page Page
     * @param sortDirection Sort Direction ASC or DESC
     * @param sortFields Sort Fields
     * @return
     */
    @ApiOperation(notes = "Get all the protein evidences", value = "proteins", nickname = "getProteinEvidences", tags = {"proteins"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/proteinevidences", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getProteinEvidences(
            @RequestParam(value = "projectAccession", defaultValue = "",
                    required = false) String projectAccession,
            @RequestParam(value = "assayAccession" , defaultValue = "",
                    required = false) String assayAccession,
            @RequestParam(value = "reportedAccession", defaultValue = "",
                    required = false) String reportedAccession,
            @RequestParam(value="pageSize", defaultValue = "100",
                    required = false) int pageSize,
            @RequestParam(value="page", defaultValue = "0",
                    required = false) int page,
            @RequestParam(value="sortDirection", defaultValue = "DESC",
                    required = false) String sortDirection,
            @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.EXTERNAL_PROJECT_ACCESSION,
                    required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        if(projectAccession == null)
            projectAccession = "";
        if(assayAccession == null)
            assayAccession = "";
        if(reportedAccession == null)
            reportedAccession = "";

        Page<PrideMongoProteinEvidence> mongoProteins = moleculesMongoService.findAllProteinEvidences(projectAccession, assayAccession, reportedAccession, PageRequest.of(page, pageSize, direction, sortFields.split(",")));

        ProteinEvidenceAssembler assembler = new ProteinEvidenceAssembler(ProteinEvidenceController.class, ProteinEvidenceResource.class, sortDirection, sortFields);

        List<ProteinEvidenceResource> resources = assembler.toResources(mongoProteins);

        long totalElements = mongoProteins.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<ProteinEvidenceResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidences(projectAccession,assayAccession,reportedAccession, pageSize, page, sortDirection, sortFields))
                        .withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidences(projectAccession,assayAccession,reportedAccession, pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidences(projectAccession,assayAccession,reportedAccession, pageSize, (int) WsUtils.validatePage(page - 1, totalPages),  sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidences(projectAccession,assayAccession,reportedAccession, pageSize, 0,  sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProteinEvidenceController.class).getProteinEvidences(projectAccession,assayAccession,reportedAccession, pageSize, (int) totalPages,  sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;


        return new HttpEntity<>(pagedResources);
    }
}
