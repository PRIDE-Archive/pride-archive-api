package uk.ac.ebi.pride.ws.pride.models.dataset;

import lombok.Data;

import java.util.Collection;

@Data
public class PrideProjectMetadata {

    private String accession;
    private String title;
    private  String submissionType;
    private String description;
    private String sampleProcessingProtocol;
    private String dataProcessingProtocol;

    public PrideProjectMetadata(String accession, String title, String submissionType, String description, String sampleProcessingProtocol, String dataProcessingProtocol) {
        this.accession = accession;
        this.title = title;
        this.submissionType = submissionType;
        this.description = description;
        this.sampleProcessingProtocol = sampleProcessingProtocol;
        this.dataProcessingProtocol = dataProcessingProtocol;
    }
}
