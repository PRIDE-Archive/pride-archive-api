package uk.ac.ebi.pride.ws.pride.models.dataset;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.reference.ReferenceProvider;
import uk.ac.ebi.pride.archive.dataprovider.user.ContactProvider;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 25/05/2018.
 */
@Data
@Builder
@XmlRootElement(name = "project")
@JsonRootName("project")
@JsonTypeName("project")
@Relation(collectionRelation = "projects")
public class PrideProject {
    @XmlElement
    private String accession;
    private String title;
    private Collection<? extends CvParamProvider> additionalAttributes = new ArrayList<>();
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
    private Collection<ContactProvider> submitters = new ArrayList<>();
    private Collection<ContactProvider> labPIs = new ArrayList<>();
    private Collection<String> affiliations = new ArrayList<>();
    private Collection<CvParamProvider> instruments = new ArrayList<>();
    private Collection<CvParamProvider> softwares = new ArrayList<>();
    private Collection<CvParamProvider> quantificationMethods = new ArrayList<>();
    private Set<String> countries;
    private Collection<CvParamProvider> sampleAttributes = new ArrayList<>();
    private Collection<CvParamProvider> organisms = new ArrayList<>();
    private Collection<CvParamProvider> organismParts = new ArrayList<>();
    private Collection<CvParamProvider> diseases = new ArrayList<>();
    private Set<ReferenceProvider> references = new HashSet<>();
    private Set<CvParamProvider> identifiedPTMStrings = new HashSet<>();
}
