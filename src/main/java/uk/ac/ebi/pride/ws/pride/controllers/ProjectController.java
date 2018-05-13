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
import uk.ac.ebi.pride.solr.indexes.pride.repository.SolrProjectRepository;
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

    final SolrProjectRepository solrProjectRepository;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    @Autowired
    public ProjectController(SolrProjectRepository solrProjectRepository, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.solrProjectRepository = solrProjectRepository;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive", value = "projects", nickname = "getProjects", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<ProjectResource>> projects(
            @RequestParam(value="query", defaultValue = "*:*", required = false) List<String> keywords,
            @RequestParam(value="limit", defaultValue = "100", required = false) int size,
            @RequestParam(value="start", defaultValue = "0" ,  required = false) int start){

        Page<PrideSolrProject> solrProjects = solrProjectRepository.findByKeyword(keywords, new PageRequest(start, size));
        ProjectResourceAssembler assembler = new ProjectResourceAssembler(ProjectController.class, ProjectResource.class);

        List<ProjectResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<ProjectResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).projects(keywords, size, start))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).projects(keywords, size, start + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).projects(keywords, size, start - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).projects(keywords, size, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).projects(keywords, size, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name()),
                linkTo(methodOn(ProjectController.class).facets(keywords, WsContastants.MAX_PAGINATION_SIZE, 0)).withRel(WsContastants.HateoasEnum.facets.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Returns the facets for projects search", value = "projects", nickname = "getDatasetsFacets", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<FacetResource>> facets(
            @RequestParam(value="query", defaultValue = "*:*", required = false) List<String> keywords,
            @RequestParam(value="limit", defaultValue = "40", required = false) long size,
            @RequestParam(value="start", defaultValue = "0" ,required = false)   int start){


        Page<PrideSolrProject> solrProjects = solrProjectRepository.findAllFacetIgnoreCase(new PageRequest(start,(int)size));
        FacetResourceAssembler assembler = new FacetResourceAssembler(ProjectController.class, FacetResource.class, start);

        List<FacetResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        int totalPages = (int) (totalElements / size);
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<FacetResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(ProjectController.class).facets(keywords, size, start))
                        .withSelfRel(),
                linkTo(methodOn(ProjectController.class).facets(keywords, size, start + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(ProjectController.class).facets(keywords, size, start - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(ProjectController.class).facets(keywords, size, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(ProjectController.class).facets(keywords, size, totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Return the dataset for a given accession", value = "projects", nickname = "getDataset", tags = {"projects"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/datasets/{accession}", method = RequestMethod.GET)
    public HttpEntity<ProjectResource> getDataset(@PathVariable(value = "accession", required = true, name = "accession") String accession) {
        return null;
    }
}
