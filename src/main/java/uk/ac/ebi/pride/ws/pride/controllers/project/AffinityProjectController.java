package uk.ac.ebi.pride.ws.pride.controllers.project;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.util.ProjectStatus;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.solr.commons.PrideProjectField;
import uk.ac.ebi.pride.solr.commons.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.services.SolrProjectService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.CompactProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.FacetResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.PrideProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectResource through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
@RequestMapping("/pride-ap")
public class AffinityProjectController {


    private final String DEFAULT_AFFINITY_PROJECTS_FILTER = "project_submission_type==AFFINITY";

    private final SolrProjectService solrProjectService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    final PrideProjectMongoService mongoProjectService;

    final ProjectRepoClient projectRepoClient;


    @Autowired
    public AffinityProjectController(SolrProjectService solrProjectService, PrideProjectMongoService mongoProjectService, CustomPagedResourcesAssembler customPagedResourcesAssembler, ProjectRepoClient projectRepoClient) {
        this.solrProjectService = solrProjectService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
        this.mongoProjectService = mongoProjectService;
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
    public HttpEntity<PagedResources<CompactProjectResource>> projects(
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
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(AffinityProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = solrProjects.getTotalPages();
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, page, dateGap, sortDirection, sortFields))
                        .withSelfRel(),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, 0, dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(AffinityProjectController.class).projects(keyword, filter, pageSize, (int) totalPages, dateGap, sortDirection, sortFields))
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
    public HttpEntity<PagedResources<FacetResource>> facets(
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
        List<FacetResource> resources = assembler.toResources(solrProjects);


        PagedResources<FacetResource> pagedResources = new PagedResources<>(resources, null,
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, facetPage, dateGap))
                        .withSelfRel(),
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, facetPage + 1, dateGap))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, (facetPage > 0) ? facetPage - 1 : 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(AffinityProjectController.class).facets(keyword, filter, facetPageSize, 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.first.name())
        );

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", value = "affinity-projects", nickname = "getProjects", tags = {"affinity-projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources> getProjects(
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @ApiParam(value = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @ApiParam(value = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortConditions", defaultValue = PrideArchiveField.SUBMISSION_DATE, required = false) String sortFields) {
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Page<MongoPrideProject> mongoProjects = mongoProjectService.findAll(PageRequest.of(page, pageSize, direction, sortFields.split(",")));

        List<MongoPrideProject> filteredList = mongoProjects.stream().filter(project -> project.getSubmissionType().equals("AFFINITY")).collect(Collectors.toList());
        mongoProjects = new PageImpl<>(filteredList, PageRequest.of(page, pageSize, direction, sortFields.split(",")), filteredList.size());

        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(AffinityProjectController.class, ProjectResource.class);

        List<ProjectResource> resources = assembler.toResources(mongoProjects);

        long totalElements = mongoProjects.getTotalElements();
        long totalPages = mongoProjects.getTotalPages();
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<ProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, page, sortDirection, sortFields)).withSelfRel(),
                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, 0, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Get Similar projects taking into account the metadata", value = "affinity-projects", nickname = "getSimilarProjects", tags = {"affinity-projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/similarProjects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<CompactProjectResource>> getSimilarProjects(
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
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(AffinityProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.size();
        long totalPages = totalElements / pageSize;
        if (totalElements % pageSize > 0)
            totalPages++;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, pageSize, page))
                        .withSelfRel(),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) WsUtils.validatePage(page + 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) WsUtils.validatePage(page - 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, pageSize, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(AffinityProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) totalPages))
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
