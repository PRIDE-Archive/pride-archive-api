package uk.ac.ebi.pride.ws.pride.models.file;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

/**
 * @author ypriverol
 */
public class PrideFileResource extends EntityModel<PrideFile> {

    /**
     * Default constructor for Pride File including hateoas links.
     *
     * @param content Object that would be represented
     * @param links   links.
     */
    public PrideFileResource(PrideFile content, Iterable<Link> links) {
        super(content, links);

    }
}