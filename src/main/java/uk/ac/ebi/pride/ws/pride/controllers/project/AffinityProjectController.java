package uk.ac.ebi.pride.ws.pride.controllers.project;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.mongo.client.FileMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.commons.model.PrideArchiveField;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.solr.commons.PrideProjectField;
import uk.ac.ebi.pride.solr.commons.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.services.SolrProjectService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.CompactProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.FacetResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.PrideProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectModel;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectModel through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
@RequestMapping("/pride-ap")
public class AffinityProjectController {

    private final String DEFAULT_AFFINITY_PROJECTS_FILTER = "project_submission_type==AFFINITY";
    private final SolrProjectService solrProjectService;
    final ProjectMongoClient projectMongoClient;
    final ProjectRepoClient projectRepoClient;
    final FileMongoClient fileMongoClient;

    @Autowired
    public AffinityProjectController(SolrProjectService solrProjectService, ProjectMongoClient projectMongoClient,
                                     ProjectRepoClient projectRepoClient,
                                     FileMongoClient fileMongoClient) {
        this.solrProjectService = solrProjectService;
        this.fileMongoClient = fileMongoClient;
        this.projectMongoClient = projectMongoClient;
        this.projectRepoClient = projectRepoClient;
    }

    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "affinity-projects", nickname = "searchProjects", tags = {"affinity-projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedModel<CompactProjectModel>> projects(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword", defaultValue = "*:*", required = false) List<String> keyword,
            @ApiParam(value = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(name = "filter", defaultValue = DEFAULT_AFFINITY_PROJECTS_FILTER) String filter,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(name = "pageSize", defaultValue = "100") int pageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @ApiParam(value = "A date range field with possible values of +1MONTH, +1YEAR")
            @RequestParam(name = "dateGap", defaultValue = "") String dateGap,
            @ApiParam(value = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @ApiParam(value = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortFields", defaultValue = PrideProjectField.PROJECT_SUBMISSION_DATE, required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }

        filter = getDefaultAffinityFilter(filter);

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keyword, filter, PageRequest.of(page, pageSize, direction, sortFields.split(",")), dateGap);
        solrProjects.stream().filter(project -> project.getSubmissionType().equals("AFFINITY"));
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(AffinityProjectController.class, CompactProjectModel.class);

        CollectionModel<CompactProjectModel> resources = assembler.toCollectionModel(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        int totalPages = solrProjects.getTotalPages();
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedModel<CompactProjectModel> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, page, dateGap, sortDirection, sortFields))
                        .withSelfRel(),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, WsUtils.validatePage(page + 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, WsUtils.validatePage(page - 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, 0, dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, WsUtils.validatePage(totalPages - 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name()),
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, WsContastants.MAX_PAGINATION_SIZE, 0, "")).withRel(WsContastants.HateoasEnum.facets.name())
        );

        return new HttpEntity<>(pagedResources);
    }

    private String getDefaultAffinityFilter(String filter) {
        if (!filter.contains(DEFAULT_AFFINITY_PROJECTS_FILTER)) {
            filter = filter.concat("," + DEFAULT_AFFINITY_PROJECTS_FILTER);
        }
        return filter;
    }

    @ApiOperation(notes = "Return the facets for an specific search query. This method is " +
            "fully-aligned to the entry point search/projects with the parameters: _keywords_, _filter_, _pageSize_, _page_. ", value = "affinity-projects", nickname = "getProjectFacets", tags = {"affinity-projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedModel<FacetResource>> facets(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(value = "keyword", defaultValue = "*:*", required = false) List<String> keyword,
            @ApiParam(value = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(value = "filter", required = false, defaultValue = DEFAULT_AFFINITY_PROJECTS_FILTER) String filter,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "facetPageSize", defaultValue = "100", required = false) int facetPageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "facetPage", defaultValue = "0", required = false) int facetPage,
            @ApiParam(value = "A date range field with possible values of +1MONTH, +1YEAR")
            @RequestParam(value = "dateGap", defaultValue = "", required = false) String dateGap) {

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(facetPage, facetPageSize);
        facetPage = facetPageParams.getKey();
        facetPageSize = facetPageParams.getValue();

        filter = getDefaultAffinityFilter(filter);

        FacetPage<PrideSolrProject> solrProjects = solrProjectService.findFacetByKeyword(keyword, filter, PageRequest.of(0, 10), PageRequest.of(facetPage, facetPageSize), dateGap);
        FacetResourceAssembler assembler = new FacetResourceAssembler(AffinityProjectController.class, FacetResource.class, dateGap);
        CollectionModel<FacetResource> resources = assembler.toCollectionModel(solrProjects);


        PagedModel<FacetResource> pagedResources = PagedModel.of(resources.getContent(), null, Arrays.asList(
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, facetPage, dateGap))
                        .withSelfRel(),
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, facetPage + 1, dateGap))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, (facetPage > 0) ? facetPage - 1 : 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.first.name())
        ));

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", value = "affinity-projects", nickname = "getProjects", tags = {"affinity-projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<HttpEntity<PagedModel<ProjectResource>>> getProjects(
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @ApiParam(value = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @ApiParam(value = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortConditions", defaultValue = PrideArchiveField.SUBMISSION_DATE, required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }
        final Sort.Direction sortDirectionFinal = direction;

        List<String> submissionType = new ArrayList<>(2);
        submissionType.add("AFFINITY");
        Mono<Page<MongoPrideProject>> allProjectsPageMono = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
        return allProjectsPageMono.map(mongoProjectsPage -> {
            PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class, ProjectResource.class, fileMongoClient);
            CollectionModel<ProjectResource> resources = assembler.toCollectionModel(mongoProjectsPage.getContent());

            long totalElements = mongoProjectsPage.getTotalElements();
            int totalPages = mongoProjectsPage.getTotalPages();
            PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSizeFinal, pageFinal, totalElements, totalPages);

            PagedModel<ProjectResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, pageFinal, sortDirectionFinal.name(), sortFields)).withSelfRel(),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(pageFinal + 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.next.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(pageFinal - 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.previous.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, 0, sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.first.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(totalPages - 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.last.name())
            );

            return new HttpEntity<>(pagedResources);

        });
    }

    //        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
