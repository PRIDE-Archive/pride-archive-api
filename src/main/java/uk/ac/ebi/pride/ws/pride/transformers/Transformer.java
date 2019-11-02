package uk.ac.ebi.pride.ws.pride.transformers;

import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.IdentifiedModification;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.IdentifiedModificationProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.sample.ISampleMSRunRow;
import uk.ac.ebi.pride.archive.dataprovider.sample.SampleProvider;
import uk.ac.ebi.pride.mongodb.archive.model.msrun.MongoPrideMSRun;
import uk.ac.ebi.pride.ws.pride.models.file.PrideMSRun;
import uk.ac.ebi.pride.ws.pride.models.sample.Sample;
import uk.ac.ebi.pride.ws.pride.models.sample.SampleMSRunRow;

import java.util.*;
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
        PrideMSRun msRun = new PrideMSRun(mongoFile.getProjectAccessions(), mongoFile.getAnalysisAccessions(), mongoFile.getAccession(), null, null, null, mongoFile.getFileSizeBytes(), null, mongoFile.getFileName(), false,null,null,null, mongoFile.getAdditionalAttributes());

        if(mongoFile.getFileProperties() != null)
            msRun.setFileProperties(mongoFile.getFileProperties()
                    .stream().map(x-> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));
        if(mongoFile.getInstrumentProperties() != null)
            msRun.setInstrumentProperties(mongoFile.getInstrumentProperties()
                    .stream().map(x-> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));
        if(mongoFile.getMsData() != null)
            msRun.setMsData(mongoFile.getMsData()
                    .stream().map(x-> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));
        if(mongoFile.getScanSettings() != null)
            msRun.setScanSettings(mongoFile.getScanSettings()
                    .stream().map(x-> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                    .collect(Collectors.toSet()));
        if(mongoFile.getIdSettings() != null)
            msRun.setIdSettings(new ArrayList<>(mongoFile.getIdSettings()));

        return msRun;
    }


    public static SampleMSRunRow transformSampleMSrun(ISampleMSRunRow mongoSampleMSrun){

        CvParamProvider fractionMongo = mongoSampleMSrun.getFractionIdentifierCvParam();
        CvParamProvider labelMongo = mongoSampleMSrun.getSampleLabel();

        // Capture the Fraction information
        CvParam fraction = null;
        if(fractionMongo != null)
            fraction = new CvParam(fractionMongo.getCvLabel(), fractionMongo.getAccession(),fractionMongo.getName(), fractionMongo.getValue());

        //Capture the Labeling
        CvParam label = null;
        if(labelMongo != null)
            label = new CvParam(labelMongo.getCvLabel(), labelMongo.getAccession(),labelMongo.getName(), labelMongo.getValue());

        //Capture the Labeling
        CvParam reagent = null;
        if (mongoSampleMSrun.getLabelReagent() != null)
            reagent = new CvParam(mongoSampleMSrun.getLabelReagent().getCvLabel(), mongoSampleMSrun.getLabelReagent().getAccession(), mongoSampleMSrun.getLabelReagent().getName(), mongoSampleMSrun.getLabelReagent().getValue());

        List<Tuple<CvParam, CvParam>> sampleProperties = (mongoSampleMSrun.getSampleProperties() != null)? mongoSampleMSrun.getSampleProperties()
                .stream().map( x-> {
                    CvParamProvider key = (CvParamProvider) x.getKey();
                    CvParamProvider value = (CvParamProvider) x.getValue();
                    return new Tuple<>(new CvParam(key.getCvLabel(), key.getAccession(), key.getName(), key.getValue()),
                            new CvParam(value.getCvLabel(), value.getAccession(), value.getName(), value.getValue()));
                })
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<Tuple<CvParam, CvParam>> msrunProperties = (mongoSampleMSrun.getMsRunProperties() != null)? mongoSampleMSrun.getMsRunProperties()
                .stream().map( x-> {
                    CvParamProvider key = (CvParamProvider) x.getKey();
                    CvParamProvider value = (CvParamProvider) x.getValue();
                    return new Tuple<>(new CvParam(key.getCvLabel(), key.getAccession(), key.getName(), key.getValue()),
                            new CvParam(value.getCvLabel(), value.getAccession(), value.getName(), value.getValue()));
                })
                .collect(Collectors.toList())
                : Collections.emptyList();


        return new SampleMSRunRow(mongoSampleMSrun.getProjectAccession(), mongoSampleMSrun.getSampleAccession(),
                mongoSampleMSrun.getMsRunAccession(), mongoSampleMSrun.getFractionAccession(),reagent,
                label, sampleProperties, msrunProperties);

    }

    public static Sample transformSample(SampleProvider sample) {
        if(sample != null){
            List<Tuple<CvParam, CvParam>> properties = new ArrayList<>();
            if(sample.getSampleProperties() != null)
                properties = sample.getSampleProperties().stream().map(x -> new Tuple<>(new CvParam(x.getKey().getCvLabel(), x.getKey().getAccession(), x.getKey().getName(), x.getKey().getValue()),
                        new CvParam(x.getValue().getCvLabel(), x.getValue().getAccession(), x.getValue().getName(), x.getValue().getValue()))).collect(Collectors.toList());
            return Sample.builder().accession((String) sample.getAccession())
                    .sampleProperties(properties)
                    .build();
        }
        return null;
    }

    /**
     * Transform a List of {@link IdentifiedModificationProvider} to a List of {@link IdentifiedModification}
     * @param oldPtms List of {@link IdentifiedModificationProvider}
     * @return List of {@link IdentifiedModification}
     */
    public static List<IdentifiedModification> transformModifications(Collection<? extends IdentifiedModificationProvider> oldPtms){
        return oldPtms.stream().map(ptm -> {
            CvParam ptmName = new CvParam(ptm.getModificationCvTerm().getCvLabel(),
                    ptm.getModificationCvTerm().getAccession(),
                    ptm.getModificationCvTerm().getName(),
                    ptm.getModificationCvTerm().getValue());

            CvParam neutral = null;
            if(ptm.getNeutralLoss() != null)
                neutral = new CvParam(ptm.getNeutralLoss().getCvLabel(),
                        ptm.getNeutralLoss().getAccession(),
                        ptm.getNeutralLoss().getName(),
                        ptm.getNeutralLoss().getValue());

            List<Tuple<Integer, Set<? extends CvParamProvider>>> ptmPositions = ptm.getPositionMap().stream().map(position ->{
                Collection<CvParamProvider> scores = (Collection<CvParamProvider>) position.getValue();
                Integer currentPosition = position.getKey();
                Set<CvParam> newScores = scores.stream().map(score -> new CvParam(score.getCvLabel(),
                        score.getAccession(), score.getName(),
                        score.getValue())).collect(Collectors.toSet());
                return new Tuple<Integer, Set<? extends CvParamProvider>>(currentPosition, newScores);
            }).collect(Collectors.toList());

            IdentifiedModification newPTM = new IdentifiedModification(neutral, ptmPositions, ptmName, new HashSet<>());

            return newPTM;
        }).collect(Collectors.toList());
    }

}
