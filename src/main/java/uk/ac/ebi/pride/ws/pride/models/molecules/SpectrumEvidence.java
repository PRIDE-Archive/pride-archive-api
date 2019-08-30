package uk.ac.ebi.pride.ws.pride.models.molecules;


import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@Builder
@XmlRootElement(name = "spectrumevidence")
@JsonRootName("spectrumevidence")
@JsonTypeName("spectrumevidence")
@Relation(collectionRelation = "spectraevidences")
public class SpectrumEvidence {

    String usi;
    Double[] mzs;
    Double[] intensities;
    int numPeaks;
    List<CvParam> attributes;
    String peptideSequence;
    List<IdentifiedModification> ptms;
    boolean isDecoy;
    boolean isValid;
    List<CvParam> qualityMethods;
    Integer charge;
    Double precursorMZ;
}
