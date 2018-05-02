package uk.ac.ebi.pride.ws.pride.controllers;

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

    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    public FacetAndHighlightPage<DatasetProvider> datasets(@RequestParam(value="name", defaultValue = "PXD00001") String name){
        return null;
    }
}
