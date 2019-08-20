package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.PeptideEvidenceController;
import uk.ac.ebi.pride.ws.pride.models.molecules.IdentifiedModification;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidenceResource;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;
import uk.ac.ebi.pride.ws.pride.transformers.Transformer;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PeptideEvidenceAssembler extends ResourceAssemblerSupport<PrideMongoPeptideEvidence, PeptideEvidenceResource> {

    public PeptideEvidenceAssembler(Class<?> controllerClass, Class<PeptideEvidenceResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public PeptideEvidenceResource toResource(PrideMongoPeptideEvidence peptideEvidence) {

        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PeptideEvidenceController.class)
                        .getPeptideEvidence(WsUtils.getIdentifier(peptideEvidence.getProjectAccession(),
                                peptideEvidence.getAssayAccession(),
                                peptideEvidence.getProteinAccession(),
                                WsUtils.mongoPeptideUiToPeptideEvidence(peptideEvidence.getPeptideAccession()))))
                .withSelfRel());

        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PeptideEvidenceController.class)
                        .getPsmsByPeptideEvidence(WsUtils.getIdentifier(peptideEvidence.getProjectAccession(),
                                peptideEvidence.getAssayAccession(),
                                peptideEvidence.getProteinAccession(),
                                WsUtils.mongoPeptideUiToPeptideEvidence(peptideEvidence.getPeptideAccession()))))
                .withRel(WsContastants.HateoasEnum.psms.name()));


        return new PeptideEvidenceResource(transform(peptideEvidence), links);
    }

    private PeptideEvidence transform(PrideMongoPeptideEvidence mongoPeptide) {
        List<uk.ac.ebi.pride.ws.pride.models.param.CvParam> attributes = mongoPeptide.getAdditionalAttributes()
                .stream()
                .map( x-> new CvParam(x.getCvLabel(), x.getAccession(),
                        x.getName(), x.getValue()))
                .collect(Collectors.toList());

        List<IdentifiedModification> ptms = new ArrayList<>();
        if(mongoPeptide.getPtmList() != null && !mongoPeptide.getPtmList().isEmpty())
            ptms = Transformer.transformModifications(mongoPeptide.getPtmList());

        return PeptideEvidence.builder()
                .accession(WsUtils.getIdentifier(mongoPeptide.getProjectAccession(),
                        mongoPeptide.getAssayAccession(),
                        mongoPeptide.getProteinAccession(),
                        WsUtils.mongoPeptideUiToPeptideEvidence(mongoPeptide.getPeptideAccession())))
                .peptideSequence(mongoPeptide.getPeptideSequence())
                .ptms(ptms)
                .properties(attributes)
                .isDecoy(mongoPeptide.isDecoy())
                .proteinAccession(mongoPeptide.getProteinAccession())
                .projectAccession(mongoPeptide.getProjectAccession())
                .assayAccession(mongoPeptide.getAssayAccession())
                .startPostion(mongoPeptide.getStartPosition())
                .endPostion(mongoPeptide.getEndPosition())
                .isValid(mongoPeptide.getIsValid())
                .qualityMethods(mongoPeptide.getQualityEstimationMethods()
                        .stream()
                        .map( x-> new CvParam(((CvParamProvider) x).getCvLabel(), ((CvParamProvider) x).getAccession(),
                                ((CvParamProvider) x).getName(), ((CvParamProvider) x).getValue()))
                        .collect(Collectors.toList()))
                .missedCleavages(mongoPeptide.getMissedCleavages())
                .build();
    }
}
