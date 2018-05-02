package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.archive.dataprovider.dataset.DatasetProvider;

/**
 * The dataset Controller enables to retrieve the information for each PRIDE Project/Dataset.
 *
 */

@RestController
public class DatasetController {



    @ApiOperation(notes = "Returns all the datasets from PRIDE with some pagination", value = "Datasets", nickname = "getDatasets", tags = {"Datasets"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Nice!", response = FacetAndHighlightPage.class),
            @ApiResponse(code = 400, message = "Invalid ID supplied", response = ApiResponse.class),
            @ApiResponse(code = 404, message = "Pet not found", response = ApiResponse.class)
    })
    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    public FacetAndHighlightPage<DatasetProvider> datasets(@RequestParam(value="name", defaultValue = "PXD00001") String name){
        return null;
    }
}
