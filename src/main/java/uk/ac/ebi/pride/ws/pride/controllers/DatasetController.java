package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;
import uk.ac.ebi.pride.ws.pride.mappers.DatasetResourceMapper;
import uk.ac.ebi.pride.ws.pride.models.dataset.ResourceDataset;
import org.springframework.http.HttpEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * The dataset Controller enables to retrieve the information for each PRIDE Project/ResourceDataset through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class DatasetController {

    @ApiOperation(notes = "Returns all the datasets from PRIDE", value = "datasets", nickname = "getDatasets", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid ID supplied", response = ApiResponse.class),
            @ApiResponse(code = 404, message = "Pet not found", response = ApiResponse.class)
    })

    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    public HttpEntity<PagedResources<ResourceDataset>> datasets(
            @RequestParam(value="query", defaultValue = "*:*", required = false) String query,
            @RequestParam(value="limit", defaultValue = "100", required = false) String limit,
            @RequestParam(value="start", defaultValue = "0" ,required = false) String start,
            @RequestParam(value="sort.field",  defaultValue = "accession" ,required = false) String sortField,
            @RequestParam(value="order",  defaultValue = "asc", required = false) String order){

        PrideSolrDataset dataset = new PrideSolrDataset();
        dataset.setAccession("PXD00001111");
        List<PrideSolrDataset> list = new ArrayList<>();
        list.add(dataset);
        
        DatasetResourceMapper assembler = new DatasetResourceMapper(DatasetController.class, ResourceDataset.class);
        List<ResourceDataset> resources = assembler.toResources(list);

        long size = 5;
        long number = 1;
        long totalElements = resources.size();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, number, totalElements, totalPages);

        PagedResources<ResourceDataset> wrapped = new PagedResources<ResourceDataset>(resources, pageMetadata, ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).getDataset(dataset.getAccession())).withSelfRel());
        return new HttpEntity<>(wrapped);
    }

    @ApiOperation(notes = "Return the dataset for a given accession", value = "datasets", nickname = "getDataset", tags = {"datasets"} )
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid ID supplied", response = ApiResponse.class),
            @ApiResponse(code = 404, message = "Pet not found", response = ApiResponse.class)
    })

    @RequestMapping(value = "/datasets/{accession}", method = RequestMethod.GET)
    private HttpEntity<ResourceDataset> getDataset(@PathVariable(value = "accession", required = true, name = "accession") String accession) {
        return null;
    }
}
