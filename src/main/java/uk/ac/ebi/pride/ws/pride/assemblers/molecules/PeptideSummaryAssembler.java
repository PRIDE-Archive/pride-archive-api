package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideSummary;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideSummary;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideSummaryResource;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.Collections;


public class PeptideSummaryAssembler extends ResourceAssemblerSupport<PrideMongoPeptideSummary, PeptideSummaryResource> {

    public PeptideSummaryAssembler(Class<?> controllerClass, Class<PeptideSummaryResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public PeptideSummaryResource toResource(PrideMongoPeptideSummary peptideSummary) {
        return new PeptideSummaryResource(transform(peptideSummary), Collections.emptyList());
    }

    private PeptideSummary transform(PrideMongoPeptideSummary mongoPeptideSummary) {

        return PeptideSummary.builder()
                .peptideSequence(mongoPeptideSummary.getPeptideSequence())
                .proteinAccession(mongoPeptideSummary.getProteinAccession())
                .projectAccessions(mongoPeptideSummary.getProjectAccessions())
                .bestSearchEngineScore(mongoPeptideSummary.getBestSearchEngineScore())
                .psmsCount(mongoPeptideSummary.getPsmsCount())
                .bestUsis(mongoPeptideSummary.getBestUsis())
                .ptmsMap(WsUtils.peptideSummaryEnhancePtmsMap(mongoPeptideSummary))
                .build();
    }

}
