package uk.ac.ebi.pride.ws.pride.controllers.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @RequestMapping(value = "/projects/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<String>> getProject(
            @Parameter(name = "The Accession id associated with this project")
            @PathVariable(value = "accession", name = "accession") String accession) {

        Mono<MongoPrideProject> byAccession = projectMongoClient.findByAccession(accession);
        byAccession = byAccession.switchIfEmpty(importedProjectMongoClient.findByAccession(accession));
        return byAccession.map(project -> {
                    try {
                        return new ResponseEntity<>(objectMapper.writeValueAsString(PrideProjectResourceAssembler.toModel(project)), HttpStatus.OK);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .switchIfEmpty(Mono.just(new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, HttpStatus.NOT_FOUND)));

//        Optional<MongoPrideProject> project = mongoProjectService.findByAccession(accession);
//        if (!project.isPresent()) {
//            Optional<MongoImportedProject> mongoImportedProjectOptional = importedProjectMongoService.findByAccession(accession);
//            if (mongoImportedProjectOptional.isPresent()) {
//                MongoPrideProject mongoImportedProject = mongoImportedProjectOptional.get();
//                project = Optional.of(mongoImportedProject);
//            }
//        }
//        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class,
//                ProjectResource.class, mongoFileService);
//        return project.<ResponseEntity<Object>>map(mongoPrideProject -> new ResponseEntity<>(assembler.toModel(mongoPrideProject), HttpStatus.OK))
//                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));

    }

    @Operation(description = "Return the FTP path of the dataset's files", tags = {"projects"})
    @RequestMapping(value = "/projects/ftp-path/{accession}", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public Mono<String> getFtpPath(@PathVariable String accession) {
        //Due to this issue : https://github.com/PRIDE-Archive/pride-archive-api/issues/108 (Datasets made public on 30-12-2021 gets wrong FTP path)
        //We have get FTP path from files ftp path stored in mongo
        Flux<MongoPrideFile> filesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(accession, "", 1, 0);
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
//            if (ftpPath == null) {
//                Date publicationDate = mongoPrideProject.getPublicationDate();
//                SimpleDateFormat year = new SimpleDateFormat("YYYY");
//                SimpleDateFormat month = new SimpleDateFormat("MM");
//                ftpPath = "ftp://ftp.pride.ebi.ac.uk/pride/data/archive/" + year.format(publicationDate).toUpperCase() + "/" + month.format(publicationDate).toUpperCase() + "/" + mongoPrideProject.getAccession();
//            }
            return ftpPath;
        });
    }

    @Operation(description = "Return the list of publications that have re-used the specified dataset", tags = {"projects"})
    @RequestMapping(value = "/projects/reanalysis/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<String>> getReanalysisProject(
            @Parameter(name = "The Accession id associated with this project")
            @PathVariable(value = "accession", name = "accession") String accession) {

        Mono<MongoPrideReanalysisProject> prideReanalysisProjectMono = reanalysisMongoClient.findByAccession(accession);
        return prideReanalysisProjectMono.map(project -> {
                    try {
                        return new ResponseEntity<>(objectMapper.writeValueAsString(prideReanalysisProjectMono), HttpStatus.OK);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .switchIfEmpty(Mono.just(new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, HttpStatus.NOT_FOUND)));


//        Optional<MongoPrideReanalysisProject> project = prideReanalysisMongoService.findByAccession(accession);
//        PrideReanalysisProjectResourceAssembler assembler = new PrideReanalysisProjectResourceAssembler(MassSpecProjectController.class, ProjectReanalysisResource.class);
//        ResponseEntity<Object> responseEntity = project.<ResponseEntity<Object>>map(reanalysisProject -> new ResponseEntity<>(assembler.toModel(reanalysisProject), HttpStatus.OK))
//                .orElseGet(() -> new ResponseEntity<>(WsContastants.PX_PROJECT_NOT_FOUND + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));

    }

    @Operation(description = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", tags = {"projects"})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideProject> getProjects(
            @Parameter(name = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @Parameter(name = "Identifies which page of results to fetch")
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

    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/count", method = RequestMethod.GET)
    public Mono<Long> getProjectsCount() {
        List<String> submissionType = new ArrayList<>(2);
        submissionType.add("COMPLETE");
        submissionType.add("PARTIAL");
        return projectMongoClient.countAllBySubmissionTypeIn(submissionType);
    }

//        return allProjectsPageMono.map(mongoProjectsPage -> {
//            CollectionModel<ProjectResource> resources = assembler.toCollectionModel(mongoProjectsPage.getContent());
//
//            long totalElements = mongoProjectsPage.getTotalElements();
//            int totalPages = mongoProjectsPage.getTotalPages();
//            PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSizeFinal, pageFinal, totalElements, totalPages);
//
//            PagedModel<ProjectResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
//                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, pageFinal, sortDirectionFinal.name(), sortFields)).withSelfRel(),
//                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(pageFinal + 1, totalPages), sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.next.name()),
//                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(pageFinal - 1, totalPages), sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.previous.name()),
//                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, 0, sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.first.name()),
//                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(totalPages - 1, totalPages), sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.last.name())
//            );
//
//            return new HttpEntity<>(pagedResources);
//
//        });

//        Page<MongoPrideProject> mongoProjects = mongoProjectService.findAll(PageRequest.of(page, pageSize, direction, sortFields.split(",")));
//        List<MongoPrideProject> filteredList = mongoProjects.stream().filter(project -> project.getSubmissionType().equals("COMPLETE") || project.getSubmissionType().equals("PARTIAL")).collect(Collectors.toList());
//        mongoProjects = new PageImpl<>(filteredList, PageRequest.of(page, pageSize, direction, sortFields.split(",")), filteredList.size());
//        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class, ProjectResource.class, mongoFileService);
//
//        CollectionModel<ProjectResource> resources = assembler.toCollectionModel(mongoProjects);
//
//        long totalElements = mongoProjects.getTotalElements();
//        long totalPages = mongoProjects.getTotalPages();
//        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);
//
//        PagedModel<ProjectResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
//                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, page, sortDirection, sortFields)).withSelfRel(),
//                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.next.name()),
//                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.previous.name()),
//                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, 0, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.first.name()),
//                linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSize, (int) totalPages, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.last.name())
//        );
//
//        return new HttpEntity<>(pagedResources);
//    }

    @Operation(description = "Get all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/{accession}/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideFile> getFilesByProject(
            @Parameter(name = "The Accession id associated with this project")
            @PathVariable(value = "accession") String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter,
            @Parameter(name = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) Integer pageSize,
            @Parameter(name = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();

        Flux<MongoPrideFile> mongoFilesFlux = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter, pageSizeFinal, pageFinal);
        return mongoFilesFlux.map(ProjectFileResourceAssembler::toModel);
    }

    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/{accession}/files/count", method = RequestMethod.GET)
    public Mono<Long> getFilesCountByProject(
            @Parameter(name = "The Accession id associated with this project")
            @PathVariable(value = "accession") String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter) {

        return fileMongoClient.countByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter);
    }


//        return mongoFilesPageMono.map(projectFilesPage -> {
//            CollectionModel<PrideFileResource> resources = assembler.toCollectionModel(projectFilesPage.getContent());
//            long totalElements = projectFilesPage.getTotalElements();
//            int totalPages = projectFilesPage.getTotalPages();
//            PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSizeFinal, pageFinal, totalElements, totalPages);
//
//            PagedModel<PrideFileResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
//                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, pageFinal, sortDirectionFinal.name(), sortFields)).withSelfRel(),
//                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, WsUtils.validatePage(pageFinal + 1, totalPages), sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.next.name()),
//                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, WsUtils.validatePage(pageFinal - 1, totalPages), sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.previous.name()),
//                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, 0, sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.first.name()),
//                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, WsUtils.validatePage(totalPages - 1, totalPages), sortDirectionFinal.name(), sortFields))
//                            .withRel(WsContastants.HateoasEnum.last.name())
//            );
//
//            return new HttpEntity<>(pagedResources);
//        });


//        Page<MongoPrideFile> projectFiles = mongoFileService.findFilesByProjectAccessionAndFiler(projectAccession, filter, PageRequest.of(page, pageSize, direction, sortFields.split(",")));
//        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
//
//        CollectionModel<PrideFileResource> resources = assembler.toCollectionModel(projectFiles);
//
//        long totalElements = projectFiles.getTotalElements();
//        long totalPages = projectFiles.getTotalPages();
//        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);
//
//        PagedModel<PrideFileResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
//                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, page, sortDirection, sortFields)).withSelfRel(),
//                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.next.name()),
//                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.previous.name()),
//                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, 0, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.first.name()),
//                linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filter, pageSize, (int) totalPages, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.last.name())
//        );
//
//        return new HttpEntity<>(pagedResources);
//    }

    @GetMapping(value = "/status/{accession}", produces = {MediaType.TEXT_PLAIN_VALUE})
    public String getProjectStatus(@Valid @PathVariable String accession) throws IOException {
        ProjectStatus status = projectRepoClient.getProjectStatus(accession);
        return status.name();
    }

    @Operation(description = "List of paged PRIDE Archive Projects with metadata", tags = {"projects"})
    @RequestMapping(value = "/projects/metadata", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Flux<PrideProjectMetadata> getProjectsMetadata(@Parameter(name = "Identifies which page of results to fetch")
                                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                          @Parameter(name = "Number of results to fetch in a page")
                                                                @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.getAllProjects(pageSize, page);
        return allProjectsFlux.map(project -> new PrideProjectMetadata(project.getAccession(), project.getTitle(), project.getSubmissionType(), project.getDescription(), project.getSampleProcessing(), project.getDataProcessing()));
//        return allProjectsPageMono.map(allProjectsPage -> {
//            List<MongoPrideProject> projects = allProjectsPage.getContent();
//            return projects.stream().map(
//                    project -> new PrideProjectMetadata(project.getAccession(), project.getTitle(), project.getSubmissionType(), project.getDescription(), project.getSampleProcessing(), project.getDataProcessing())
//            ).toList();
//        });


//        return mongoProjectService.findAll(PageRequest.of(page, pageSize)).stream().map(
//                project -> new PrideProjectMetadata(project.getAccession(), project.getTitle(), project.getSubmissionType(), project.getDescription(), project.getSampleProcessingProtocol(), project.getDataProcessingProtocol())
//        ).collect(Collectors.toList());

    }

//    @Operation(description = "List of all data", tags = {"projects"})
//    @RequestMapping(value = "/projects/stream", method = RequestMethod.GET, produces = {MediaType.APPLICATION_STREAM_JSON_VALUE})
//    public Flux<Map<String, Object>> getProjectsStream(@RequestParam(name = "fieldsToReturn") String fieldsToReturn) {
//
//        int batchSize = 1;
//        List<String> fields = Arrays.asList(fieldsToReturn.split(","));
//
//        AtomicInteger offset = new AtomicInteger(0);
//
//        Map<String, Object> a = new HashMap<>();
//        a.put("Error", "Error in fieldsToReturn");
//
//        return Flux.
//                defer(() -> elasticQueryClientService.findAllBy(batchSize, 0, fields)) // Initial call with offset 0
//                .expand(batch -> {
//                    if (batch.isEmpty()) {
//                        return Mono.empty(); // Stop expanding if the batch is empty
//                    }
//                    int nextOffset = offset.addAndGet(batchSize);
//                    return elasticQueryClientService.findAllBy(batchSize, nextOffset, fields); // Fetch the next batch
//                })
//                .flatMap(Flux::fromIterable)
//                .map(item -> getFieldValues(item, fields))
//                .onErrorReturn(a);
//    }

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
