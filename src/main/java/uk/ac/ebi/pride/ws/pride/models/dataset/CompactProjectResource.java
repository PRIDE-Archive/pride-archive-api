package uk.ac.ebi.pride.ws.pride.models.dataset;


import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

/**
 * The Resource of each class in the model add different links to make the resource discoverable. You can read more here:
 * https://spring.io/understanding/HATEOAS
 *
 * @author yriverol
 */

public class CompactProjectResource extends Resource<CompactProject>{

    /**
     * Default constructor for Resource Dataset including hateoas links.
     * @param content Object that would be represented
     * @param links links.
     */
    public CompactProjectResource(CompactProject content, Iterable<Link> links) {
        super(content, links);

    }
}
