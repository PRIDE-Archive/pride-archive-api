package uk.ac.ebi.pride.ws.pride.models.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ebi.pride.archive.dataprovider.msrun.MsRunProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;

import java.util.Collection;
import java.util.Collections;
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
 * @author ypriverol on 22/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MSRunMetadata implements MsRunProvider {

    // File Properties in CvTerms
    @JsonProperty("fileProperties")
    List<CvParam> fileProperties;

    // Instruments Properties
    @JsonProperty("instrumentProperties")
    List<CvParam> instrumentProperties;

    // MS Data Properties
    @JsonProperty("msData")
    List<CvParam> msData;

    // Scan MS Properties
    @JsonProperty("scanSettings")
    List<CvParam> scanSettings;

    public MSRunMetadata() {
    }

    public MSRunMetadata(List<CvParam> fileProperties,
                         List<CvParam> instrumentProperties,
                         List<CvParam> msData,
                         List<CvParam> scanSettings) {
        this.fileProperties = fileProperties;
        this.instrumentProperties = instrumentProperties;
        this.msData = msData;
        this.scanSettings = scanSettings;
    }

    public void setFileProperties(List<CvParam> fileProperties) {
        this.fileProperties = fileProperties;
    }

    public void setInstrumentProperties(List<CvParam> instrumentProperties) {
        this.instrumentProperties = instrumentProperties;
    }

    public void setMsData(List<CvParam> msData) {
        this.msData = msData;
    }

    public void setScanSettings(List<CvParam> scanSettings) {
        this.scanSettings = scanSettings;
    }

    public List<CvParam> getFileProperties() {
        return fileProperties;
    }

    public List<CvParam> getInstrumentProperties() {
        return instrumentProperties;
    }

    public List<CvParam> getMsData() {
        return msData;
    }

    public Collection<? extends CvParamProvider> getScanSettings() {
        return scanSettings;
    }

    @Override
    public Collection<? extends String> getAdditionalAttributesStrings() {
        return Collections.EMPTY_LIST;
    }
}
