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
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.util.ProjectStatus;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoImportedProject;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideReanalysisProject;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.ImportedProjectMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideReanalysisMongoService;
import uk.ac.ebi.pride.solr.commons.PrideProjectField;
import uk.ac.ebi.pride.solr.commons.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.services.SolrProjectService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.*;
import uk.ac.ebi.pride.ws.pride.controllers.file.FileController;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProjectMetadata;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectReanalysisResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFileResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectResource through a RestFull API.
 *
 * @author ypriverol
 */

@RestController("/")
public class MassSpecProjectController {


    private final String PARTIAL_SUBMISSION_TYPE_FILTER= "project_submission_type==PARTIAL";

    private final String COMPLETE_SUBMISSION_TYPE_FILTER= "project_submission_type==COMPLETE";

    private final String DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER = PARTIAL_SUBMISSION_TYPE_FILTER+","
            + COMPLETE_SUBMISSION_TYPE_FILTER;

    private final SolrProjectService solrProjectService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    final PrideFileMongoService mongoFileService;

    final PrideProjectMongoService mongoProjectService;

    final ImportedProjectMongoService importedProjectMongoService;

    final PrideReanalysisMongoService prideReanalysisMongoService;

    final ProjectRepoClient projectRepoClient;

    @Autowired
    public MassSpecProjectController(SolrProjectService solrProjectService,
                                     CustomPagedResourcesAssembler customPagedResourcesAssembler,
                                     PrideFileMongoService mongoFileService,
                                     PrideProjectMongoService mongoProjectService,
                                     ImportedProjectMongoService importedProjectMongoService, PrideReanalysisMongoService prideReanalysisMongoService, ProjectRepoClient projectRepoClient) {
        this.solrProjectService = solrProjectService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
        this.mongoFileService = mongoFileService;
        this.mongoProjectService = mongoProjectService;
        this.importedProjectMongoService = importedProjectMongoService;
        this.prideReanalysisMongoService = prideReanalysisMongoService;
        this.projectRepoClient = projectRepoClient;
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "projects", nickname = "searchProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<CompactProjectResource>> projects(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword", defaultValue = "*:*", required = false) List<String> keyword,
            @ApiParam(value = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(name = "filter", defaultValue = DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER) String filter,
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

        filter = getDefaultMassSpecFilter(filter);

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keyword, filter, PageRequest.of(page, pageSize, direction, sortFields.split(",")), dateGap);
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(MassSpecProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = solrProjects.getTotalPages();
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, page, dateGap, sortDirection, sortFields))
                        .withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, 0, dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, (int) totalPages, dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name()),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, WsContastants.MAX_PAGINATION_SIZE, 0, "")).withRel(WsContastants.HateoasEnum.facets.name())
        );

        return new HttpEntity<>(pagedResources);
    }

    private String getDefaultMassSpecFilter(String filter) {
        if (!filter.contains(COMPLETE_SUBMISSION_TYPE_FILTER) && !filter.contains(PARTIAL_SUBMISSION_TYPE_FILTER) ) {
            filter = filter.concat("," + DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER);
        }
        return filter;
    }

    @ApiOperation(notes = "Return the facets for an specific search query. This method is " +
            "fully-aligned to the entry point search/projects with the parameters: _keywords_, _filter_, _pageSize_, _page_. ", value = "projects", nickname = "getProjectFacets", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<FacetResource>> facets(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(value = "keyword", defaultValue = "*:*", required = false) List<String> keyword,
            @ApiParam(value = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(value = "filter", required = false, defaultValue = DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER) String filter,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "facetPageSize", defaultValue = "100", required = false) int facetPageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "facetPage", defaultValue = "0", required = false) int facetPage,
            @ApiParam(value = "A date range field with possible values of +1MONTH, +1YEAR")
            @RequestParam(value = "dateGap", defaultValue = "", required = false) String dateGap) {

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(facetPage, facetPageSize);
        facetPage = facetPageParams.getKey();
        facetPageSize = facetPageParams.getValue();

        filter = getDefaultMassSpecFilter(filter);

        FacetPage<PrideSolrProject> solrProjects = solrProjectService.findFacetByKeyword(keyword, filter, PageRequest.of(0, 10), PageRequest.of(facetPage, facetPageSize), dateGap);
        FacetResourceAssembler assembler = new FacetResourceAssembler(MassSpecProjectController.class, FacetResource.class, dateGap);
        List<FacetResource> resources = assembler.toResources(solrProjects);


        PagedResources<FacetResource> pagedResources = new PagedResources<>(resources, null,
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, facetPage, dateGap))
                        .withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, facetPage + 1, dateGap))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, (facetPage > 0) ? facetPage - 1 : 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.first.name())
        );

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Return the dataset for a given accession", value = "projects", nickname = "getProject", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/projects/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getProject(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession", name = "accession") String accession) {

        Optional<MongoPrideProject> project = mongoProjectService.findByAccession(accession);
        if (!project.isPresent()) {
            Optional<MongoImportedProject> mongoImportedProjectOptional = importedProjectMongoService.findByAccession(accession);
            if (mongoImportedProjectOptional.isPresent()) {
                MongoPrideProject mongoImportedProject = mongoImportedProjectOptional.get();
                project = Optional.of(mongoImportedProject);
            }
        }
        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class,
                ProjectResource.class,mongoFileService);
        return project.<ResponseEntity<Object>>map(mongoPrideProject -> new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));

    }

    @ApiOperation(notes = "Return the list of publications that have re-used the specified dataset", value = "projects", nickname = "getReanalysedProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/projects/reanalysis/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getReanalysisProject(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession", name = "accession") String accession) {

        Optional<MongoPrideReanalysisProject> project = prideReanalysisMongoService.findByAccession(accession);
        PrideReanalysisProjectResourceAssembler assembler = new PrideReanalysisProjectResourceAssembler(MassSpecProjectController.class, ProjectReanalysisResource.class);
        ResponseEntity<Object> responseEntity = project.<ResponseEntity<Object>>map(reanalysisProject -> new ResponseEntity<>(assembler.toResource(reanalysisProject), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));
        return responseEntity;

    }

    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", value = "projects", nickname = "getProjects", tags = {"projects"})
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
        List<MongoPrideProject> filteredList = mongoProjects.stream().filter(project -> project.getSubmissionType().equals("COMPLETE") || project.getSubmissionType().equals("PARTIAL")).collect(Collectors.toList());
        mongoProjects = new PageImpl<>(filteredList,PageRequest.of(page, pageSize, direction, sortFields.split(",")),filteredList.size());
        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class, ProjectResource.class,mongoFileService);

        List<ProjectResource> resources = assembler.toResources(mongoProjects);

        long totalElements = mongoProjects.getTotalElements();
        long totalPages = mongoProjects.getTotalPages();
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<ProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, page, sortDirection, sortFields)).withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, 0, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Get all the Files for an specific project in PRIDE.", value = "projects", nickname = "getFilesByProject", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<PrideFileResource>> getFilesByProject(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession") String projectAccession,
            @ApiParam(value = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(value = "filter", required = false, defaultValue = "''") String filter,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) Integer pageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @ApiParam(value = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @ApiParam(value = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortConditions", defaultValue = PrideArchiveField.FILE_NAME, required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }

        Page<MongoPrideFile> projectFiles = mongoFileService.findFilesByProjectAccessionAndFiler(projectAccession, filter, PageRequest.of(page, pageSize, direction, sortFields.split(",")));
        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = projectFiles.getTotalPages();
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, page, sortDirection, sortFields)).withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, 0, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Get Similar projects taking into account the metadata", value = "projects", nickname = "getSimilarProjects", tags = {"projects"})
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
        solrProjects = solrProjects.stream().filter(project -> project.getSubmissionType().equals("COMPLETE") || project.getSubmissionType().equals("PARTIAL")).collect(Collectors.toList());
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(MassSpecProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.size();
        long totalPages = totalElements / pageSize;
        if (totalElements % pageSize > 0)
            totalPages++;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, pageSize, page))
                        .withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) WsUtils.validatePage(page + 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) WsUtils.validatePage(page - 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, pageSize, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "projects", nickname = "searchProjects", tags = {"projects"})
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

    @GetMapping(value = "/status/{accession}", produces = {MediaType.TEXT_PLAIN_VALUE})
    public String getProjectStatus(@Valid @PathVariable String accession) throws IOException {
        ProjectStatus status = projectRepoClient.getProjectStatus(accession);
        return status.name();
    }

    @ApiOperation(notes = "List of paged PRIDE Archive Projects with metadata", value = "projects", nickname = "getProjectsMetadata", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects/metadata", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<PrideProjectMetadata> getProjectsMetadata(@ApiParam(value = "Identifies which page of results to fetch")
                                                                  @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                              @ApiParam(value = "Number of results to fetch in a page")
                                                                  @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
        return mongoProjectService.findAll(PageRequest.of(page,pageSize)).stream().map(
                project -> new PrideProjectMetadata(project.getAccession(),project.getTitle(),project.getSubmissionType(),project.getDescription(),project.getSampleProcessingProtocol(),project.getDataProcessingProtocol())
        ).collect(Collectors.toList());

    }
}
