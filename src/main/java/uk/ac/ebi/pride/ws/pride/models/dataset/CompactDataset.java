package uk.ac.ebi.pride.ws.pride.models.dataset;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author ypriverol
 */

@Data
@Builder
public class CompactDataset {
    private String accession;
    private String title;
    private List<String> additionalAttributes;
    private String projectDescription;
    private String sampleProcessingProtocol;
    private String dataProcessingProtocol;
    private List<String> projectTags;
    private List<String> keywords;
    private String doi;
    private Date submissionDate;
    private Date publicationDate;
    private Date updatedDate;
    private Set<String> submitters;
    private List<String> labPIs;
    private List<String> affiliations;
    private List<String> instruments;
    private List<String> softwares;
    private List<String> quantificationMethods;
    private Set<String> countries;
    private List<String> sampleAttributes;
    private Set<String> organisms;
    private Set<String> organismParts;
    private Set<String> diseases;
    private Set<String> references;
    private Set<String> identifiedPTMStrings;
}
