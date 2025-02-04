package uk.ac.ebi.pride.ws.pride.models.file;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;

import jakarta.xml.bind.annotation.XmlRootElement;
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
@AllArgsConstructor
public class PrideFile implements Serializable {
    private Set<String> projectAccessions;
    private Set<String> analysisAccessions;
    private String accession;
    private CvParamProvider fileCategory;
    private String checksum;
    private Set<? extends CvParamProvider> publicFileLocations;
    private long fileSizeBytes;
    private String fileExtension;
    private String fileName;
    private boolean compress;
    private Date submissionDate;
    private Date publicationDate;
    private Date updatedDate;
    private Set<? extends CvParamProvider> additionalAttributes;
    private long totalDownloads;
}
