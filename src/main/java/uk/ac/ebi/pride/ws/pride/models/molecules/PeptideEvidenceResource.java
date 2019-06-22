package uk.ac.ebi.pride.ws.pride.models.molecules;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import java.util.List;

public class PeptideEvidenceResource extends Resource<PeptideEvidence> {

public PeptideEvidenceResource(PeptideEvidence content, List<Link> links) {
        super(content, links);
        }
}
