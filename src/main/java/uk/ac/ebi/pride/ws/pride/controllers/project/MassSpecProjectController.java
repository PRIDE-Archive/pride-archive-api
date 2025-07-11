package uk.ac.ebi.pride.ws.pride.controllers.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.ParamProvider;
import uk.ac.ebi.pride.archive.elastic.client.ElasticProjectClient;
import uk.ac.ebi.pride.archive.elastic.commons.models.ElasticPrideProject;
import uk.ac.ebi.pride.archive.elastic.commons.util.CustomPageImpl;
import uk.ac.ebi.pride.archive.elastic.commons.util.PrideArchiveType;
import uk.ac.ebi.pride.archive.mongo.client.FileMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ImportedProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ReanalysisMongoClient;
import uk.ac.ebi.pride.archive.mongo.commons.model.files.MongoPrideFile;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoImportedProject;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideReanalysisProject;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.util.ProjectStatus;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.PrideProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectFileResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProject;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProjectMetadata;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFile;
import uk.ac.ebi.pride.ws.pride.service.FireService;
import uk.ac.ebi.pride.ws.pride.transformers.ElasticPrideProjectMapper;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectModel through a RestFull API.
 *
 * @author ypriverol
 */

@RestController("/")
@Slf4j
public class MassSpecProjectController {

    private final FileMongoClient fileMongoClient;
    private final ProjectMongoClient projectMongoClient;
    private final ImportedProjectMongoClient importedProjectMongoClient;
    private final ReanalysisMongoClient reanalysisMongoClient;
    private final ProjectRepoClient projectRepoClient;
    private final ObjectMapper objectMapper;
    private final ElasticProjectClient elasticProjectClient;
    private final ElasticPrideProjectMapper elasticPrideProjectMapper;

    private final FireService fireService;

