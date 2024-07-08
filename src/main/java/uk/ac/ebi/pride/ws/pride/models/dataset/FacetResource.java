package uk.ac.ebi.pride.ws.pride.models.dataset;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import uk.ac.ebi.pride.ws.pride.hateoas.Facets;

/**
 * @author ypriverol
 */
public class FacetResource extends EntityModel<Facets> {

    public FacetResource(Facets content, Iterable<Link> links) {
        super(content, links);
    }
}
