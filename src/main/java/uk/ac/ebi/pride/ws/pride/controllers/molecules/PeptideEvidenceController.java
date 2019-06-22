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
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.model.protein.PrideMongoProteinEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.PeptideEvidenceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.ProteinEvidenceAssembler;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidenceResource;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;

public class PeptideEvidenceController {

    final PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public PeptideEvidenceController(PrideMoleculesMongoService moleculesMongoService) {
        this.moleculesMongoService = moleculesMongoService;
    }

    @ApiOperation(notes = "Get all the peptide evidences for an specific protein evidence",
            value = "proteinevidences", nickname = "getPeptideEvidencesByProteinEvidence", tags = {"proteinevidences"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptideevidences", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPeptideEvidencesByProteinEvidence(
            @RequestParam(value = "proteinAccession") String proteinAccession,
            @RequestParam(value = "projectAccession") String projectAccession,
            @RequestParam(value = "assayAccession") String assayAccession,
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

        Page<PrideMongoPeptideEvidence> mongoPeptides = moleculesMongoService.findPeptideEvidencesByProteinEvidence(proteinAccession,
                projectAccession,assayAccession,
                PageRequest.of(page, pageSize, direction, sortFields.split(",")));

        PeptideEvidenceAssembler assembler = new PeptideEvidenceAssembler(PeptideEvidenceController.class,
                PeptideEvidenceResource.class);

        List<PeptideEvidenceResource> resources = assembler.toResources(mongoPeptides);

        long totalElements = mongoPeptides.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize,
                page, totalElements, totalPages);

        PagedResources<PeptideEvidenceResource> pagedResources = new PagedResources<PeptideEvidenceResource>(resources,
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
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideEvidenceController.class).getPeptideEvidencesByProteinEvidence( proteinAccession, projectAccession, assayAccession, pageSize,
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
}
