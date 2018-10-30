package uk.ac.ebi.pride.ws.pride.transformers;

import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.DefaultCvParam;
import uk.ac.ebi.pride.archive.dataprovider.sample.SampleMSRunTuple;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideMSRun;
import uk.ac.ebi.pride.mongodb.archive.model.sample.MongoPrideSample;
import uk.ac.ebi.pride.ws.pride.models.file.PrideMSRun;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;
import uk.ac.ebi.pride.ws.pride.models.sample.Sample;
import uk.ac.ebi.pride.ws.pride.models.sample.SampleMSRun;

import java.util.ArrayList;
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


    public static SampleMSRun transformSampleMSrun(SampleMSRunTuple mongoSampleMSrun){

            SampleMSRunTuple sampleMSRun = mongoSampleMSrun;
            CvParamProvider fractionMongo = sampleMSRun.getFractionIdentifier();
            CvParamProvider labelMongo = sampleMSRun.getSampleLabel();
            CvParamProvider technicalRep = sampleMSRun.getTechnicalReplicateIdentifier();

            // Capture the Fraction information
            CvParam fraction = null;
            if(fractionMongo != null)
                fraction = new CvParam(fractionMongo.getCvLabel(), fractionMongo.getAccession(),fractionMongo.getName(), fractionMongo.getValue());

            //Capture the Labeling
            CvParam label = null;
            if(labelMongo != null)
                label = new CvParam(labelMongo.getCvLabel(), labelMongo.getAccession(),labelMongo.getName(), labelMongo.getValue());

            //Capture the Labeling
            CvParam rep = null;
            if(technicalRep != null)
                rep = new CvParam(technicalRep.getCvLabel(), technicalRep.getAccession(),technicalRep.getName(), technicalRep.getValue());

            return SampleMSRun.builder()
                    .sampleAccession((String) sampleMSRun.getKey())
                    .msRunAccession((String) sampleMSRun.getValue())
                    .fractionIdentifier(fraction)
                    .sampleLabel(label)
                    .technicalReplicateIdentifier(rep)
                    .msRunAccession((String) mongoSampleMSrun.getValue())
                    .additionalProperies((List<Tuple<CvParam, CvParam>>) mongoSampleMSrun.getAdditionalProperties())
                    .build();

    }

    public static Sample transformSample(MongoPrideSample sample) {
        if(sample != null){
            List<Tuple<CvParam, CvParam>> properties = new ArrayList<>();
            if(sample.getProperties() != null)
                properties = sample.getProperties().stream().map(x -> new Tuple<>(new CvParam(x.getKey().getCvLabel(), x.getKey().getAccession(), x.getKey().getName(), x.getKey().getValue()),
                        new CvParam(x.getValue().getCvLabel(), x.getValue().getAccession(), x.getValue().getName(), x.getValue().getValue()))).collect(Collectors.toList());
            return Sample.builder().accession((String) sample.getAccession())
                    .sampleProperties(properties)
                    .build();
        }
        return null;
    }
}
