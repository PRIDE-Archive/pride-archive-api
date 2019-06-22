package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.mongodb.molecules.model.protein.PrideMongoProteinEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidenceResource;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidenceResource;

public class PeptideEvidenceAssembler extends ResourceAssemblerSupport<PrideMongoPeptideEvidence, PeptideEvidenceResource> {

    public PeptideEvidenceAssembler(Class<?> controllerClass, Class<PeptideEvidenceResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public PeptideEvidenceResource toResource(PrideMongoPeptideEvidence peptideEvidence) {
        return null;
    }
}
