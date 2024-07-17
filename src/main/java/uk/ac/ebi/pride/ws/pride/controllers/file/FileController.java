package uk.ac.ebi.pride.ws.pride.controllers.file;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.mongo.client.FileMongoClient;
import uk.ac.ebi.pride.archive.mongo.commons.model.files.MongoPrideFile;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectFileResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * @author ypriverol
 */

@RestController
public class FileController {

    private final FileMongoClient fileMongoClient;

    @Autowired
    public FileController(FileMongoClient fileMongoClient) {
        this.fileMongoClient = fileMongoClient;
    }

    @Operation(description = "Get a File from PRIDE database", tags = {"files"})
    @RequestMapping(value = "/files/{fileAccession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<PrideFile> getFile(
            @PathVariable(value = "fileAccession") String fileAccession) {

        Mono<MongoPrideFile> fileMono = fileMongoClient.findByAccession(fileAccession);
        return fileMono.map(file -> {
            ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler();
            return assembler.toModel(file);
        });
    }


    @Operation(description = "Get an SDRF file from project accession", tags = {"files"})
    @RequestMapping(value = "/files/sdrf/{projectAccession}", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<List<String>> getSDRFFilesByProjectAccession(@PathVariable(value = "projectAccession") String projectAccession) {

        Flux<MongoPrideFile> filesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase
                (projectAccession, ".tsv", 99999, 0);

        return filesFlux.filter(file -> file.getFileCategory().getAccession().equals("PRIDE:0000584")).map(f -> {
            Optional<CvParam> ftpURLCvParam = f.getPublicFileLocations().stream()
                    .filter(cvParam -> cvParam.getAccession().equalsIgnoreCase("PRIDE:0000469"))
                    .findFirst();
            return ftpURLCvParam.map(cvParam -> cvParam.getValue().trim()).orElse("");
        }).filter(s -> !s.trim().isEmpty()).collectList();
    }

    @Operation(description = "Get count of each file types in a project by accession", tags = {"files"})
    @RequestMapping(value = "/files/getCountOfFilesByType/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> getCountOfFilesByType(@PathVariable(value = "projectAccession") String projectAccession) {
        Mono<Map<String, Long>> mapMono = fileMongoClient.getCountOfFilesByType(projectAccession);
        return mapMono.map(m -> new ResponseEntity(m, HttpStatus.OK));
    }

}
