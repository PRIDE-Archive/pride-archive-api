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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideSummary;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.molecules.PeptideSummaryAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProjectForPeptideSummary;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideDetails;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideSummaryResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class PeptideSummaryController {

    private final PrideMoleculesMongoService moleculesMongoService;
    private final PrideProjectMongoService mongoProjectService;


    @Autowired
    public PeptideSummaryController(PrideMoleculesMongoService moleculesMongoService, PrideProjectMongoService mongoProjectService) {
        this.moleculesMongoService = moleculesMongoService;
        this.mongoProjectService = mongoProjectService;
    }

    @ApiOperation(notes = "Get all the peptide summary records",
            value = "peptide_summary", nickname = "getPeptideSummary", tags = {"peptide_summary"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptidesummary", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPeptideSummary(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "includeUniprotOnly", required = false) Boolean includeUniprotOnly,
            @RequestParam(value = "isMultiorganismPeptide", required = false) Boolean isMultiorganismPeptide,
            @RequestParam(value = "isUniqPeptideWithinOrganism", required = false) Boolean isUniqPeptideWithinOrganism,
            @RequestParam(value = "pageSize", defaultValue = "100",
                    required = false) Integer pageSize,
            @RequestParam(value = "page", defaultValue = "0",
                    required = false) Integer page,
            @RequestParam(value = "sortDirection", defaultValue = "DESC",
                    required = false) String sortDirection,
            @RequestParam(value = "sortConditions", defaultValue = PrideArchiveField.PSMS_COUNT,
                    required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize,
                WsContastants.MAX_PAGINATION_SIZE);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }
        if (keyword == null) {
            keyword = ""; // this is needed to fix the sortConditions in Hateoas links. Without this "sortConditions=psms_count{&keyword}"
        }
        PageRequest pageRequest = PageRequest.of(page, pageSize, direction, sortFields.split(","));

        Page<PrideMongoPeptideSummary> mongoPeptides;

        mongoPeptides = moleculesMongoService.
                findUniprotPeptideSummaryByProperties(keyword, includeUniprotOnly,
                        isMultiorganismPeptide, isUniqPeptideWithinOrganism, pageRequest);

        PeptideSummaryAssembler assembler = new PeptideSummaryAssembler(PeptideSummaryController.class,
                PeptideSummaryResource.class);

        List<PeptideSummaryResource> resources = assembler.toResources(mongoPeptides);

        long totalElements = mongoPeptides.getTotalElements();
        long totalPages = mongoPeptides.getTotalPages();

        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize,
                page, totalElements, totalPages);

        PagedResources<PeptideSummaryResource> pagedResources = new PagedResources<>(resources,
                pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummary(keyword, includeUniprotOnly, isMultiorganismPeptide,
                                isUniqPeptideWithinOrganism, pageSize, page, sortDirection, sortFields)).withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummary(keyword, includeUniprotOnly, isMultiorganismPeptide,
                                isUniqPeptideWithinOrganism, pageSize, (int) WsUtils.validatePage(page + 1, totalPages),
                                sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummary(keyword, includeUniprotOnly, isMultiorganismPeptide,
                                isUniqPeptideWithinOrganism, pageSize, (int) WsUtils.validatePage(page - 1, totalPages),
                                sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                                .getPeptideSummary(keyword, includeUniprotOnly, isMultiorganismPeptide,
                                        isUniqPeptideWithinOrganism, pageSize, 0, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                                .getPeptideSummary(keyword, includeUniprotOnly, isMultiorganismPeptide,
                                        isUniqPeptideWithinOrganism, pageSize, (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Get all the peptide summary records for an specific peptideSequence",
            value = "peptide_summary", nickname = "getPeptideSummaryByPeptideSequence", tags = {"peptide_summary"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptidesummary/peptide", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPeptideSummaryByPeptideSequence(
            @RequestParam(value = "keyword", required = false) String peptideSequence,
            @RequestParam(value = "proteinAccession", required = false) String proteinAccession,
            @RequestParam(value = "pageSize", defaultValue = "100",
                    required = false) Integer pageSize,
            @RequestParam(value = "page", defaultValue = "0",
                    required = false) Integer page,
            @RequestParam(value = "sortDirection", defaultValue = "DESC",
                    required = false) String sortDirection,
            @RequestParam(value = "sortConditions", defaultValue = PrideArchiveField.PSMS_COUNT,
                    required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize, WsContastants.MAX_PAGINATION_SIZE);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }
        if (peptideSequence == null) {
            peptideSequence = ""; // this is needed to fix the sortConditions in Hateoas links. Without this "sortConditions=psms_count{&keyword}"
        }
        if (proteinAccession == null) {
            proteinAccession = ""; // this is needed to fix the sortConditions in Hateoas links. Without this "sortConditions=psms_count{&proteinAccession}"
        }
        Page<PrideMongoPeptideSummary> mongoPeptides = moleculesMongoService.findPeptideSummary(peptideSequence, proteinAccession, PageRequest.of(page, pageSize, direction, sortFields.split(",")));

        PeptideSummaryAssembler assembler = new PeptideSummaryAssembler(PeptideSummaryController.class,
                PeptideSummaryResource.class);

        List<PeptideSummaryResource> resources = assembler.toResources(mongoPeptides);

        long totalElements = mongoPeptides.getTotalElements();
        long totalPages = mongoPeptides.getTotalPages();

        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize,
                page, totalElements, totalPages);

        PagedResources<PeptideSummaryResource> pagedResources = new PagedResources<>(resources,
                pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence, proteinAccession, pageSize, page, sortDirection, sortFields)).withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence, proteinAccession,
                                pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence, proteinAccession,
                                pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence, proteinAccession,
                                pageSize, 0, sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PeptideSummaryController.class)
                        .getPeptideSummaryByPeptideSequence(peptideSequence, proteinAccession,
                                pageSize, (int) totalPages, sortDirection, sortFields)).withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Get all the peptide details for a given specific peptideSequence and proteinAccession",
            value = "peptide_details", nickname = "getPeptideDetails", tags = {"peptide_summary"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/peptidedetails", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getPeptideDetails(
            @RequestParam(value = "peptideSequence") String peptideSequence,
            @RequestParam(value = "proteinAccession") String proteinAccession) {

        PrideMongoPeptideSummary mongoPeptideSummary = moleculesMongoService.findPeptideSummary(peptideSequence, proteinAccession);

        String[] projectAccessions = mongoPeptideSummary.getProjectAccessions();
        List<MongoPrideProject> mongoPrideProjects = mongoProjectService.findByMultipleAccessions(Arrays.asList(projectAccessions));
        List<PrideProjectForPeptideSummary> prideProjectsForPeptideSummary = mongoPrideProjects.stream()
                .map(PrideProjectForPeptideSummary::fromMongoPrideProject).collect(Collectors.toList());

        PeptideDetails peptideDetails = PeptideDetails.builder()
                .peptideSequence(mongoPeptideSummary.getPeptideSequence())
                .bestSearchEngineScore(mongoPeptideSummary.getBestSearchEngineScore())
                .bestUsis(mongoPeptideSummary.getBestUsis())
                .psmsCount(mongoPeptideSummary.getPsmsCount())
                .proteinAccession(mongoPeptideSummary.getProteinAccession())
                .projects(prideProjectsForPeptideSummary)
                .ptmsMap(WsUtils.peptideSummaryEnhancePtmsMap(mongoPeptideSummary))
                .proteinName(mongoPeptideSummary.getProteinName())
                .isUnique(mongoPeptideSummary.getIsUnique())
                .isMultiOrganism(mongoPeptideSummary.getIsMultiOrganism())
                .gene(mongoPeptideSummary.getGene())
                .organism(mongoPeptideSummary.getOrganism())
                .taxId(mongoPeptideSummary.getTaxId())
                .build();

        return new HttpEntity<>(peptideDetails);
    }

}