    @Autowired
    public MassSpecProjectController(FileMongoClient fileMongoClient,
                                     ProjectMongoClient projectMongoClient,
                                     ImportedProjectMongoClient importedProjectMongoClient,
                                     ReanalysisMongoClient reanalysisMongoClient,
                                     ProjectRepoClient projectRepoClient,
                                     ElasticProjectClient elasticProjectClient,
                                     FireService fireService,
                                     ObjectMapper objectMapper,
                                     ElasticPrideProjectMapper elasticPrideProjectMapper) {
        this.fileMongoClient = fileMongoClient;
        this.projectMongoClient = projectMongoClient;
        this.importedProjectMongoClient = importedProjectMongoClient;
        this.reanalysisMongoClient = reanalysisMongoClient;
        this.projectRepoClient = projectRepoClient;
        this.objectMapper = objectMapper;
        this.elasticProjectClient = elasticProjectClient;
        this.fireService = fireService;
        this.elasticPrideProjectMapper = elasticPrideProjectMapper;
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

    @Operation(description = "Return the path of the dataset's files", tags = {"projects"})
    @RequestMapping(value = "/projects/files-path/{projectAccession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<String> getFtpPath(@PathVariable String projectAccession) {
        //Due to this issue : https://github.com/PRIDE-Archive/pride-archive-api/issues/108 (Datasets made public on 30-12-2021 gets wrong FTP path)
        //We have get FTP path from files ftp path stored in mongo
        Flux<MongoPrideFile> filesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, "", 1, 0);
        return filesFlux.collectList().map(mongoFiles -> {
            String ftpPath = "";
            String globusPath = "";
            if (mongoFiles != null && !mongoFiles.isEmpty()) {
                MongoPrideFile mongoPrideFile = mongoFiles.getFirst();
                Set<? extends CvParamProvider> publicFileLocations = mongoPrideFile.getPublicFileLocations();
                Optional<String> ftpPathOptional = publicFileLocations.stream().filter(l -> l.getAccession().equals("PRIDE:0000469")).map(ParamProvider::getValue).findFirst();
                if (ftpPathOptional.isPresent()) {
                    ftpPath = ftpPathOptional.get();
                }
                ftpPath = ftpPath.substring(0, ftpPath.lastIndexOf("/"));
                ftpPath = ftpPath.replace("/generated", ""); //ex: ftp://ftp.pride.ebi.ac.uk/pride/data/archive/2017/07/PXD006084/generated/Biodiversity_R_pal
                String[] split = ftpPath.split("/");
//                String accession = split[split.length - 1];
                String month = split[split.length - 2];
                String year = split[split.length - 3];
                globusPath = "https://app.globus.org/file-manager?origin_id=47772002-3e5b-4fd3-b97c-18cee38d6df2&origin_path=/pride-archive/" + year + "/" + month + "/" + projectAccession;
            }
            return "{\"ftp\": \"" + ftpPath + "\", \"globus\": \"" + globusPath + "\"}";
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
    public Mono<ResponseEntity<Flux<PrideProject>>> getProjects(
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();
        List<String> submissionType = new ArrayList<>(2);
        submissionType.add("COMPLETE");
        submissionType.add("PARTIAL");
        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
        HttpHeaders headers = new HttpHeaders();
        Mono<Long> countMono = projectMongoClient.countAllBySubmissionTypeIn(submissionType);
        return countMono.map(c -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, c.toString());
            return ResponseEntity.ok().headers(headers).body(allProjectsFlux.map(PrideProjectResourceAssembler::toModel));
        });
    }

    @Operation(description = "Get all PRIDE Archive Projects. ** DON'T TRY THIS API IN THE WEB BROWSER. USE CURL **", tags = {"projects"})
    @RequestMapping(value = "/projects/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideProject> getAllProjects() {
        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.getAllProjects();
        return allProjectsFlux.map(PrideProjectResourceAssembler::toModel);
    }

    @Operation(description = "download all PRIDE Archive Projects", tags = {"projects"})
    @RequestMapping(value = "/projects/download", method = RequestMethod.GET)
    public ResponseEntity<Flux<DataBuffer>> downloadAllProjects() {
        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.getAllProjects();
        Flux<PrideProject> prideProjectFlux = allProjectsFlux.map(PrideProjectResourceAssembler::toModel);
        Flux<DataBuffer> dataBufferFlux = prideProjectFlux
                .map(this::convertObjectToJson)
                .map(json -> json + "\n") // Adding newline for better readability in the JSON output
                .map(json -> {
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    return new DefaultDataBufferFactory().wrap(bytes);
                });

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pride_datasets.json\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(dataBufferFlux);
    }

    @Operation(description = "download all PRIDE Archive Projects by keyword", tags = {"projects"})
    @RequestMapping(value = "/projects/download/by/keyword", method = RequestMethod.GET)
    public ResponseEntity<Flux<DataBuffer>> downloadAllProjectsByKeyword(@RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,

                                                                         @RequestParam(name = "filter", required = false) String filter,
                                                                         @RequestParam(value = "sortFields", defaultValue = "submissionDate", required = false) String sortFields,

                                                                         @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection) {
        Flux<ElasticPrideProject> allProjectsFlux = elasticProjectClient.streamAllByKeyword(keyword, filter, PrideArchiveType.MS, sortFields, sortDirection);
        Flux<DataBuffer> dataBufferFlux = allProjectsFlux
                .map(p -> (convertElasticObjectToJson(elasticPrideProjectMapper.toDto(p))))
                .map(json -> json + "\n") // Adding newline for better readability in the JSON output
                .map(json -> {
                    byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                    return new DefaultDataBufferFactory().wrap(bytes);
                });

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pride_filter_datasets.json\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(dataBufferFlux);
    }

    private String convertObjectToJson(PrideProject object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    private String convertElasticObjectToJson(ElasticPrideProject object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    @Operation(description = "Count of all PRIDE Archive Projects", tags = {"projects"})
    @RequestMapping(value = "/projects/count", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<Long> getCountOfAllProjects() {
        return projectMongoClient.count();
    }

//    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"projects"})
//    @RequestMapping(value = "/projects/count", method = RequestMethod.GET)
//    public Mono<Long> getProjectsCount() {
//        List<String> submissionType = new ArrayList<>(2);
//        submissionType.add("COMPLETE");
//        submissionType.add("PARTIAL");
//        return projectMongoClient.countAllBySubmissionTypeIn(submissionType);
//    }

    @Operation(description = "Get all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/{projectAccession}/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Flux<PrideFile>>> getFilesByProject(
            @PathVariable(value = "projectAccession") String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter,
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) Integer pageSize,
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();

        Flux<MongoPrideFile> mongoFilesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter, pageSizeFinal, pageFinal);
        HttpHeaders headers = new HttpHeaders();
        Mono<Long> countMono = fileMongoClient.countByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter);
        return countMono.map(c -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, c.toString());
            return ResponseEntity.ok().headers(headers).body(mongoFilesFlux.map(ProjectFileResourceAssembler::toModel));
        });
    }

    @Operation(description = "Get all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/{projectAccession}/files/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideFile> getAllFilesByProject(
            @PathVariable(value = "projectAccession") String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter) {

        Flux<MongoPrideFile> mongoFilesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter);
        return mongoFilesFlux.map(ProjectFileResourceAssembler::toModel);
    }

    @Operation(description = "Count of PRIDE project Files", tags = {"projects"})
    @RequestMapping(value = "/projects/{projectAccession}/files/count", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<Long> getCountOfProjectFiles(@PathVariable(value = "projectAccession") String projectAccession) {
        return fileMongoClient.countByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, null);
    }

