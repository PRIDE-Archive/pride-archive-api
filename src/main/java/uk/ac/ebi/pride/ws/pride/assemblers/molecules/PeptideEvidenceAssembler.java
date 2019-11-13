package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.IdentifiedModification;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PeptideSpectrumOverview;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideEvidence;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.PeptideEvidenceController;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.SpectraEvidenceController;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideEvidenceResource;
import uk.ac.ebi.pride.ws.pride.transformers.Transformer;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class PeptideEvidenceAssembler extends ResourceAssemblerSupport<PrideMongoPeptideEvidence, PeptideEvidenceResource> {


    private String sortDirection;
    private String sortFields;

    public PeptideEvidenceAssembler(Class<?> controllerClass, Class<PeptideEvidenceResource> resourceType, String sortDirection, String sortFields) {
        super(controllerClass, resourceType);
        this.sortDirection = sortDirection;
        this.sortFields = sortFields;
    }

    @Override
    public PeptideEvidenceResource toResource(PrideMongoPeptideEvidence peptideEvidence) {

        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(PeptideEvidenceController.class)
                        .getPeptideEvidences(peptideEvidence.getProjectAccession(), peptideEvidence.getAssayAccession(), peptideEvidence.getProteinAccession(), peptideEvidence.getPeptideAccession(),"", 10, 0, sortDirection, sortFields))
                .withSelfRel());

        List<String> usis = new ArrayList<>();
        if(peptideEvidence.getPsmAccessions() != null) {
            for(PeptideSpectrumOverview peptideSpectrumOverview: peptideEvidence.getPsmAccessions())
                usis.add(peptideSpectrumOverview.getUsi());
        }

        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(SpectraEvidenceController.class)
                        .getSpectrumBy(usis,peptideEvidence.getProjectAccession(),peptideEvidence.getAssayAccession(),peptideEvidence.getPeptideSequence(),"",  WsContastants.ResultType.FULL, 0, 10, sortDirection, sortFields))
                .withRel(WsContastants.HateoasEnum.psms.name()));


        return new PeptideEvidenceResource(transform(peptideEvidence), links);
    }

    private PeptideEvidence transform(PrideMongoPeptideEvidence mongoPeptide) {
        Set<CvParam> attributes = mongoPeptide.getAdditionalAttributes();

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
                .qualityMethods(mongoPeptide.getQualityEstimationMethods())
                .missedCleavages(mongoPeptide.getMissedCleavages())
                .build();
    }
}
