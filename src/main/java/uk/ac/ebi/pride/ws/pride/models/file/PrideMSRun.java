package uk.ac.ebi.pride.ws.pride.models.file;

import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 24/10/2018.
 */

public class PrideMSRun extends PrideFile{

    Set<CvParamProvider> fileProperties = new HashSet<>();
    Set<CvParamProvider> instrumentProperties = new HashSet<>();
    Set<CvParamProvider> msData = new HashSet<>();
    Set<CvParamProvider> scanSettings = new HashSet<>();

    public PrideMSRun(Set<String> projectAccessions, Set<String> analysisAccessions, String accession, CvParamProvider fileCategory, String md5Checksum, List<? extends CvParamProvider> publicFileLocations, long fileSizeBytes, String fileExtension, String fileName, boolean compress, Date submissionDate, Date publicationDate, Date updatedDate, List<? extends CvParamProvider> additionalAttributes) {
        super(projectAccessions, analysisAccessions, accession, fileCategory, md5Checksum, publicFileLocations, fileSizeBytes, fileExtension, fileName, compress, submissionDate, publicationDate, updatedDate, additionalAttributes);
    }

    public void setFileProperties(Set<CvParamProvider> fileProperties) {
        this.fileProperties = fileProperties;
    }

    public void setInstrumentProperties(Set<CvParamProvider> instrumentProperties) {
        this.instrumentProperties = instrumentProperties;
    }

    public void setMsData(Set<CvParamProvider> msData) {
        this.msData = msData;
    }

    public void setScanSettings(Set<CvParamProvider> scanSettings) {
        this.scanSettings = scanSettings;
    }

    public Set<CvParamProvider> getFileProperties() {
        return fileProperties;
    }

    public Set<CvParamProvider> getInstrumentProperties() {
        return instrumentProperties;
    }

    public Set<CvParamProvider> getMsData() {
        return msData;
    }

    public Set<CvParamProvider> getScanSettings() {
        return scanSettings;
    }
}
