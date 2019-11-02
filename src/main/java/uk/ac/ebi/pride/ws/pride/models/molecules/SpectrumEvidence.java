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
@XmlRootElement(name = "spectrumevidence")
@JsonRootName("spectrumevidence")
@JsonTypeName("spectrumevidence")
@Relation(collectionRelation = "spectraevidences")
public class SpectrumEvidence {

    String usi;
    Double[] mzs;
    Double[] intensities;
    int numPeaks;
    Set<CvParam> attributes;
    String peptideSequence;
    List<IdentifiedModification> ptms;
    boolean isDecoy;
    boolean isValid;
    Set<CvParam> qualityMethods;
    Integer charge;
    Double precursorMZ;
}
