package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;
import uk.ac.ebi.pride.solr.indexes.pride.repository.SolrProjectRepository;
import uk.ac.ebi.pride.ws.pride.mappers.DatasetResourceMapper;
import uk.ac.ebi.pride.ws.pride.models.dataset.ResourceDataset;
import org.springframework.http.HttpEntity;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.util.List;

/**
 * The dataset Controller enables to retrieve the information for each PRIDE Project/ResourceDataset through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class DatasetController {

    @Autowired
    SolrProjectRepository solrProjectRepository;

    @ApiOperation(notes = "Returns all the datasets from PRIDE", value = "datasets", nickname = "getDatasets", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/datasets", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<ResourceDataset>> datasets(
            @RequestParam(value="query", defaultValue = "*:*", required = false) String query,
            @RequestParam(value="limit", defaultValue = "100", required = false) int size,
            @RequestParam(value="start", defaultValue = "0" ,required = false) long start,
            @RequestParam(value="sort.field",  defaultValue = "accession" ,required = false) String sortField,
            @RequestParam(value="order",  defaultValue = "asc", required = false) String order){


        FacetAndHighlightPage<PrideSolrDataset> solrProjects = solrProjectRepository.findAllWithFacetIgnoreCase(new PageRequest(0,200));
        DatasetResourceMapper assembler = new DatasetResourceMapper(DatasetController.class, ResourceDataset.class);
        List<ResourceDataset> resources = assembler.toResources(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<ResourceDataset> wrapped = new PagedResources<>(resources, pageMetadata,
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, start, sortField, order))
                        .withSelfRel(),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, start + 1, sortField, order))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, start - 1, sortField, order))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, 0, sortField, order))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).datasets(query, size, totalPages, sortField, order))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );
        return new HttpEntity<>(wrapped);
    }

    @ApiOperation(notes = "Return the dataset for a given accession", value = "datasets", nickname = "getDataset", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/datasets/{accession}", method = RequestMethod.GET)
    public HttpEntity<ResourceDataset> getDataset(@PathVariable(value = "accession", required = true, name = "accession") String accession) {
        return null;
    }
}
