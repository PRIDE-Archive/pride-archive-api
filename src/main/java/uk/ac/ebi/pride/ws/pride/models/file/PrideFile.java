package uk.ac.ebi.pride.ws.pride.models.file;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author ypriverol
 */

@Data
@Builder
@XmlRootElement(name = "file")
@JsonRootName("file")
@JsonTypeName("file")
@Relation(collectionRelation = "files")
@AllArgsConstructor
public class PrideFile implements Serializable {
    Set<String> projectAccessions;
    Set<String> analysisAccessions;
    String accession;
    CvParamProvider fileCategory;
    String md5Checksum;
    Set<? extends CvParamProvider> publicFileLocations;
    long fileSizeBytes;
    String fileExtension;
    private String fileName;
    private boolean compress;
    private Date submissionDate;
    private Date publicationDate;
    private Date updatedDate;
    Set<? extends CvParamProvider> additionalAttributes;

}
