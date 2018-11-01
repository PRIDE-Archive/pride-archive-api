package uk.ac.ebi.pride.ws.pride.models.sample;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.core.Relation;
import uk.ac.ebi.pride.archive.dataprovider.common.ITuple;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.sample.SampleProvider;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
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
@Data
@Builder
@XmlRootElement(name = "sample")
@JsonRootName("sample")
@JsonTypeName("sample")
@Relation(collectionRelation = "samples")
public class Sample implements SampleProvider {

    public String accession;
    public Collection<Tuple<CvParam, CvParam>> sampleProperties = new ArrayList<>();

    @Override
    public Comparable getAccession() {
        return accession;
    }

    @Override
    public Collection<ITuple<CvParamProvider, CvParamProvider>> getSampleProperties() {
        if(sampleProperties != null)
            sampleProperties.stream().map( x-> new Tuple<CvParamProvider, CvParamProvider>(x.getKey(), x.getValue())).collect(Collectors.toList());
        return null;
    }
}
