package uk.ac.ebi.pride.ws.test.integration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.msrun.MongoPrideMSRun;
import uk.ac.ebi.pride.mongodb.archive.repo.msruns.PrideMSRunMongoRepository;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.msruns.PrideMsRunMongoService;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestService {

    @Autowired
    private PrideFileMongoService mongoFileService;

    @Autowired
    private PrideMSRunMongoRepository prideMSRunMongoRepository;

    public String getFileAccession(){
        Page<MongoPrideFile> projectFiles = mongoFileService.findAll(PageRequest.of(0, 100, Sort.Direction.DESC, PrideArchiveField.SUBMISSION_DATE.split(",")));
        return projectFiles.iterator().next().getAccession();
    }

    public List<String> getMsRunFileAccession(){
        List<String> resultsList = new ArrayList<String>();
        MongoPrideMSRun mongoPrideMSRun = prideMSRunMongoRepository.findAll().get(0);
        if(mongoPrideMSRun!=null){
            resultsList.add(mongoPrideMSRun.getAccession());//msrun file accession
            resultsList.add(mongoPrideMSRun.getProjectAccessions().iterator().next());//add first accession id
            return resultsList;
        }
        return null;
    }



}
