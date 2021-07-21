package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.molecules.model.peptide.PrideMongoPeptideSummary;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideSummary;
import uk.ac.ebi.pride.ws.pride.models.molecules.PeptideSummaryResource;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


public class PeptideSummaryAssembler extends ResourceAssemblerSupport<PrideMongoPeptideSummary, PeptideSummaryResource> {

    public PeptideSummaryAssembler(Class<?> controllerClass, Class<PeptideSummaryResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public PeptideSummaryResource toResource(PrideMongoPeptideSummary peptideSummary) {
        return new PeptideSummaryResource(transform(peptideSummary), Collections.emptyList());
    }

    private PeptideSummary transform(PrideMongoPeptideSummary mongoPeptideSummary) {

        Map<String, String[]> ptmsMap = mongoPeptideSummary.getPtmsMap();
        ModReader modReader = ModReader.getInstance();
        Map<String, String[]> ptmsMapModified = ptmsMap.entrySet().stream()
                .filter(e -> !e.getKey().contains(":,")) //to filter out cases where key has invalid PTM i.e., "UNIMOD:, 4"
                .collect(Collectors.toMap(e -> {
                    String[] split = e.getKey().split(",");
                    String mod = split[0];
                    String position = split[1];
                    String name;
                    try {
                        name = modReader.getPTMbyAccession(mod).getName();
                    } catch (Exception ex) { //to handle cases where PTM is not found
                        return e.getKey();
                    }
                    return mod + "(" + name + ")," + position;
                }, Map.Entry::getValue));

        return PeptideSummary.builder()
                .peptideSequence(mongoPeptideSummary.getPeptideSequence())
                .proteinAccession(mongoPeptideSummary.getProteinAccession())
                .projectAccessions(mongoPeptideSummary.getProjectAccessions())
                .bestSearchEngineScore(mongoPeptideSummary.getBestSearchEngineScore())
                .psmsCount(mongoPeptideSummary.getPsmsCount())
                .bestUsis(mongoPeptideSummary.getBestUsis())
                .ptmsMap(ptmsMapModified)
                .build();
    }

}
