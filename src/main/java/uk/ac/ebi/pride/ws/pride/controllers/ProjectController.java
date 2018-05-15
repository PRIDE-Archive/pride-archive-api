package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.pride.services.SolrProjectService;
import uk.ac.ebi.pride.ws.pride.assemblers.FacetResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import org.springframework.http.HttpEntity;
import uk.ac.ebi.pride.ws.pride.utils.ErrorInfo;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

/**
 * The dataset Controller enables to retrieve the information for each PRIDE Project/ProjectResource through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class ProjectController {

    private final SolrProjectService solrProjectService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    @Autowired
    public ProjectController(SolrProjectService solrProjectService, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.solrProjectService = solrProjectService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1:value1, field2:value2.", value = "projects", nickname = "searchProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<ProjectResource>> projects(@RequestParam(value="List of Keywords", defaultValue = "*:*", required = false) List<String> keyword,
                                                                @RequestParam(value="Filter by property", required = false, defaultValue = "''") String filter,
                                                                @RequestParam(value="Number projects per page ", defaultValue = "100", required = false) int size,
                                                                @RequestParam(value="Page number", defaultValue = "0" ,  required = false) int start){

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keyword, filter, new PageRequest(start, size));
        ProjectResourceAssembler assembler = new ProjectResourceAssembler(ProjectController.class, ProjectResource.class);

        List<ProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<ProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
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
                linkTo(methodOn(ProjectController.class).facets(keyword, filter, (int) WsContastants.MAX_PAGINATION_SIZE, 0)).withRel(WsContastants.HateoasEnum.facets.name())
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


        Page<PrideSolrProject> solrProjects = solrProjectService.findFacetByKeyword(keyword, filter, new PageRequest(start, size));
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
    public HttpEntity<ProjectResource> getProject(@PathVariable(value = "accession", required = true, name = "accession") String accession) {
        return null;
    }



    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _size_ and _start_.", value = "projects", nickname = "getProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)})
    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public HttpEntity<ProjectResource> getProjects(@RequestParam(value="Number projects per page", defaultValue = "100", required = false) int size,
                                                   @RequestParam(value="Page number", defaultValue = "0" ,  required = false) int start) {
        return null;
    }


}
