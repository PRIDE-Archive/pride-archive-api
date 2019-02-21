package uk.ac.ebi.pride.ws.pride.models.sample;

import java.util.List;

public class SampleMSRunTable {

    List<SampleMSRunRow> sampleMSRunRows;

    public SampleMSRunTable() {
    }

    public SampleMSRunTable(List<SampleMSRunRow> sampleMSRunRows) {
        this.sampleMSRunRows = sampleMSRunRows;
    }

    public List<SampleMSRunRow> getSampleMSRunRows() {
        return sampleMSRunRows;
    }

    public void setSampleMSRunRows(List<SampleMSRunRow> sampleMSRunRows) {
        this.sampleMSRunRows = sampleMSRunRows;
    }
}
