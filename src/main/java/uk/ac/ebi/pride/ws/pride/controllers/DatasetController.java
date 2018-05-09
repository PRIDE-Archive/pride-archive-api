package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;
import uk.ac.ebi.pride.solr.indexes.pride.repository.SolrProjectRepository;
import uk.ac.ebi.pride.ws.pride.assemblers.FacetResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.DatasetResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.FacetsResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.DatasetREsource;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;
import org.springframework.http.HttpEntity;
import uk.ac.ebi.pride.ws.pride.utils.ErrorInfo;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.util.List;

/**
 * The dataset Controller enables to retrieve the information for each PRIDE Project/DatasetREsource through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class DatasetController {

    final SolrProjectRepository solrProjectRepository;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    @Autowired
    public DatasetController(SolrProjectRepository solrProjectRepository, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.solrProjectRepository = solrProjectRepository;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }


    @ApiOperation(notes = "Search all public datasets inPRIDE Archive", value = "datasets", nickname = "getDatasets", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/datasets", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity datasets(
            @RequestParam(value="query", defaultValue = "*:*", required = false) String query,
            @RequestParam(value="limit", defaultValue = "100", required = false) long size,
            @RequestParam(value="start", defaultValue = "0" ,required = false)   long start,
            @RequestParam(value="sort.field",  defaultValue = "accession" , required = false) String sortField,
            @RequestParam(value="order",  defaultValue = "asc", required = false) String order){


        Page<PrideSolrDataset> solrProjects = solrProjectRepository.findAllIgnoreCase(new PageRequest(0,200));
        DatasetResourceAssembler assembler = new DatasetResourceAssembler(DatasetController.class, DatasetREsource.class);

        List<DatasetREsource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources pagedResources = new PagedResources(resources, pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, start, sortField, order))
                        .withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, start + 1, sortField, order))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, start - 1, sortField, order))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, 0, sortField, order))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, totalPages, sortField, order))
                        .withRel(WsContastants.HateoasEnum.last.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).facets(query, WsContastants.MAX_PAGINATION_SIZE, 0)).withRel(WsContastants.HateoasEnum.facets.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Returns the facets for datasets search", value = "datasets", nickname = "getDatasetsFacets", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/facets", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity facets(
            @RequestParam(value="query", defaultValue = "*:*", required = false) String query,
            @RequestParam(value="limit", defaultValue = "100", required = false) long size,
            @RequestParam(value="start", defaultValue = "0" ,required = false)   long start){


        Page<PrideSolrDataset> solrProjects = solrProjectRepository.findAllFacetIgnoreCase(new PageRequest(0,200));
        FacetResourceAssembler assembler = new FacetResourceAssembler(DatasetController.class, FacetResource.class);

        List<FacetResource> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources pagedResources = new PagedResources(resources, pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).facets(query, size, start))
                        .withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).facets(query, size, start + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).facets(query, size, start - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).facets(query, size, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).facets(query, size, totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Return the dataset for a given accession", value = "datasets", nickname = "getDataset", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/datasets/{accession}", method = RequestMethod.GET)
    public HttpEntity<DatasetREsource> getDataset(@PathVariable(value = "accession", required = true, name = "accession") String accession) {
        return null;
    }
}
