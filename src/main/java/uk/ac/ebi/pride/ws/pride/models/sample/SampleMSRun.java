package uk.ac.ebi.pride.ws.pride.models.sample;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.archive.dataprovider.common.ITuple;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.sample.SampleMSRunTuple;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.List;

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
@Data
@Builder
@XmlRootElement(name = "sampleMSRun")
@JsonRootName("sampleMSRun")
@JsonTypeName("sampleMSRun")
@Relation(collectionRelation = "sampleMSRuns")
public class SampleMSRun implements SampleMSRunTuple {

    String sampleAccession;
    String msRunAccession;
    CvParam sampleLabel;
    CvParam fractionIdentifier;
    CvParam technicalReplicateIdentifier;
    List<Tuple<CvParam, CvParam>> additionalProperies;

    @Override
    public Comparable getKey() {
        return sampleAccession;
    }

    @Override
    public Comparable getValue() {
        return msRunAccession;
    }

    @Override
    public CvParamProvider getSampleLabel() {
        return sampleLabel;
    }

    @Override
    public CvParamProvider getFractionIdentifier() {
        return fractionIdentifier;
    }

    @Override
    public CvParamProvider getTechnicalReplicateIdentifier() {
        return technicalReplicateIdentifier;
    }

    @Override
    public Collection<? extends ITuple<? extends CvParamProvider, ? extends CvParamProvider>> getAdditionalProperties() {
        return additionalProperies;
    }
}
