package uk.ac.ebi.pride.ws.pride.models.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.ws.pride.models.PrideArchiveAPIField;
import uk.ac.ebi.pride.ws.pride.utils.MappingsConstants;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ypriverol
 */

@Data
@Builder
@XmlRootElement(name = "project")
@JsonRootName("project")
@JsonTypeName("project")
@Relation(collectionRelation = "compactprojects")
public class CompactProject implements Serializable, PrideArchiveAPIField {

    @XmlElement
    @JsonProperty(PRIDE_PROJECT_ACCESSION)
    private String accession;

    @JsonProperty(PRIDE_PROJECT_TITLE)
    private String title;

    @JsonProperty(PRIDE_PROJECT_ADDITIONAL_ATTRIBUTES)
    private Collection<String> additionalAttributes = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_DESCRIPTION)
    private String projectDescription;

    @JsonProperty(PRIDE_PROJECT_SAMPLE_DESCRIPTION)
    private String sampleProcessingProtocol;

    @JsonProperty(PRIDE_PROJECT_DATA_PROCESSING)
    private String dataProcessingProtocol;

    @JsonProperty(PRIDE_PROJECT_TAGS)
    private Collection<String> projectTags;

    @JsonProperty(PRIDE_PROJECT_KEYWORDS)
    private Collection<String> keywords;

    @JsonProperty(PRIDE_PROJECT_DOI)
    private String doi;

    @JsonProperty(PRIDE_PROJECT_SUBMISSION_DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date submissionDate;

    @JsonProperty(PRIDE_PROJECT_PUBLICATION_DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date publicationDate;

    @JsonProperty(PRIDE_PROJECT_UPDATED_DATE)
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date updatedDate;

    @JsonProperty(PRIDE_PROJECT_SUBMITTERS)
    private Collection<String> submitters = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_LAB_HEADS)
    private Collection<String> labPIs = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECTS_AFFILIATIONS)
    private Collection<String> affiliations = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_INSTRUMENTS)
    private Collection<String> instruments = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_SOFTWARE)
    private Collection<String> softwares = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_QUANT_METHODS)
    private Collection<String> quantificationMethods = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_COUNTRY)
    private Set<String> countries;

    @JsonProperty(PRIDE_PROJECT_SAMPLE_ATTRIBUTES)
    private Collection<String> sampleAttributes = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_ORGANISM)
    private Collection<String> organisms = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_ORGANISM_PART)
    private Collection<String> organismParts = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_DISEASES)
    private Collection<String> diseases = new ArrayList<>();

    @JsonProperty(PRIDE_PROJECT_REFERENCES)
    private Set<String> references = new HashSet<>();

    @JsonProperty(PRIDE_PROJECT_PTMS)
    private Set<String> identifiedPTMS = new HashSet<>();

    @JsonProperty(QUERY_SCORE)
    private Double queryScore;

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

    public void setIdentifiedPTM(Set<String> identifiedPTMStrings) {
        if(identifiedPTMStrings != null)
            this.identifiedPTMS = identifiedPTMStrings;
    }

    public void setQueryScore(Double queryScore) {
        this.queryScore = queryScore;
    }

    public void setHighlights(Map<String, List<String>> highlights) {

        if(highlights != null)
            this.highlights = transform(highlights);
    }

    private Map<String, List<String>> transform(Map<String, List<String>> highlights) {

        if(highlights != null && highlights.size() > 0){
            highlights = highlights.entrySet()
                    .stream()
                    .collect(Collectors
                            .toMap(e -> MappingsConstants.backendKeyToAPIKey(e.getKey()), Map.Entry::getValue));
        }
        return highlights;
    }
}