//    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"projects"})
//    @RequestMapping(value = "/projects/{projectAccession}/files/count", method = RequestMethod.GET)
//    public Mono<Long> getFilesCountByProject(
//            @PathVariable String projectAccession,
//            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter) {
//
//        return fileMongoClient.countByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter);
//    }

    @Operation(description = "Check if the dataset is public/private in PRIDE.", tags = {"projects"})
    @GetMapping(value = "/status/{accession}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getProjectStatus(@Valid @PathVariable String accession) {
        return Mono.fromCallable(() -> projectRepoClient.getProjectStatus(accession))
                .onErrorResume(IOException.class, ex -> {
                    // Log or handle IOException
                    return Mono.just(ProjectStatus.NOT_FOUND);
                })
                .flatMap(status -> {
                    if (status == ProjectStatus.NOT_FOUND) {
                        return importedProjectMongoClient.findByAccession(accession)
                                .map(project -> ProjectStatus.PUBLIC.name())
                                .defaultIfEmpty(ProjectStatus.NOT_FOUND.name());
                    } else {
                        return Mono.just(status.name());
                    }
                });
    }

    @Operation(description = "List of paged PRIDE Archive Projects with metadata", tags = {"projects"})
    @RequestMapping(value = "/projects/metadata", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Flux<PrideProjectMetadata>>> getProjectsMetadata(@RequestParam(value = "page", defaultValue = "0") Integer page,
                                                                                @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.getAllProjects(pageSize, page);
        Flux<PrideProjectMetadata> prideProjectMetadataFlux = allProjectsFlux.map(project -> new PrideProjectMetadata(project.getAccession(), project.getTitle(), project.getSubmissionType(), project.getDescription(), project.getSampleProcessing(), project.getDataProcessing()));

        HttpHeaders headers = new HttpHeaders();
        Mono<Long> countMono = projectMongoClient.count();
        return countMono.map(c -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, c.toString());
            return ResponseEntity.ok().headers(headers).body(prideProjectMetadataFlux);
        });
    }


    @Operation(description = "Get Similar projects taking into account the metadata", tags = {"projects"})
    @RequestMapping(value = "/projects/{accession}/similarProjects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<List<ElasticPrideProject>>> getSimilarProjects(
            @PathVariable(value = "accession") String projectAccession,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        HttpHeaders headers = new HttpHeaders();

        Mono<CustomPageImpl<ElasticPrideProject>> customPageMono = elasticProjectClient.findSimilarProjects(projectAccession, PrideArchiveType.MS, pageSize, page);

        return customPageMono.map(elasticPrideProjects -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, String.valueOf(elasticPrideProjects.getTotalHits()));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(elasticPrideProjects.getContent().stream().map(elasticPrideProjectMapper::toDto).toList());
        });
    }


    @Operation(description = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", tags = {"projects"})
    @RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
    public Mono<List<String>> projects(
            @RequestParam(name = "keyword") String keyword) {
        return elasticProjectClient.autoComplete(PrideArchiveType.MS, keyword);
    }

    @Operation(description = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", tags = {"projects"})
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<List<ElasticPrideProject>>> projects(

            @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,

            @RequestParam(name = "filter", required = false) String filter,

            @RequestParam(name = "pageSize", defaultValue = "100") int pageSize,

            @RequestParam(name = "page", defaultValue = "0") int page,

            @RequestParam(name = "dateGap", defaultValue = "") String dateGap,

            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,

            @RequestParam(value = "sortFields", defaultValue = "submissionDate", required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Mono<CustomPageImpl<ElasticPrideProject>> customPageMono = elasticProjectClient.findAllByKeyword(keyword, filter, PrideArchiveType.MS, pageSize, page, sortFields, sortDirection);

        HttpHeaders headers = new HttpHeaders();

        return customPageMono.map(elasticPrideProjects -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, String.valueOf(elasticPrideProjects.getTotalHits()));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(elasticPrideProjects.getContent().stream().map(elasticPrideProjectMapper::toDto).toList());
        });
    }

    @Operation(description = "Return the facets for an specific search query. This method is " +
            "fully-aligned to the entry point search/projects with the parameters: _keywords_, _filter_, _pageSize_, _page_. ", tags = {"projects"})
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<Map<String, Map<String, Long>>> facets(
            @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "facetPageSize", defaultValue = "100", required = false) int facetPageSize,
            @RequestParam(value = "facetPage", defaultValue = "0", required = false) int facetPage,
            @RequestParam(value = "dateGap", defaultValue = "", required = false) String dateGap) {

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(facetPage, facetPageSize);
        facetPage = facetPageParams.getKey();
        facetPageSize = facetPageParams.getValue();

        Mono<Map<String, Map<String, Long>>> elasticProjects = elasticProjectClient.findFacetByKeyword(keyword, filter, PrideArchiveType.MS, facetPageSize, facetPage, dateGap);
        return elasticProjects;
    }

    @Operation(description = "Get md5Checksum of all the files in a project", tags = {"projects"})
    @RequestMapping(value = "/files/checksum/{projectAccession}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getCheckSumOfFiles(@PathVariable(value = "projectAccession") String projectAccession) {
        Mono<MongoPrideProject> mongoPrideProject = projectMongoClient.findByAccession(projectAccession);
        return mongoPrideProject.map(project -> {
            Date publicationDate = project.getPublicationDate();
            SimpleDateFormat year = new SimpleDateFormat("YYYY");
            SimpleDateFormat month = new SimpleDateFormat("MM");
            String projectPath = year.format(publicationDate).toUpperCase() + "/" + month.format(publicationDate).toUpperCase() + "/" + projectAccession + "/";
            return fireService.getcheckSumOfFiles(projectPath);
        });
    }
}
