package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.common.ITuple;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.DefaultIdentifiedModification;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.protein.PrideMongoProteinEvidence;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.PeptideEvidenceController;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.ProteinEvidenceController;
import uk.ac.ebi.pride.ws.pride.models.molecules.IdentifiedModification;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidenceResource;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ProteinEvidenceAssembler extends ResourceAssemblerSupport<PrideMongoProteinEvidence, ProteinEvidenceResource> {

    public ProteinEvidenceAssembler(Class<?> controllerClass, Class<ProteinEvidenceResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public ProteinEvidenceResource toResource(PrideMongoProteinEvidence prideMongoProteinEvidence) {

        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(ProteinEvidenceController.class)
                        .getProteinEvidence(prideMongoProteinEvidence.getReportedAccession(), prideMongoProteinEvidence.getProjectAccession(), prideMongoProteinEvidence.getAssayAccession()))
                .withSelfRel());

        // This needs to be build in a different way

//        Method method = null;
//        try {
//            method = PeptideEvidenceController.class.getMethod("getPeptideEvidences",
//                    String.class, String.class, Integer.class, Integer.class, String.class, String.class);
//            Link link = ControllerLinkBuilder.linkTo(method, prideMongoProteinEvidence.getAccession(), "",
//                    WsContastants.MAX_PAGINATION_SIZE, 0,
//                    "DESC" , PrideArchiveField.EXTERNAL_PROJECT_ACCESSION)
//                    .withRel(WsContastants.HateoasEnum.files.name());
//            links.add(link);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }

        return new ProteinEvidenceResource(transform(prideMongoProteinEvidence), links);
    }

    /**
     * Transform a {@link PrideMongoProteinEvidence} into a {@link ProteinEvidence}
     * @param prideMongoProteinEvidence Mongo protein evidence
     * @return A {@link ProteinEvidence}
     */
    private ProteinEvidence transform(PrideMongoProteinEvidence prideMongoProteinEvidence) {
        List<CvParam> additionalAttributes = prideMongoProteinEvidence.getAdditionalAttributes()
                .stream()
                .map( x-> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                .collect(Collectors.toList());

        CvParam bestSearchEngine = null;
        if(prideMongoProteinEvidence.getBestSearchEngineScore() != null){
            bestSearchEngine = new CvParam(prideMongoProteinEvidence.getBestSearchEngineScore().getCvLabel(),
                    prideMongoProteinEvidence.getBestSearchEngineScore().getAccession(),
                    prideMongoProteinEvidence.getBestSearchEngineScore().getName(),
                    prideMongoProteinEvidence.getBestSearchEngineScore().getValue());
        }
        List<IdentifiedModification> ptms = new ArrayList<>();
        if(prideMongoProteinEvidence.getPtms() != null){
            ptms = prideMongoProteinEvidence.getPtms().stream().map( ptm -> {
                CvParam ptmName = new CvParam(ptm.getModificationCvTerm().getCvLabel(),
                        ptm.getModificationCvTerm().getAccession(),
                        ptm.getModificationCvTerm().getName(),
                        ptm.getModificationCvTerm().getValue());

                CvParam neutral = null;
                if(ptm.getNeutralLoss() != null)
                    neutral = new CvParam(ptm.getNeutralLoss().getCvLabel(),
                            ptm.getNeutralLoss().getAccession(),
                            ptm.getNeutralLoss().getName(),
                            ptm.getNeutralLoss().getValue());

                List<Tuple<Integer, List<CvParam>>> ptmPositions = ptm.getPositionMap().stream().map(position ->{
                    Collection<CvParamProvider> scores = (Collection<CvParamProvider>) position.getValue();
                    Integer currentPosition = position.getKey();
                    List<CvParam> newScores = scores.stream().map(score -> new CvParam(score.getCvLabel(),
                            score.getAccession(), score.getName(),
                            score.getValue())).collect(Collectors.toList());
                    return new Tuple<>(currentPosition, newScores);
                }).collect(Collectors.toList());

                IdentifiedModification newPTM = IdentifiedModification.builder()
                        .modification(ptmName)
                        .neutralLoss(neutral)
                        .positionMap(ptmPositions)
                        .build();

                return newPTM;
            }).collect(Collectors.toList());
        }

        return ProteinEvidence.builder()
                .reportedAccession(prideMongoProteinEvidence.getReportedAccession())
                .assayAccession(prideMongoProteinEvidence.getAssayAccession())
                .projectAccession(prideMongoProteinEvidence.getProjectAccession())
                .proteinDescription(prideMongoProteinEvidence.getProteinDescription())
                .proteinGroupMembers(prideMongoProteinEvidence.getProteinGroupMembers())
                .proteinSequence(prideMongoProteinEvidence.getProteinSequence())
                .additionalAttributes(additionalAttributes)
                .bestSearchEngineScore(bestSearchEngine)
                .ptms(ptms)
                .build();
    }
}
