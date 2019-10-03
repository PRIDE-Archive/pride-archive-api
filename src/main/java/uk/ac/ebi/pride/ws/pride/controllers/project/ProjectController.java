package uk.ac.ebi.pride.ws.pride.controllers.project;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.archive.repo.repos.project.Project;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideProjectField;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.pride.services.SolrProjectService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.FacetResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.PrideProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectFileResourceAssembler;
import uk.ac.ebi.pride.ws.pride.controllers.file.FileController;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.CompactProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectResource;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFileResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.service.project.ProjectService;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;
import uk.ac.ebi.tsc.aap.client.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectResource through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class ProjectController {

    private final SolrProjectService solrProjectService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    final PrideFileMongoService mongoFileService;

    final PrideProjectMongoService mongoProjectService;

    final private ProjectService projectService;

    @Autowired
    public ProjectController(SolrProjectService solrProjectService, CustomPagedResourcesAssembler customPagedResourcesAssembler,
                             PrideFileMongoService mongoFileService,
                             PrideProjectMongoService mongoProjectService, ProjectService projectService, PrideMoleculesMongoService moleculesMongoService) {
        this.solrProjectService = solrProjectService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
        this.mongoFileService = mongoFileService;
        this.mongoProjectService = mongoProjectService;
        this.projectService = projectService;

    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "projects", nickname = "searchProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<CompactProjectResource>> projects(@RequestParam(name = "keyword", defaultValue = "*:*", required = false) List<String> keyword,
                                                                       @RequestParam(name="filter",  defaultValue = "''") String filter,
                                                                       @RequestParam(name="pageSize",  defaultValue = "100") int pageSize,
                                                                       @RequestParam(name="page" , defaultValue = "0" ) int page,
                                                                       @RequestParam(name="dateGap", defaultValue = "") String dateGap,
                                                                       @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
                                                                       @RequestParam(value="sortFields", defaultValue = PrideProjectField.PROJECT_SUBMISSION_DATE ,  required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keyword, filter, PageRequest.of(page, pageSize,direction, sortFields.split(",")), dateGap);
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(ProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, page, dateGap, sortDirection, sortFields))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, 0, dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, (int) totalPages, dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name()),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, WsContastants.MAX_PAGINATION_SIZE, 0, "")).withRel(WsContastants.HateoasEnum.facets.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Return the facets for an specific search query. This method is " +
            "fully-aligned to the entry point search/projects with the parameters: _keywords_, _filter_, _pageSize_, _page_. ", value = "projects", nickname = "getProjectFacets", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<FacetResource>> facets(@RequestParam(value="keyword", defaultValue = "*:*", required = false) List<String> keyword,
                                                            @RequestParam(value="filter", required = false, defaultValue = "''") String filter,
                                                            @RequestParam(value="facetPageSize", defaultValue = "100", required = false) int facetPageSize,
                                                            @RequestParam(value ="facetPage", defaultValue = "0", required = false) int facetPage,
                                                            @RequestParam(value = "dateGap", defaultValue = "", required = false) String dateGap){

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(facetPage, facetPageSize);
        facetPage = facetPageParams.getKey();
        facetPageSize = facetPageParams.getValue();

        FacetPage<PrideSolrProject> solrProjects = solrProjectService.findFacetByKeyword(keyword, filter, PageRequest.of(0, 10), PageRequest.of(facetPage, facetPageSize), dateGap);
        FacetResourceAssembler assembler = new FacetResourceAssembler(ProjectController.class, FacetResource.class, dateGap);
        List<FacetResource> resources = assembler.toResources(solrProjects);


        PagedResources<FacetResource> pagedResources = new PagedResources<>(resources, null,
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, facetPageSize, facetPage, dateGap))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, facetPageSize, facetPage + 1, dateGap))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, facetPageSize, (facetPage > 0)? facetPage -1 : 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, facetPageSize, 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.first.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Return the dataset for a given accession", value = "projects", nickname = "getProject", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/projects/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getProject(@PathVariable(value = "accession", name = "accession") String accession){

        Optional<MongoPrideProject> project = mongoProjectService.findByAccession(accession);
        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(ProjectController.class,
                ProjectResource.class);
        return project.<ResponseEntity<Object>>map(mongoPrideProject -> new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));

    }

    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", value = "projects", nickname = "getProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources> getProjects(@RequestParam(value="pageSize", defaultValue = "100", required = false) int pageSize,
                                                          @RequestParam(value="page", defaultValue = "0" ,  required = false) int page,
                                                            @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
                                                              @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.SUBMISSION_DATE,  required = false) String sortFields) {
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<MongoPrideProject> mongoProjects = mongoProjectService.findAll(PageRequest.of(page, pageSize, direction, sortFields.split(",")));
        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(ProjectController.class, ProjectResource.class);

        List<ProjectResource> resources = assembler.toResources(mongoProjects);

        long totalElements = mongoProjects.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<ProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).getProjects( pageSize, page, sortDirection, sortFields)).withSelfRel(),
                linkTo(methodOn(ProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).getProjects( pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).getProjects(pageSize, 0, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).getProjects(pageSize, (int) totalPages, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "List of Private PRIDE Archive Projects submitted by the user. User needs to be authenticated to view his private submissions", value = "my submissions", nickname = "getMySubmissions", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/projects/private", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Project>> getPrivateProjects(@RequestParam(value="isPublic", defaultValue = "true") boolean isPublic,
                                                         Authentication authentication) {

        User currentUser = (User) (authentication).getDetails();

        List<Project> projectsList = projectService.findUserProjects(currentUser.getUserReference(),isPublic);

        return ResponseEntity.ok().body(projectsList);
    }

    @ApiOperation(notes = "Private PRIDE Archive Project submitted by the user which is under review of the reviewer. User needs to be authenticated to view his private submissions", value = "reviewer view private submission", nickname = "getPrivateProjectForReviewer", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/projects/private/reviewer-submissions/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Project> getPrivateProjectForReviewer(@PathVariable(value ="accession") String projectAccession,
                                                            Authentication authentication) {

        User currentUser = (User) (authentication).getDetails();
        List<Project> projectsList = projectService.findReviewerProjects(currentUser.getUserReference());
        List<Project> privateProjects = projectsList.stream().filter(project -> project.getAccession().equals(projectAccession)).collect(Collectors.toList());
        Project privateProject = null;
        if(privateProjects.size() == 1){
            privateProject = privateProjects.get(0);
        }
        return ResponseEntity.ok().body(privateProject);
    }

    @ApiOperation(notes = "List of PRIDE Archive Projects accessible to reviewer. User needs to be authenticated to view these submissions", value = "reviewer projects", nickname = "getReviewerSubmissions", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "projects/private/reviewer-submissions", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Project>> getReviewerProjects(Authentication authentication) {
        User currentUser = (User) (authentication).getDetails();
        List<Project> projectsList = projectService.findReviewerProjects(currentUser.getUserReference());
        List<Project> privateProjectsList = projectsList.stream().filter(project -> !project.isPublicProject()).collect(Collectors.toList());
        return ResponseEntity.ok().body(privateProjectsList);
    }

    @ApiOperation(notes = "Get all the Files for an specific project in PRIDE.", value = "projects", nickname = "getFilesByProject", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<PrideFileResource>> getFilesByProject(@PathVariable(value ="accession") String projectAccession,
                                                                           @RequestParam(value="filter", required = false, defaultValue = "''") String filter,
                                                                           @RequestParam(value="pageSize",  defaultValue = "100", required = false) Integer pageSize,
                                                                           @RequestParam(value="page", defaultValue = "0" ,  required = false) Integer page,
                                                                           @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
                                                                           @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.FILE_NAME,  required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<MongoPrideFile> projectFiles = mongoFileService.findFilesByProjectAccessionAndFiler(projectAccession, filter, PageRequest.of(page, pageSize,direction,sortFields.split(",")));
        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, page,sortDirection,sortFields)).withSelfRel(),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages),sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession,filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages),sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, 0,sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) totalPages,sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Get Similar projects taking into account the metadata", value = "projects", nickname = "getSimilarProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/similarProjects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources<CompactProjectResource>> getSimilarProjects(@PathVariable(value ="accession") String projectAccession,
                                                 @RequestParam(value ="page", defaultValue = "0") Integer page,
                                                 @RequestParam(value ="pageSize", defaultValue = "100") Integer pageSize){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        List<PrideSolrProject> solrProjects = solrProjectService.findSimilarProjects(projectAccession, pageSize, page);
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(ProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.size();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).getSimilarProjects(projectAccession,  pageSize, page))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) WsUtils.validatePage(page + 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) WsUtils.validatePage(page - 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).getSimilarProjects(projectAccession, pageSize,  0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).getSimilarProjects(projectAccession, pageSize, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "projects", nickname = "searchProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> projects(@RequestParam(name = "keyword") String keyword){

        List<String> terms = solrProjectService.findAutoComplete(keyword);

        return new HttpEntity<>(terms);
    }


}
