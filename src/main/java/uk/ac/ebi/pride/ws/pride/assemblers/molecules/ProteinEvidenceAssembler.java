package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.IdentifiedModification;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.protein.PrideMongoProteinEvidence;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.PeptideEvidenceController;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.ProteinEvidenceController;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidenceResource;
import uk.ac.ebi.pride.ws.pride.transformers.Transformer;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProteinEvidenceAssembler extends ResourceAssemblerSupport<PrideMongoProteinEvidence, ProteinEvidenceResource> {

    private String sortDirection;
    private String sortFields;

    public ProteinEvidenceAssembler(Class<?> proteinEvidenceControllerClass, Class<ProteinEvidenceResource> proteinEvidenceResourceClass, String sortDirection, String sortFields) {
        super(proteinEvidenceControllerClass, proteinEvidenceResourceClass);
        this.sortDirection = sortDirection;
        this.sortFields = sortFields;
    }

    @Override
    public ProteinEvidenceResource toResource(PrideMongoProteinEvidence prideMongoProteinEvidence) {

        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(ProteinEvidenceController.class)
                        .getProteinEvidences(prideMongoProteinEvidence.getProjectAccession(),
                                prideMongoProteinEvidence.getAssayAccession(),
                                prideMongoProteinEvidence.getReportedAccession(), 10, 0, sortDirection, sortFields))
                .withSelfRel());

        Link link = ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PeptideEvidenceController.class)
                        .getPeptideEvidences(prideMongoProteinEvidence.getProjectAccession(), prideMongoProteinEvidence.getAssayAccession(), prideMongoProteinEvidence.getReportedAccession(),
                                "","", WsContastants.MAX_PAGINATION_SIZE, 0, "DESC" , PrideArchiveField.EXTERNAL_PROJECT_ACCESSION))
                .withRel(WsContastants.HateoasEnum.peptideevidences.name());
        links.add(link);

        return new ProteinEvidenceResource(transform(prideMongoProteinEvidence), links);
    }

    /**
     * Transform a {@link PrideMongoProteinEvidence} into a {@link ProteinEvidence}
     * @param prideMongoProteinEvidence Mongo protein evidence
     * @return A {@link ProteinEvidence}
     */
    private ProteinEvidence transform(PrideMongoProteinEvidence prideMongoProteinEvidence) {
        Set<CvParam> additionalAttributes = prideMongoProteinEvidence.getAdditionalAttributes();

        CvParam bestSearchEngine = null;
        if(prideMongoProteinEvidence.getBestSearchEngineScore() != null){
            bestSearchEngine = new CvParam(prideMongoProteinEvidence.getBestSearchEngineScore().getCvLabel(),
                    prideMongoProteinEvidence.getBestSearchEngineScore().getAccession(),
                    prideMongoProteinEvidence.getBestSearchEngineScore().getName(),
                    prideMongoProteinEvidence.getBestSearchEngineScore().getValue());
        }
        List<IdentifiedModification> ptms = new ArrayList<>();
        if(prideMongoProteinEvidence.getPtms() != null){
            ptms = Transformer.transformModifications(prideMongoProteinEvidence.getPtms());
        }

        return ProteinEvidence.builder()
                .usi(WsUtils.getIdentifier(prideMongoProteinEvidence.getProjectAccession(),
                        prideMongoProteinEvidence.getAssayAccession(),
                        prideMongoProteinEvidence.getReportedAccession()))
                .reportedAccession(prideMongoProteinEvidence.getReportedAccession())
                .assayAccession(prideMongoProteinEvidence.getAssayAccession())
                .projectAccession(prideMongoProteinEvidence.getProjectAccession())
                .proteinDescription(prideMongoProteinEvidence.getProteinDescription())
                .proteinGroupMembers(prideMongoProteinEvidence.getProteinGroupMembers())
                .proteinSequence(prideMongoProteinEvidence.getProteinSequence())
                .additionalAttributes(additionalAttributes)
                .bestSearchEngineScore(bestSearchEngine)
                .isValid(prideMongoProteinEvidence.getIsValid())
                .qualityMethods(prideMongoProteinEvidence.getQualityEstimationMethods()
                        .stream()
                        .map( x-> new CvParam(((CvParamProvider) x).getCvLabel(), ((CvParamProvider) x).getAccession(),
                                ((CvParamProvider) x).getName(), ((CvParamProvider) x).getValue()))
                        .collect(Collectors.toList()))
                .ptms(ptms)
                .numberPeptides(prideMongoProteinEvidence.getNumberPeptides())
                .numberPSMs(prideMongoProteinEvidence.getNumberPSMs())
                .sequenceCoverage(prideMongoProteinEvidence.getSequenceCoverage())
                .build();
    }
}
