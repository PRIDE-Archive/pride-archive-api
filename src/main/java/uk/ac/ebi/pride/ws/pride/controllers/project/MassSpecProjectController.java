package uk.ac.ebi.pride.ws.pride.controllers.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.ParamProvider;
import uk.ac.ebi.pride.archive.mongo.client.FileMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ImportedProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ReanalysisMongoClient;
import uk.ac.ebi.pride.archive.mongo.commons.model.files.MongoPrideFile;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideReanalysisProject;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.util.ProjectStatus;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.*;
import uk.ac.ebi.pride.ws.pride.models.dataset.*;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFile;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectModel through a RestFull API.
 *
 * @author ypriverol
 */

@RestController("/")
@Slf4j
public class MassSpecProjectController {


    private final String PARTIAL_SUBMISSION_TYPE_FILTER = "project_submission_type==PARTIAL";

    private final String COMPLETE_SUBMISSION_TYPE_FILTER = "project_submission_type==COMPLETE";

    private final String DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER = PARTIAL_SUBMISSION_TYPE_FILTER + ","
            + COMPLETE_SUBMISSION_TYPE_FILTER;

    private final FileMongoClient fileMongoClient;
    private final ProjectMongoClient projectMongoClient;
    private final ImportedProjectMongoClient importedProjectMongoClient;
    private final ReanalysisMongoClient reanalysisMongoClient;
    private final ProjectRepoClient projectRepoClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public MassSpecProjectController(FileMongoClient fileMongoClient,
                                     ProjectMongoClient projectMongoClient,
                                     ImportedProjectMongoClient importedProjectMongoClient,
                                     ReanalysisMongoClient reanalysisMongoClient,
                                     ProjectRepoClient projectRepoClient,
//                                     ElasticQueryClientService elasticQueryClientService,
                                     ObjectMapper objectMapper) {
        this.fileMongoClient = fileMongoClient;
        this.projectMongoClient = projectMongoClient;
        this.importedProjectMongoClient = importedProjectMongoClient;
        this.reanalysisMongoClient = reanalysisMongoClient;
        this.projectRepoClient = projectRepoClient;
        this.objectMapper = objectMapper;
    }