//        page = pageParams.getKey();
//        pageSize = pageParams.getValue();
//        Sort.Direction direction = Sort.Direction.DESC;
//        if (sortDirection.equalsIgnoreCase("ASC")) {
//            direction = Sort.Direction.ASC;
//        }
//
//        Page<MongoPrideProject> mongoProjects = mongoProjectService.findAll(PageRequest.of(page, pageSize, direction, sortFields.split(",")));

    //FIXME TODO fileter for AFFINITY SubmissionType should be done at the DB query .. Otherwise it will end up with lots of empty pages.
//        List<MongoPrideProject> filteredList = mongoProjects.stream().filter(project -> project.getSubmissionType().equals("AFFINITY")).collect(Collectors.toList());
//        mongoProjects = new PageImpl<>(filteredList, PageRequest.of(page, pageSize, direction, sortFields.split(",")), filteredList.size());
//
//        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(AffinityProjectController.class, ProjectResource.class, mongoFileService);
//
//        CollectionModel<ProjectResource> resources = assembler.toCollectionModel(mongoProjects);
//
//        long totalElements = mongoProjects.getTotalElements();
//        long totalPages = mongoProjects.getTotalPages();
//        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);
//
//        PagedModel<ProjectResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, page, sortDirection, sortFields)).withSelfRel(),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.next.name()),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.previous.name()),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, 0, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.first.name()),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) totalPages, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.last.name())
//        );
//
//        return new HttpEntity<>(pagedResources);
//    }


    @ApiOperation(notes = "Get Similar projects taking into account the metadata", value = "affinity-projects", nickname = "getSimilarProjects", tags = {"affinity-projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/similarProjects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedModel<CompactProjectModel>> getSimilarProjects(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession") String projectAccession,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        List<PrideSolrProject> solrProjects = solrProjectService.findSimilarProjects(projectAccession, pageSize, page);
        solrProjects = solrProjects.stream().filter(project -> project.getSubmissionType().equals("AFFINITY")).collect(Collectors.toList());
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(AffinityProjectController.class, CompactProjectModel.class);

        CollectionModel<CompactProjectModel> resources = assembler.toCollectionModel(solrProjects);

        long totalElements = solrProjects.size();
        int totalPages = (int) (totalElements / pageSize);
        if (totalElements % pageSize > 0)
            totalPages++;
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedModel<CompactProjectModel> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, page, pageSize))
                        .withSelfRel(),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, WsUtils.validatePage(page + 1, totalPages), pageSize))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, WsUtils.validatePage(page - 1, totalPages), pageSize))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, 0, pageSize))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, WsUtils.validatePage(totalPages - 1, totalPages), pageSize))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "affinity-projects", nickname = "searchProjects", tags = {"affinity-projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> projects(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword") String keyword) {

        List<String> terms = solrProjectService.findAutoComplete(keyword);

        return new HttpEntity<>(terms);
    }
}
