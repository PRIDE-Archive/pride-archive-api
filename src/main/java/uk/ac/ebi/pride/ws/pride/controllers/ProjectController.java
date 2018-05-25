package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideFileMongoService;
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
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideFileResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

/**
 * The dataset Controller enables to retrieve the information for each PRIDE Project/CompactProjectResource through a RestFull API.
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
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<CompactProjectResource>> projects(@RequestParam(value="List of Keywords", defaultValue = "*:*", required = false) List<String> keyword,
                                                                       @RequestParam(value="Filter by property", required = false, defaultValue = "''") String filter,
                                                                       @RequestParam(value="Number projects per page ", defaultValue = "100", required = false) int size,
                                                                       @RequestParam(value="Page number", defaultValue = "0" ,  required = false) int start){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(start, size);
        start = pageParams.getKey();
        size = pageParams.getValue();

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keyword, filter, PageRequest.of(start, size));
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(ProjectController.class, CompactProjectResource.class);

        List<CompactProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<CompactProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, size, start))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, size, start + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, size, start - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, size, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).projects(keyword, filter, size, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name()),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, WsContastants.MAX_PAGINATION_SIZE, 0)).withRel(WsContastants.HateoasEnum.facets.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Return the facets for an specific search query. This method is fully-aligned to the entry point search/projects with the parameters: _keywords_, _filter_, _size_, _start_. ", value = "projects", nickname = "getProjectFacets", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<FacetResource>> facets(@RequestParam(value="List of Keywords", defaultValue = "*:*", required = false) List<String> keyword,
                                                            @RequestParam(value="Filter by property", required = false, defaultValue = "''") String filter,
                                                            @RequestParam(value="Number projects per page ", defaultValue = "100", required = false) int size,
                                                            @RequestParam(value="Page number", defaultValue = "0" ,  required = false) int start){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(start, size);
        start = pageParams.getKey();
        size = pageParams.getValue();

        Page<PrideSolrProject> solrProjects = solrProjectService.findFacetByKeyword(keyword, filter, PageRequest.of(start, size));
        FacetResourceAssembler assembler = new FacetResourceAssembler(ProjectController.class, FacetResource.class, start);

        List<FacetResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        int totalPages = (int) (totalElements / size);
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<FacetResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, size, start))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, size, start + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, size, start - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, size, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, size, totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Return the dataset for a given accession", value = "projects", nickname = "getProject", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/projects/{accession}", method = RequestMethod.GET)
    public ResponseEntity<Object> getProject(@PathVariable(value = "accession", required = true, name = "accession") String accession){

        Optional<MongoPrideProject> project = mongoProjectService.findByAccession(accession);
        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(ProjectController.class, ProjectResource.class);
        return project.<ResponseEntity<Object>>map(mongoPrideProject -> new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.ACCEPTED))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));

    }



    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _size_ and _start_.", value = "projects", nickname = "getProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public HttpEntity<CompactProjectResource> getProjects(@RequestParam(value="Number projects per page", defaultValue = "100", required = false) int size,
                                                          @RequestParam(value="Page number", defaultValue = "0" ,  required = false) int start) {
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(start, size);
        start = pageParams.getKey();
        size = pageParams.getValue();

        return null;
    }


    @ApiOperation(notes = "Get all the Files for an specific project in PRIDE.", value = "projects", nickname = "getFilesByProject", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<PrideFileResource>> getFilesByProject(@PathVariable(value ="Project accession") String projectAccession,
                                                                           @RequestParam(value="Filter by property", required = false, defaultValue = "''") String filter,
                                                                           @RequestParam(value="Number files per page ", defaultValue = "100", required = false) int size,
                                                                           @RequestParam(value="Page number", defaultValue = "0" ,  required = false) int start){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(start, size);
        start = pageParams.getKey();
        size = pageParams.getValue();


        Page<MongoPrideFile> projectFiles = mongoFileService.findFilesByProjectAccessionAndFiler(projectAccession, filter, PageRequest.of(start, size));
        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, size, start)).withSelfRel(),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, size, start + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession,filter, size, start - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, size, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).getFilesByProject(projectAccession, filter, size, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


}
