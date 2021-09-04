package uk.ac.ebi.pride.ws.pride.models.molecules;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProjectForPeptideSummary;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@Data
@Builder
@XmlRootElement(name = "peptidedetails")
@JsonRootName("peptidedetails")
@JsonTypeName("peptidedetails")
public class PeptideDetails {
    private String peptideSequence;
    private String proteinAccession;
    private List<PrideProjectForPeptideSummary> projects;
    private Double bestSearchEngineScore;
    private Integer psmsCount;
    private String[] bestUsis;
    private Map<String, String[]> ptmsMap;

}
