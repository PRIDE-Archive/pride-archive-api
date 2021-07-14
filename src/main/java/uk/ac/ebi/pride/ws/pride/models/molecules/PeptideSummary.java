package uk.ac.ebi.pride.ws.pride.models.molecules;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.IdentifiedModification;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@XmlRootElement(name = "peptidesummary")
@JsonRootName("peptidesummary")
@JsonTypeName("peptidesummary")
public class PeptideSummary {

    private String peptideSequence;
    private String proteinAccession;
    private String[] projectAccessions;
    private Double bestSearchEngineScore;
    private Integer psmsCount;
    private String[] bestUsis;
    private Map<String, String[]> ptmsMap;

}
