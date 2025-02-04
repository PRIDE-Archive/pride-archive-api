package uk.ac.ebi.pride.ws.pride.models.dataset;

import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.archive.dataprovider.param.ParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.reference.Reference;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PrideProjectForPeptideSummary {
//    private String accession;
//    private String title;
//    private List<Integer> pubmedIds;
//    private Collection<String> instruments;
//    private List<String> diseases;
//    private List<String> tissues;
//
//    public static PrideProjectForPeptideSummary fromMongoPrideProject(MongoPrideProject mongoPrideProject) {
//        List<Integer> pubmedIds = null;
//        try {
//            pubmedIds = mongoPrideProject.getReferencesWithPubmed().stream().map(Reference::getPubmedId)
//                    .filter(i -> i > 0).collect(Collectors.toList());
//        } catch (Exception e) {
//            //nothing_todo
//        }
//        List<String> diseases = null;
//        try {
//            diseases = WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_DISEASE)
//                    .stream().map(ParamProvider::getName).collect(Collectors.toList());
//        } catch (Exception e) {
//            //nothing_todo
//        }
//        List<String> tissues = null;
//        try {
//            tissues = WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_ORGANISM_PART)
//                    .stream().filter(s -> s.getAccession().startsWith("BTO"))
//                    .map(ParamProvider::getName).collect(Collectors.toList());
//        } catch (Exception e) {
//            //nothing_todo
//        }
//        return PrideProjectForPeptideSummary.builder()
//                .accession(mongoPrideProject.getAccession())
//                .title(mongoPrideProject.getTitle())
//                .pubmedIds(pubmedIds)
//                .instruments(mongoPrideProject.getInstruments())
//                .diseases(diseases)
//                .tissues(tissues)
//                .build();
//    }
}
