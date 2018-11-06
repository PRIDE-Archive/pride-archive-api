package uk.ac.ebi.pride.ws.pride.utils;

import uk.ac.ebi.pride.solr.indexes.pride.model.PrideProjectField;
import uk.ac.ebi.pride.ws.pride.models.PrideArchiveAPIField;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class helps to transfrom keywords from backend into properties in fronted for highligths and other fiters.
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 02/07/2018.
 */
public enum MappingsConstants {

    ACCESSION(PrideProjectField.ACCESSION,PrideArchiveAPIField.PRIDE_PROJECT_ACCESSION),
    TITLE(PrideProjectField.PROJECT_TILE, PrideArchiveAPIField.PRIDE_PROJECT_TITLE),
    ADDITIONAL_ATTRIBUTES(PrideProjectField.ADDITIONAL_ATTRIBUTES, PrideArchiveAPIField.PRIDE_PROJECT_ADDITIONAL_ATTRIBUTES),
    PROJECT_DESCRIPTION(PrideProjectField.PROJECT_DESCRIPTION, PrideArchiveAPIField.PRIDE_PROJECT_DESCRIPTION),
    PROJECT_SAMPLE_DESCRIPTION(PrideProjectField.PROJECT_SAMPLE_PROTOCOL, PrideArchiveAPIField.PRIDE_PROJECT_SAMPLE_DESCRIPTION),
    PROJECT_DATA_PROCESSING(PrideProjectField.PROJECT_DATA_PROTOCOL, PrideArchiveAPIField.PRIDE_PROJECT_DATA_PROCESSING),
    PROJECT_TAGS(PrideProjectField.PROJECT_TAGS,PrideArchiveAPIField.PRIDE_PROJECT_TAGS ),
    PROJECT_KEYWORDS(PrideProjectField.PROJECT_KEYWORDS, PrideArchiveAPIField.PRIDE_PROJECT_KEYWORDS),
    PROJECT_DOI(PrideProjectField.PROJECT_DOI, PrideArchiveAPIField.PRIDE_PROJECT_DOI),
    PROJECT_SUBMISSION_DATE(PrideProjectField.PROJECT_SUBMISSION_DATE, PrideArchiveAPIField.PRIDE_PROJECT_SUBMISSION_DATE),
    PROJECT_PUBLICATION_DATE(PrideProjectField.PROJECT_PUBLICATION_DATE, PrideArchiveAPIField.PRIDE_PROJECT_PUBLICATION_DATE),
    PROJECT_UPDATED_DATE(PrideProjectField.PROJECT_UPDATED_DATE, PrideArchiveAPIField.PRIDE_PROJECT_UPDATED_DATE),
    PROJECT_SUBMITTERS(PrideProjectField.PROJECT_SUBMITTER, PrideArchiveAPIField.PRIDE_PROJECT_SUBMITTERS),
    PROJECT_LAB_HEADS(PrideProjectField.PROJECT_PI_NAMES, PrideArchiveAPIField.PRIDE_PROJECT_LAB_HEADS),
    PROJECTS_AFFILIATIONS(PrideProjectField.AFFILIATIONS, PrideArchiveAPIField.PRIDE_PROJECTS_AFFILIATIONS),
    PROJECT_INSTRUMENTS(PrideProjectField.INSTRUMENTS, PrideArchiveAPIField.PRIDE_PROJECT_INSTRUMENTS),
    PROJECT_SOFTWARE(PrideProjectField.SOFTWARES, PrideArchiveAPIField.PRIDE_PROJECT_SOFTWARE),
    PROJECT_QUANT_METHODS(PrideProjectField.QUANTIFICATION_METHODS, PrideArchiveAPIField.PRIDE_PROJECT_QUANT_METHODS),
    PROJECT_COUNTRY(PrideProjectField.COUNTRIES, PrideArchiveAPIField.PRIDE_PROJECT_COUNTRY),
    PROJECT_SAMPLE_ATTRIBUTES(PrideProjectField.SAMPLE_ATTRIBUTES_NAMES, PrideArchiveAPIField.PRIDE_PROJECT_SAMPLE_ATTRIBUTES),
    PROJECT_ORGANISM(PrideProjectField.ORGANISM , PrideArchiveAPIField.PRIDE_PROJECT_ORGANISM),
    PROJECT_ORGANISM_PART(PrideProjectField.ORGANISM_PART , PrideArchiveAPIField.PRIDE_PROJECT_ORGANISM_PART),
    PROJECT_DISEASES(PrideProjectField.DISEASES, PrideArchiveAPIField.PRIDE_PROJECT_DISEASES),
    PROJECT_REFERENCES(PrideProjectField.PROJECT_REFERENCES, PrideArchiveAPIField.PRIDE_PROJECT_REFERENCES),
    PROJECT_PTMS (PrideProjectField.PROJECT_IDENTIFIED_PTM_STRING, PrideArchiveAPIField.PRIDE_PROJECT_PTMS);


    private String frontAPIKey;
    private String backendKey;

    MappingsConstants(String backendKey, String frontKey) {
        this.frontAPIKey = frontKey;
        this.backendKey = backendKey;
    }


    public static String backendKeyToAPIKey(String key) {
        for(MappingsConstants value: MappingsConstants.values()){
            if(value.backendKey.equalsIgnoreCase(key))
                return value.frontAPIKey;
        }
        return key;
    }
}
