package uk.ac.ebi.pride.ws.pride.transformers;

import uk.ac.ebi.pride.archive.dataprovider.param.DefaultCvParam;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideMSRun;
import uk.ac.ebi.pride.ws.pride.models.file.PrideMSRun;

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
public class Transformer {

    /**
     * Transform a Mongo {@link MongoPrideMSRun} to a Web service {@link PrideMSRun}
     * @param mongoFile msRun from mongo database
     * @return msrun
     */
    public static PrideMSRun transformMSRun(MongoPrideMSRun mongoFile){
        PrideMSRun msRun = new PrideMSRun(mongoFile.getProjectAccessions(), mongoFile.getAnalysisAccessions(), mongoFile.getAccession(), mongoFile.getFileCategory(), mongoFile.getMd5Checksum(), mongoFile.getPublicFileLocations(), mongoFile.getFileSizeBytes(), mongoFile.getFileExtension(), mongoFile.getFileName(), mongoFile.isCompress(), mongoFile.getSubmissionDate(), mongoFile.getPublicationDate(), mongoFile.getUpdatedDate(), mongoFile.getAdditionalAttributes());

        if(mongoFile.getFileProperties() != null)
            msRun.setFileProperties(mongoFile.getFileProperties()
                    .stream().map(x-> new DefaultCvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));
        if(mongoFile.getInstrumentProperties() != null)
            msRun.setInstrumentProperties(mongoFile.getInstrumentProperties()
                    .stream().map(x-> new DefaultCvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));
        if(mongoFile.getMsData() != null)
            msRun.setMsData(mongoFile.getMsData()
                    .stream().map(x-> new DefaultCvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));
        if(mongoFile.getScanSettings() != null)
            msRun.setScanSettings(mongoFile.getScanSettings()
                    .stream().map(x-> new DefaultCvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));

        return msRun;
    }
}
