package uk.ac.ebi.pride.ws.pride.models.dataset;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import uk.ac.ebi.pride.ws.pride.hateoas.Facets;

/**
 * @author ypriverol
 *
 */
public class FacetResource extends Resource<Facets> {

    public FacetResource(Facets content, Iterable<Link> links) {
        super(content, links);
    }
}
