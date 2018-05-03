package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactDataset;
import org.springframework.http.HttpEntity;
import org.springframework.hateoas.Resources;


import java.util.Map;

/**
 * The dataset Controller enables to retrieve the information for each PRIDE Project/CompactDataset through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
public class DatasetController {

    @ApiOperation(notes = "Returns all the datasets from PRIDE", value = "Datasets", nickname = "getDatasets", tags = {"Datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Nice!", response = FacetAndHighlightPage.class),
            @ApiResponse(code = 400, message = "Invalid ID supplied", response = ApiResponse.class),
            @ApiResponse(code = 404, message = "Pet not found", response = ApiResponse.class)
    })

    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    public HttpEntity<Resources<CompactDataset>> datasets(
            @RequestParam(value="query", defaultValue = "*:*", required = false) String query,
            @RequestParam(value="Limit of limit", defaultValue = "100", required = false) String limit,
            @RequestParam(value="start", defaultValue = "0" ,required = false) String start,
            @RequestParam(value="sort.field",  defaultValue = "accession" ,required = false) String sortField,
            @RequestParam(value="order",  defaultValue = "asc", required = false) String order){
        return null;
    }
}
