package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideProjectMongoService;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.pride.services.SolrProjectService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.FacetResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.PrideProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectFileResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.CompactProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectResource;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFileResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
import java.util.Map;
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


    @Autowired
    public ProjectController(SolrProjectService solrProjectService, CustomPagedResourcesAssembler customPagedResourcesAssembler,
                             PrideFileMongoService mongoFileService,
                             PrideProjectMongoService mongoProjectService) {
        this.solrProjectService = solrProjectService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
        this.mongoFileService = mongoFileService;
        this.mongoProjectService = mongoProjectService;
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
                                                                       @RequestParam(name="dateGap", defaultValue = "") String dateGap ){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keyword, filter, PageRequest.of(page, pageSize), dateGap);
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(ProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, page, dateGap))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages), dateGap))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages), dateGap))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, pageSize, (int) totalPages, dateGap))
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
    public ResponseEntity<Object> getProject(@PathVariable(value = "accession", required = true, name = "accession") String accession){

        Optional<MongoPrideProject> project = mongoProjectService.findByAccession(accession);
        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(ProjectController.class, ProjectResource.class);
        return project.<ResponseEntity<Object>>map(mongoPrideProject -> new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.ACCEPTED))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));

    }

    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", value = "projects", nickname = "getProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources> getProjects(@RequestParam(value="pageSize", defaultValue = "100", required = false) int pageSize,
                                                          @RequestParam(value="page", defaultValue = "0" ,  required = false) int page) {
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Page<MongoPrideProject> mongoProjects = mongoProjectService.findAll(PageRequest.of(page, pageSize));
        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(ProjectController.class, ProjectResource.class);

        List<ProjectResource> resources = assembler.toResources(mongoProjects);

        long totalElements = mongoProjects.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<ProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).getProjects( pageSize, page)).withSelfRel(),
                linkTo(methodOn(ProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page + 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).getProjects( pageSize, (int) WsUtils.validatePage(page - 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).getProjects(pageSize, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).getProjects(pageSize, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
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
                                                                           @RequestParam(value="page", defaultValue = "0" ,  required = false) Integer page){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();


        Page<MongoPrideFile> projectFiles = mongoFileService.findFilesByProjectAccessionAndFiler(projectAccession, filter, PageRequest.of(page, pageSize));
        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, page)).withSelfRel(),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession,filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages)))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) totalPages))
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
    public HttpEntity<Object> projects(@RequestParam(name = "keyword", required = true) String keyword){

        List<String> terms = solrProjectService.findAutoComplete(keyword);

        return new HttpEntity<>(terms);
    }

}
