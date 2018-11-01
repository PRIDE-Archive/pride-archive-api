package uk.ac.ebi.pride.ws.pride.models.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 23/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CvParam implements CvParamProvider {

    String cvLabel;
    String accession;
    String name;
    String value;

    public CvParam(String cvLabel, String accession, String name, String value) {
        this.cvLabel = cvLabel;
        this.accession = accession;
        this.name = name;
        this.value = value;
    }

    @Override
    public String getCvLabel() {
        return cvLabel;
    }

    @Override
    public String getAccession() {
        return accession;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    @JsonIgnore
    public Comparable getId() {
        return accession;
    }
}
