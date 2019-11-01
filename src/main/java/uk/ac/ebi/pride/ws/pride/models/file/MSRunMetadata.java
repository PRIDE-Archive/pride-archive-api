package uk.ac.ebi.pride.ws.pride.models.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ebi.pride.archive.dataprovider.msrun.MsRunProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.ParamProvider;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.archive.dataprovider.msrun.idsettings.IdSetting;

import java.util.Collection;
import java.util.Collections;
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
 * @author ypriverol on 22/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MSRunMetadata implements MsRunProvider {

    // File Properties in CvTerms
    @JsonProperty(PrideArchiveField.MS_RUN_FILE_PROPERTIES)
    Set<CvParam> fileProperties;

    // Instruments Properties
    @JsonProperty(PrideArchiveField.MS_RUN_INSTRUMENT_PROPERTIES)
    Set<CvParam> instrumentProperties;

    // MS Data Properties
    @JsonProperty(PrideArchiveField.MS_RUN_MS_DATA)
    Set<CvParam> msData;

    // Scan MS Properties
    @JsonProperty(PrideArchiveField.MS_RUN_SCAN_SETTINGS)
    Set<CvParam> scanSettings;

    // Scan ID Settings
    @JsonProperty(PrideArchiveField.MS_RUN_ID_SETTINGS)
    Set<IdSetting> idSettings;

    public MSRunMetadata() {
    }

    public MSRunMetadata(Set<CvParam> fileProperties,
                         Set<CvParam> instrumentProperties,
                         Set<CvParam> msData,
                         Set<CvParam> scanSettings,
                         Set<IdSetting> idSettings) {
        this.fileProperties = fileProperties;
        this.instrumentProperties = instrumentProperties;
        this.msData = msData;
        this.scanSettings = scanSettings;
        this.idSettings = idSettings;
    }

    public void setFileProperties(Set<CvParam> fileProperties) {
        this.fileProperties = fileProperties;
    }

    public void setInstrumentProperties(Set<CvParam> instrumentProperties) {
        this.instrumentProperties = instrumentProperties;
    }

    public void setMsData(Set<CvParam> msData) {
        this.msData = msData;
    }

    public void setScanSettings(Set<CvParam> scanSettings) {
        this.scanSettings = scanSettings;
    }

    public Set<CvParam> getFileProperties() {
        return fileProperties;
    }

    public Set<CvParam> getInstrumentProperties() {
        return instrumentProperties;
    }

    public Set<CvParam> getMsData() {
        return msData;
    }

    public Collection<? extends CvParamProvider> getScanSettings() {
        return scanSettings;
    }

    public Set<IdSetting> getIdSettings() {
        return idSettings;
    }

    public void setIdSettings(Set<IdSetting> idSettings) {
        this.idSettings = idSettings;
    }

    //@Override
    public Collection<? extends String> getAdditionalAttributesStrings() {
        return Collections.EMPTY_LIST;
    }

    //@Override
    public Collection<? extends ParamProvider> getParams() {
        return null;
    }
}
