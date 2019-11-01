package uk.ac.ebi.pride.ws.pride.models.sample;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.sample.ISampleMSRunRow;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 29/10/2018.
 */

@XmlRootElement(name = "sampleMSRun")
@JsonRootName("sampleMSRun")
@JsonTypeName("sampleMSRun")
@Relation(collectionRelation = "sampleMSRuns")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SampleMSRunRow implements ISampleMSRunRow {

    @JsonProperty("projectAccession")
    String projectAccession;

    @JsonProperty("sampleAccession")
    String sampleAccession;

    @JsonProperty("msRunAccession")
    String msRunAccession;

    @JsonProperty("fractionAccession")
    String fractionAccession;

    @JsonProperty("sampleLabel")
    CvParam sampleLabel;

    @JsonProperty("sampleReagent")
    CvParam sampleReagent;

    @JsonProperty("sampleProperties")
    List<Tuple<CvParam, CvParam>> sampleProperties;

    @JsonProperty("msRunProperties")
    List<Tuple<CvParam, CvParam>> msRunProperties;


    public SampleMSRunRow() {
    }

    public SampleMSRunRow(String projectAccession, String sampleAccession, String msRunAccession, String fractionAccession, CvParam sampleReagent, CvParam sampleLabel,
                          List<Tuple<CvParam, CvParam>> sampleProperties, List<Tuple<CvParam, CvParam>> msRunProperties) {
        this.projectAccession = projectAccession;
        this.sampleAccession = sampleAccession;
        this.msRunAccession = msRunAccession;
        this.fractionAccession = fractionAccession;
        this.sampleLabel = (sampleLabel != null)? new CvParam(sampleLabel.getCvLabel(), sampleLabel.getAccession(), sampleLabel.getName(), sampleLabel.getValue()): null;
        this.sampleReagent = (sampleReagent != null)? new CvParam(sampleReagent.getCvLabel(), sampleReagent.getAccession(), sampleReagent.getName(), sampleReagent.getValue()): null;
        this.sampleProperties = (sampleProperties != null)? sampleProperties.stream().map(x ->{
            CvParamProvider key = x.getKey();
            CvParamProvider value = x.getValue();
            return new Tuple<>(new CvParam(key.getCvLabel(), key.getAccession(), key.getName(), key.getValue()), new CvParam(value.getCvLabel(), value.getAccession(), value.getName(), value.getValue()));
        }).collect(Collectors.toList()):null;

        this.msRunProperties = (msRunProperties != null)? msRunProperties.stream().map(x ->{
            CvParamProvider key = x.getKey();
            CvParamProvider value = x.getValue();
            return new Tuple<>(new CvParam(key.getCvLabel(), key.getAccession(), key.getName(), key.getValue()), new CvParam(value.getCvLabel(), value.getAccession(), value.getName(), value.getValue()));
        }).collect(Collectors.toList()):null;

    }

    @Override
    public String getProjectAccession() {
        return projectAccession;
    }

    @Override
    public String getSampleAccession() {
        return sampleAccession;
    }

    @Override
    public String getMsRunAccession() {
        return msRunAccession;
    }

    @Override
    public CvParam getSampleLabel() {
        return sampleLabel;
    }

    @Override
    public CvParam getLabelReagent() {
        return sampleReagent;
    }

    @Override
    public String getFractionAccession() {
        return fractionAccession;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public CvParam getFractionIdentifierCvParam() {
        return new CvParam(CvTermReference.MS_FRACTION_IDENTIFIER.getCvLabel(),CvTermReference.MS_FRACTION_IDENTIFIER.getAccession(), CvTermReference.MS_FRACTION_IDENTIFIER.getName(), fractionAccession);
    }


    @Override
    public List<Tuple<CvParam, CvParam>> getSampleProperties() {
        return sampleProperties;
    }

    @Override
    public List<Tuple<CvParam, CvParam>> getMsRunProperties() {
        return msRunProperties;
    }

    public void setMsRunProperties(List<Tuple<CvParam, CvParam>> msRunProperties) {
        this.msRunProperties = msRunProperties;
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public Comparable getUniqueKey() {
        String result =   (projectAccession != null ? projectAccession + "-" : "");
        result = result + (sampleAccession != null ? sampleAccession + "-":"") ;
        result = result + (msRunAccession != null ? msRunAccession + "-": "");
        result = result + (fractionAccession != null ? fractionAccession + "-":"");
        result = result + (sampleLabel != null ? sampleLabel.getAccession()+"-":"");
        result = result + (sampleReagent != null ? sampleReagent.getAccession():"");
        return result;
    }
}