    @Operation(description = "Return the dataset for a given accession", tags = {"projects"})
    @RequestMapping(value = "/projects/{projectAccession}", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> getProject(
            @PathVariable String projectAccession) {

        Mono<MongoPrideProject> byAccession = projectMongoClient.findByAccession(projectAccession);
        byAccession = byAccession.switchIfEmpty(importedProjectMongoClient.findByAccession(projectAccession));
        return byAccession.map(project -> {
                    try {
                        return new ResponseEntity<>(objectMapper.writeValueAsString(PrideProjectResourceAssembler.toModel(project)), HttpStatus.OK);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .switchIfEmpty(Mono.just(new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + projectAccession + WsContastants.CONTACT_PRIDE, HttpStatus.NOT_FOUND)));
    }

    @Operation(description = "Return the FTP path of the dataset's files", tags = {"projects"})
    @RequestMapping(value = "/projects/ftp-path/{projectAccession}", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public Mono<String> getFtpPath(@PathVariable String projectAccession) {
        //Due to this issue : https://github.com/PRIDE-Archive/pride-archive-api/issues/108 (Datasets made public on 30-12-2021 gets wrong FTP path)
        //We have get FTP path from files ftp path stored in mongo
        Flux<MongoPrideFile> filesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, "", 1, 0);
        return filesFlux.collectList().map(mongoFiles -> {
            String ftpPath = "";
            if (mongoFiles != null && !mongoFiles.isEmpty()) {
                MongoPrideFile mongoPrideFile = mongoFiles.getFirst();
                Set<? extends CvParamProvider> publicFileLocations = mongoPrideFile.getPublicFileLocations();
                Optional<String> ftpPathOptional = publicFileLocations.stream().filter(l -> l.getAccession().equals("PRIDE:0000469")).map(ParamProvider::getValue).findFirst();
                if (ftpPathOptional.isPresent()) {
                    ftpPath = ftpPathOptional.get();
                }
                ftpPath = ftpPath.substring(0, ftpPath.lastIndexOf("/"));
            }
            return ftpPath;
        });
    }

    @Operation(description = "Return the list of publications that have re-used the specified dataset", tags = {"projects"})
    @RequestMapping(value = "/projects/reanalysis/{projectAccession}", method = RequestMethod.GET)
    public Mono<ResponseEntity<String>> getReanalysisProject(
            @PathVariable String projectAccession) {

        Mono<MongoPrideReanalysisProject> prideReanalysisProjectMono = reanalysisMongoClient.findByAccession(projectAccession);
        return prideReanalysisProjectMono.map(project -> {
                    try {
                        return new ResponseEntity<>(objectMapper.writeValueAsString(prideReanalysisProjectMono), HttpStatus.OK);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .switchIfEmpty(Mono.just(new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + projectAccession + WsContastants.CONTACT_PRIDE, HttpStatus.NOT_FOUND)));
    }

    @Operation(description = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", tags = {"projects"})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideProject> getProjects(
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();
        List<String> submissionType = new ArrayList<>(2);
        submissionType.add("COMPLETE");
        submissionType.add("PARTIAL");
        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
        return allProjectsFlux.map(PrideProjectResourceAssembler::toModel);
    }

    @Operation(description = "List of all PRIDE Archive Projects", tags = {"projects"})
    @RequestMapping(value = "/projects/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideProject> getAllProjects() {
        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.getAllProjects();
        return allProjectsFlux.map(PrideProjectResourceAssembler::toModel);
    }

    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/count", method = RequestMethod.GET)
    public Mono<Long> getProjectsCount() {
        List<String> submissionType = new ArrayList<>(2);
        submissionType.add("COMPLETE");
        submissionType.add("PARTIAL");
        return projectMongoClient.countAllBySubmissionTypeIn(submissionType);
    }

    @Operation(description = "Get all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/{projectAccession}/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideFile> getFilesByProject(
            @PathVariable(value = "projectAccession") String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter,
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) Integer pageSize,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();

        Flux<MongoPrideFile> mongoFilesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter, pageSizeFinal, pageFinal);
        return mongoFilesFlux.map(ProjectFileResourceAssembler::toModel);
    }

    @Operation(description = "Get all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/{projectAccession}/files/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideFile> getAllFilesByProject(
            @PathVariable(value = "projectAccession") String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter) {

        Flux<MongoPrideFile> mongoFilesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter);
        return mongoFilesFlux.map(ProjectFileResourceAssembler::toModel);
    }

    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/{projectAccession}/files/count", method = RequestMethod.GET)
    public Mono<Long> getFilesCountByProject(
            @PathVariable String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter) {

        return fileMongoClient.countByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter);
    }

    @GetMapping(value = "/status/{accession}", produces = {MediaType.TEXT_PLAIN_VALUE})
    public String getProjectStatus(@Valid @PathVariable String accession) throws IOException {
        ProjectStatus status = projectRepoClient.getProjectStatus(accession);
        return status.name();
    }

    @Operation(description = "List of paged PRIDE Archive Projects with metadata", tags = {"projects"})
    @RequestMapping(value = "/projects/metadata", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideProjectMetadata> getProjectsMetadata(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                          @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.getAllProjects(pageSize, page);
        return allProjectsFlux.map(project -> new PrideProjectMetadata(project.getAccession(), project.getTitle(), project.getSubmissionType(), project.getDescription(), project.getSampleProcessing(), project.getDataProcessing()));
    }

    public static Map<String, Object> getFieldValues(Object obj, List<String> fields) {
        Map<String, Object> fieldValues = new HashMap<>();

        for (String field : fields) {
            try {
                // Construct the getter method name
                String getterName = "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);

                // Get the method from the class
                Method getterMethod = obj.getClass().getMethod(getterName);

                // Invoke the getter method on the object
                Object value = getterMethod.invoke(obj);

                // Add the field name and value to the map
                fieldValues.put(field, value);
            } catch (Exception e) {
                log.error("Issue with fields passed", e.getMessage());
            }
        }

        return fieldValues;
    }
}
