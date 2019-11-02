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
import java.util.Set;

@Data
@Builder
@XmlRootElement(name = "peptideevidence")
@JsonRootName("peptideevidence")
@JsonTypeName("peptideevidence")
@Relation(collectionRelation = "peptideevidences")
public class PeptideEvidence {

    String accession;
    String projectAccession;
    String assayAccession;
    String proteinAccession;
    String peptideSequence;
    Set<CvParam> properties;
    List<IdentifiedModification> ptms;
    CvParam bestSearchEngineScore;
    private boolean isDecoy;
    Integer startPostion;
    Integer endPostion;
    Integer missedCleavages;
    Boolean isValid;
    Set<CvParam> qualityMethods;

}
