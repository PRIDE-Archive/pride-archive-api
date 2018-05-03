package uk.ac.ebi.pride.ws.pride.models.dataset;

import uk.ac.ebi.pride.archive.dataprovider.dataset.DatasetProvider;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 03/05/2018.
 */
public class CompactDataset implements DatasetProvider {

    // Dataset Accession
    public String accession;


    @Override
    public String getAccession() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getProjectDescription() {
        return null;
    }

    @Override
    public String getSampleProcessingProtocol() {
        return null;
    }

    @Override
    public String getDataProcessingProtocol() {
        return null;
    }

    @Override
    public Collection<? extends String> getSubmitters() {
        return null;
    }

    @Override
    public Collection<? extends String> getHeadLab() {
        return null;
    }

    @Override
    public Collection<? extends String> getKeywords() {
        return null;
    }

    @Override
    public Collection<? extends String> getProjectTags() {
        return null;
    }

    @Override
    public Collection<? extends String> getPtms() {
        return null;
    }

    @Override
    public Collection<? extends String> getInstruments() {
        return null;
    }

    @Override
    public Collection<? extends String> getSoftwares() {
        return null;
    }

    @Override
    public Collection<? extends String> getQuantificationMethods() {
        return null;
    }

    @Override
    public Collection<? extends String> getReferences() {
        return null;
    }

    @Override
    public Optional<String> getDoi() {
        return Optional.empty();
    }

    @Override
    public Collection<? extends String> getOtherOmicsLink() {
        return null;
    }

    @Override
    public boolean isPublicProject() {
        return false;
    }

    @Override
    public String getSubmissionType() {
        return null;
    }

    @Override
    public Date getSubmissionDate() {
        return null;
    }

    @Override
    public Date getPublicationDate() {
        return null;
    }

    @Override
    public Date getUpdateDate() {
        return null;
    }

    @Override
    public Collection<? extends String> getExperimentalFactors() {
        return null;
    }

    @Override
    public Collection<? extends String> getCountries() {
        return null;
    }

    @Override
    public Collection<? extends String> getAllAffiliations() {
        return null;
    }

    @Override
    public Collection<? extends String> getSampleAttributes() {
        return null;
    }

    @Override
    public Comparable getId() {
        return null;
    }

    @Override
    public Collection<? extends String> getAdditionalAttributes() {
        return null;
    }
}
