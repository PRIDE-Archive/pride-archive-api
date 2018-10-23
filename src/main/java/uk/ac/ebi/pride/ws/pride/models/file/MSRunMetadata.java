package uk.ac.ebi.pride.archive.dataprovider.msrun;

import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;

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

public class MSRunMetadata {

    // File Properties in CvTerms
    List<CvParamProvider> fileProperties;

    // Instruments Properties
    List<CvParamProvider> instrumentProperties;

    // MS Data Properties
    List<CvParamProvider> msData;

    // Scan MS Properties
    List<CvParamProvider> scanSettings;

    public MSRunMetadata() {
    }

    public MSRunMetadata(List<CvParamProvider> fileProperties,
                         List<CvParamProvider> instrumentProperties,
                         List<CvParamProvider> msData,
                         List<CvParamProvider> scanSettings) {
        this.fileProperties = fileProperties;
        this.instrumentProperties = instrumentProperties;
        this.msData = msData;
        this.scanSettings = scanSettings;
    }

    public void setFileProperties(List<CvParamProvider> fileProperties) {
        this.fileProperties = fileProperties;
    }

    public void setInstrumentProperties(List<CvParamProvider> instrumentProperties) {
        this.instrumentProperties = instrumentProperties;
    }

    public void setMsData(List<CvParamProvider> msData) {
        this.msData = msData;
    }

    public void setScanSettings(List<CvParamProvider> scanSettings) {
        this.scanSettings = scanSettings;
    }

    public List<CvParamProvider> getFileProperties() {
        return fileProperties;
    }

    public List<CvParamProvider> getInstrumentProperties() {
        return instrumentProperties;
    }

    public List<CvParamProvider> getMsData() {
        return msData;
    }

    public List<CvParamProvider> getScanSettings() {
        return scanSettings;
    }
}
