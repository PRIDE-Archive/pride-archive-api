package uk.ac.ebi.pride.ws.pride.models.dataset;


import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.ac.ebi.pride.archive.dataprovider.dataset.DatasetProvider;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * The Resource of each class in the model add different links to make the resource discoverable. You can read more here:
 * https://spring.io/understanding/HATEOAS
 *
 * @author yriverol
 */

public class ResourceDataset extends Resource<CompactDataset>{

    /**
     * Default constructor for Resource Dataset including hateoas links.
     * @param content Object that would be represented
     * @param links links.
     */
    public ResourceDataset(CompactDataset content, Iterable<Link> links) {
        super(content, links);

    }
}
