package uk.ac.ebi.pride.ws.pride.models.dataset;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.Indexed;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * @author ypriverol
 */

@Data
@Builder
public class CompactDataset {
    private String accession;
    private String title;
    private Collection<String> additionalAttributes;
    private String projectDescription;
    private String sampleProcessingProtocol;
    private String dataProcessingProtocol;
    private Collection<String> projectTags;
    private Collection<String> keywords;
    private String doi;
    private Date submissionDate;
    private Date publicationDate;
    private Date updatedDate;
    private Collection<String> submitters;
    private Collection<String> labPIs;
    private Collection<String> affiliations;
    private Collection<String> instruments;
    private Collection<String> softwares;
    private Collection<String> quantificationMethods;
    private Set<String> countries;
    private Collection<String> sampleAttributes;
    private Collection<String> organisms;
    private Collection<String> organismParts;
    private Collection<String> diseases;
    private Set<String> references;
    private Set<String> identifiedPTMStrings;
}
