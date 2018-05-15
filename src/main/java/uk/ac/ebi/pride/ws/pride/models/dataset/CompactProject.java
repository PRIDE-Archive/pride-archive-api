package uk.ac.ebi.pride.ws.pride.models.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import org.springframework.hateoas.core.Relation;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

/**
 * @author ypriverol
 */

@Data
@Builder
@XmlRootElement(name = "project")
@JsonRootName("project")
@JsonTypeName("project")
@Relation(collectionRelation = "compactprojects")
public class CompactProject implements Serializable {

    @XmlElement
    private String accession;
    private String title;
    private Collection<String> additionalAttributes = new ArrayList<>();
    private String projectDescription;
    private String sampleProcessingProtocol;
    private String dataProcessingProtocol;
    private Collection<String> projectTags;
    private Collection<String> keywords;
    private String doi;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date submissionDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date publicationDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date updatedDate;
    private Collection<String> submitters = new ArrayList<>();
    private Collection<String> labPIs = new ArrayList<>();
    private Collection<String> affiliations = new ArrayList<>();
    private Collection<String> instruments = new ArrayList<>();
    private Collection<String> softwares = new ArrayList<>();
    private Collection<String> quantificationMethods = new ArrayList<>();
    private Set<String> countries;
    private Collection<String> sampleAttributes = new ArrayList<>();
    private Collection<String> organisms = new ArrayList<>();
    private Collection<String> organismParts = new ArrayList<>();
    private Collection<String> diseases = new ArrayList<>();
    private Set<String> references = new HashSet<>();
    private Set<String> identifiedPTMStrings = new HashSet<>();
    private Map<String, List<String>> highlights = new HashMap<>();

    public void setAdditionalAttributes(Collection<String> additionalAttributes) {
        if(this.additionalAttributes != null)
            this.additionalAttributes = additionalAttributes;
    }

    public void setProjectTags(Collection<String> projectTags) {
        if(this.projectTags != null)
            this.projectTags = projectTags;
    }

    public void setKeywords(Collection<String> keywords) {
        if(keywords != null)
            this.keywords = keywords;
    }

    public void setSubmitters(Collection<String> submitters) {
        if(submitters != null)
            this.submitters = submitters;
    }

    public void setAffiliations(Collection<String> affiliations) {
        if(affiliations != null)
            this.affiliations = affiliations;
    }

    public void setInstruments(Collection<String> instruments) {
        if(instruments != null)
            this.instruments = instruments;
    }

    public void setQuantificationMethods(Collection<String> quantificationMethods) {
        if(quantificationMethods != null)
            this.quantificationMethods = quantificationMethods;
    }

    public void setCountries(Set<String> countries) {
        if(countries != null)
            this.countries = countries;
    }

    public void setSampleAttributes(Collection<String> sampleAttributes) {
        if(sampleAttributes != null)
            this.sampleAttributes = sampleAttributes;
    }

    public void setOrganisms(Collection<String> organisms) {
        if(organisms != null)
            this.organisms = organisms;
    }

    public void setOrganismParts(Collection<String> organismParts) {
        if(organismParts != null)
            this.organismParts = organismParts;
    }

    public void setDiseases(Collection<String> diseases) {
        if(diseases != null)
            this.diseases = diseases;
    }

    public void setReferences(Set<String> references) {
        if(references != null)
            this.references = references;
    }

    public void setIdentifiedPTMStrings(Set<String> identifiedPTMStrings) {
        if(identifiedPTMStrings != null)
            this.identifiedPTMStrings = identifiedPTMStrings;
    }

    public void setHighlights(Map<String, List<String>> highlights) {
        if(highlights != null)
            this.highlights = highlights;
    }
}
