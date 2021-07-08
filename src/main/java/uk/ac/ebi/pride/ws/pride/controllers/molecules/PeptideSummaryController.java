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
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideSummary;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.PeptideSummaryAssembler;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideSummaryResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;

@RestController
@Slf4j
public class PeptideSummaryController {

    final PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public PeptideSummaryController(PrideMoleculesMongoService moleculesMongoService) {
        this.moleculesMongoService = moleculesMongoService;
    }

    @ApiOperation(notes = "Get all the peptide summary records for an specific peptideSequence",
            value = "peptide_summary", nickname = "getPeptideSummaryByPeptideSequence", tags = {"peptide_summary"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptidesummary/peptide/{peptideSequence}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPeptideSummaryByPeptideSequence(
            @PathVariable(value = "peptideSequence") String peptideSequence,
            @RequestParam(value = "pageSize", defaultValue = "100",
                    required = false) Integer pageSize,
            @RequestParam(value = "page", defaultValue = "0",
                    required = false) Integer page,
            @RequestParam(value = "sortDirection", defaultValue = "DESC",
                    required = false) String sortDirection) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize, WsContastants.MAX_PAGINATION_SIZE);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Page<PrideMongoPeptideSummary> mongoPeptides = moleculesMongoService.findPeptideSummaryByPeptideSequence(peptideSequence, PageRequest.of(page, pageSize, direction));

        PeptideSummaryAssembler assembler = new PeptideSummaryAssembler(PeptideSummaryController.class,
                PeptideSummaryResource.class, sortDirection);

        List<PeptideSummaryResource> resources = assembler.toResources(mongoPeptides);

        long totalElements = mongoPeptides.getTotalElements();
        long totalPages = mongoPeptides.getTotalPages();

        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize,
                page, totalElements, totalPages);

        PagedResources<PeptideSummaryResource> pagedResources = new PagedResources<>(resources,
                pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence, pageSize, page, sortDirection)).withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence,
                                pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection)).withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence,
                                pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection)).withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence,
                                pageSize, 0, sortDirection)).withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence,
                                pageSize, (int) totalPages, sortDirection)).withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }

}
