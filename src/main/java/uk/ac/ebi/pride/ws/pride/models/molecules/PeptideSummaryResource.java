package uk.ac.ebi.pride.ws.pride.models.molecules;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import java.util.List;

public class PeptideSummaryResource extends Resource<PeptideSummary> {

    public PeptideSummaryResource(PeptideSummary content, List<Link> links) {
        super(content, links);
    }
}
