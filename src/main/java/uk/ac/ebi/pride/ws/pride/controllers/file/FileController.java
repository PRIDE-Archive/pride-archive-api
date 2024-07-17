package uk.ac.ebi.pride.ws.pride.controllers.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

//    @Operation(description = "Get a File from PRIDE database by FileName", value = "files", nickname = "fileByName", tags = {"files"})
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "OK", response = APIError.class),
//            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
//            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
//    })
//    @RequestMapping(value = "/files/fileByName", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    public ResponseEntity getFileByName(@RequestParam(value = "fileName") String fileName,
//                                        @RequestParam(value = "projectAccession", defaultValue = "",
//                                                required = false) String projectAccession
//    ) {
//
//
//        Page<MongoPrideFile> file = null;
//        if (projectAccession != null && !projectAccession.isEmpty())
//            file = mongoFileService.searchFiles("fileName==" + fileName + ",projectAccessions=in=" + projectAccession, PageRequest.of(0, 100));
//        else
//            file = mongoFileService.searchFiles("fileName==" + fileName, PageRequest.of(0, 100));
//
//        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
//        PrideFileResource resource = null;
//        if (file.getTotalElements() == 1) {
//            resource = assembler.toModel(file.getContent().get(0));
//        }
//        if (resource == null)
//            return new ResponseEntity(null, HttpStatus.NO_CONTENT);
//
//        return new ResponseEntity(resource, HttpStatus.OK);
//    }

    @Operation(description = "Get a File from PRIDE database", tags = {"files"})
    @RequestMapping(value = "/files/{fileAccession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<PrideFile> getFile(
            @PathVariable(value = "fileAccession") String fileAccession) {

        Mono<MongoPrideFile> fileMono = fileMongoClient.findByAccession(fileAccession);
        return fileMono.map(file -> {
            ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler();
            return assembler.toModel(file);
        });

//        Optional<MongoPrideFile> file = mongoFileService.findByFileAccession(accession);
//        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
//        PrideFileResource resource = assembler.toModel(file.get());
//        return new HttpEntity<>(resource);
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

//        return filesPageMono.map(filesPage -> {
//            List<MongoPrideFile> files = filesPage.getContent();
//            return files.stream()
//                    .filter(file -> file.getFileCategory().getAccession().equals("PRIDE:0000584"))
//                    .map(f -> {
//                        Optional<CvParam> ftpURLCvParam = f.getPublicFileLocations().stream()
//                                .filter(cvParam -> cvParam.getAccession().equalsIgnoreCase("PRIDE:0000469"))
//                                .findFirst();
//                        return ftpURLCvParam.map(cvParam -> cvParam.getValue().trim()).orElse("");
//                    }).filter(s->!s.trim().isEmpty()).toList();
//        });
//    }

//        List<MongoPrideFile> files = mongoFileService.findFilesByProjectAccession(accession);
//        List<MongoPrideFile> sdrfFiles = files.stream()
//                .filter(file -> file.getFileCategory().getAccession().equals("PRIDE:0000584"))
//                .collect(Collectors.toList());
//
//        if (sdrfFiles == null || sdrfFiles.size() == 0) {
//            return new ResponseEntity(null, HttpStatus.NO_CONTENT);
//        }
//
//        if (sdrfFiles.size() > 1) {
//            return new ResponseEntity("Contains more than one SDRF file", HttpStatus.BAD_REQUEST);
//        }
//
//        Optional<CvParam> ftpURL = sdrfFiles.get(0).getPublicFileLocations()
//                .stream()
//                .filter(url -> url.getAccession().equalsIgnoreCase("PRIDE:0000469"))
//                .findFirst();
//
//        if (!ftpURL.isPresent()) {
//            return new ResponseEntity(null, HttpStatus.NO_CONTENT);
//        }
//
//        String url = ftpURL.get().getValue();
//        url = ProjectFileResourceAssembler.getFTPUrl(url);
//        String resource = null;
//        try {
//            resource = FileUtils.readFileURL(url);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return new ResponseEntity(resource, HttpStatus.OK);
//    }

    @Operation(description = "Get count of each file types in a project by accession", tags = {"files"})
    @RequestMapping(value = "/files/getCountOfFilesByType/{projectAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity> getCountOfFilesByType(@PathVariable(value = "projectAccession") String projectAccession) {

        Mono<Map<String, Long>> mapMono = fileMongoClient.getCountOfFilesByType(projectAccession);
        return mapMono.map(m -> new ResponseEntity(m, HttpStatus.OK));

//        List<MongoPrideFile> files = mongoFileService.findFilesByProjectAccession(accession);
//
//        if (files != null && files.size() > 0) {
//            Map<String, Long> fileTypeCount = files.stream().collect(Collectors.groupingBy(
//                    file -> file.getFileCategory().getValue(), Collectors.counting()
//            ));
//            return new ResponseEntity(fileTypeCount, HttpStatus.OK);
//        }
//
//        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}
