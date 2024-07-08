package uk.ac.ebi.pride.ws.pride.models.dataset;


import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

/**
 * The Resource of each class in the model add different links to make the resource discoverable. You can read more here:
 * https://spring.io/understanding/HATEOAS
 *
 * @author yriverol
 */

public class CompactProjectModel extends EntityModel<CompactProject> {

    /**
     * Default constructor for Resource Dataset including hateoas links.
     *
     * @param content Object that would be represented
     * @param links   links.
     */
    public CompactProjectModel(CompactProject content, Iterable<Link> links) {
        super(content, links);

    }
}
