package uk.ac.ebi.pride.ws.pride.controllers.project;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.elastic.client.service.ElasticQueryClientService;
import uk.ac.ebi.pride.archive.mongo.client.FileMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ImportedProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ReanalysisMongoClient;
import uk.ac.ebi.pride.archive.mongo.commons.model.PrideArchiveField;
import uk.ac.ebi.pride.archive.mongo.commons.model.files.MongoPrideFile;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideReanalysisProject;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.util.ProjectStatus;
import uk.ac.ebi.pride.solr.commons.PrideProjectField;
import uk.ac.ebi.pride.solr.commons.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.services.SolrProjectService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.*;
import uk.ac.ebi.pride.ws.pride.controllers.file.FileController;
import uk.ac.ebi.pride.ws.pride.models.dataset.*;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFileResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectModel through a RestFull API.
 *
 * @author ypriverol
 */

@RestController("/")
@Slf4j
public class MassSpecProjectController extends PagedModel {


    private final String PARTIAL_SUBMISSION_TYPE_FILTER = "project_submission_type==PARTIAL";

    private final String COMPLETE_SUBMISSION_TYPE_FILTER = "project_submission_type==COMPLETE";

    private final String DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER = PARTIAL_SUBMISSION_TYPE_FILTER + ","
            + COMPLETE_SUBMISSION_TYPE_FILTER;

    private final SolrProjectService solrProjectService;
    private final FileMongoClient fileMongoClient;
    private final ProjectMongoClient projectMongoClient;
    private final ImportedProjectMongoClient importedProjectMongoClient;
    private final ReanalysisMongoClient reanalysisMongoClient;
    private final ProjectRepoClient projectRepoClient;
    private final ElasticQueryClientService elasticQueryClientService;
    private final ObjectMapper objectMapper;

    @Autowired
    public MassSpecProjectController(SolrProjectService solrProjectService, FileMongoClient fileMongoClient,
                                     ProjectMongoClient projectMongoClient,
                                     ImportedProjectMongoClient importedProjectMongoClient,
                                     ReanalysisMongoClient reanalysisMongoClient,
                                     ProjectRepoClient projectRepoClient,
                                     ElasticQueryClientService elasticQueryClientService,
                                     ObjectMapper objectMapper) {
        this.solrProjectService = solrProjectService;
        this.fileMongoClient = fileMongoClient;
        this.projectMongoClient = projectMongoClient;
        this.importedProjectMongoClient = importedProjectMongoClient;
        this.reanalysisMongoClient = reanalysisMongoClient;
        this.projectRepoClient = projectRepoClient;
        this.elasticQueryClientService = elasticQueryClientService;
        this.objectMapper = objectMapper;
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "projects", nickname = "searchProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedModel<CompactProjectModel>> projects(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword", defaultValue = "*:*", required = false) List<String> keyword,
            @ApiParam(value = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(name = "filter", defaultValue = DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER) String filter,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(name = "pageSize", defaultValue = "100") int pageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @ApiParam(value = "A date range field with possible values of +1MONTH, +1YEAR")
            @RequestParam(name = "dateGap", defaultValue = "") String dateGap,
            @ApiParam(value = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @ApiParam(value = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortFields", defaultValue = PrideProjectField.PROJECT_SUBMISSION_DATE, required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }

        filter = getDefaultMassSpecFilter(filter);

        Page<PrideSolrProject> solrProjects = solrProjectService.findByKeyword(keyword, filter, PageRequest.of(page, pageSize, direction, sortFields.split(",")), dateGap);
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(MassSpecProjectController.class, CompactProjectModel.class);

        CollectionModel<CompactProjectModel> resources = assembler.toCollectionModel(solrProjects);

        long totalElements = solrProjects.getTotalElements();
        int totalPages = solrProjects.getTotalPages();
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedModel<CompactProjectModel> pagedResources = PagedModel.of(resources.getContent(), pageMetadata, Arrays.asList(
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, page, dateGap, sortDirection, sortFields))
                        .withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, WsUtils.validatePage(page + 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, WsUtils.validatePage(page - 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, 0, dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(MassSpecProjectController.class).projects(keyword, filter, pageSize, WsUtils.validatePage(totalPages - 1, totalPages), dateGap, sortDirection, sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name()),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, WsContastants.MAX_PAGINATION_SIZE, 0, "")).withRel(WsContastants.HateoasEnum.facets.name())
        ));

        return new HttpEntity<>(pagedResources);
    }

    private String getDefaultMassSpecFilter(String filter) {
        if (!filter.contains(COMPLETE_SUBMISSION_TYPE_FILTER) && !filter.contains(PARTIAL_SUBMISSION_TYPE_FILTER)) {
            filter = filter.concat("," + DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER);
        }
        return filter;
    }

    @ApiOperation(notes = "Return the facets for an specific search query. This method is " +
            "fully-aligned to the entry point search/projects with the parameters: _keywords_, _filter_, _pageSize_, _page_. ", value = "projects", nickname = "getProjectFacets", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedModel<FacetResource>> facets(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(value = "keyword", defaultValue = "*:*", required = false) List<String> keyword,
            @ApiParam(value = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(value = "filter", required = false, defaultValue = DEFAULT_MASS_SPEC_PROJECT_TYPE_FILTER) String filter,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "facetPageSize", defaultValue = "100", required = false) int facetPageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "facetPage", defaultValue = "0", required = false) int facetPage,
            @ApiParam(value = "A date range field with possible values of +1MONTH, +1YEAR")
            @RequestParam(value = "dateGap", defaultValue = "", required = false) String dateGap) {

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(facetPage, facetPageSize);
        facetPage = facetPageParams.getKey();
        facetPageSize = facetPageParams.getValue();

        filter = getDefaultMassSpecFilter(filter);

        FacetPage<PrideSolrProject> solrProjects = solrProjectService.findFacetByKeyword(keyword, filter, PageRequest.of(0, 10), PageRequest.of(facetPage, facetPageSize), dateGap);
        FacetResourceAssembler assembler = new FacetResourceAssembler(MassSpecProjectController.class, FacetResource.class, dateGap);
        CollectionModel<FacetResource> resources = assembler.toCollectionModel(solrProjects);


        PagedModel<FacetResource> pagedResources = PagedModel.of(resources.getContent(), null, Arrays.asList(
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, facetPage, dateGap))
                        .withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, facetPage + 1, dateGap))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, (facetPage > 0) ? facetPage - 1 : 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).facets(keyword, filter, facetPageSize, 0, dateGap))
                        .withRel(WsContastants.HateoasEnum.first.name()))
        );

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Return the dataset for a given accession", value = "projects", nickname = "getProject", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/projects/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<String>> getProject(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession", name = "accession") String accession) {

        Mono<MongoPrideProject> byAccession = projectMongoClient.findByAccession(accession);
        byAccession = byAccession.switchIfEmpty(importedProjectMongoClient.findByAccession(accession));
        return byAccession.map(project -> {
                    PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class,
                            ProjectResource.class, fileMongoClient);

                    try {
                        return new ResponseEntity<>(objectMapper.writeValueAsString(assembler.toModel(project)), HttpStatus.OK);
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

    @ApiOperation(notes = "Return the list of publications that have re-used the specified dataset", value = "projects", nickname = "getReanalysedProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ApiResponse.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ApiResponse.class)
    })

    @RequestMapping(value = "/projects/reanalysis/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<String>> getReanalysisProject(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession", name = "accession") String accession) {

        Mono<MongoPrideReanalysisProject> byAccession = reanalysisMongoClient.findByAccession(accession);
        return byAccession.map(project -> {
                    PrideReanalysisProjectResourceAssembler assembler = new PrideReanalysisProjectResourceAssembler(MassSpecProjectController.class, ProjectReanalysisResource.class);
                    try {
                        return new ResponseEntity<>(objectMapper.writeValueAsString(assembler.toModel(project)), HttpStatus.OK);
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

    @ApiOperation(notes = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", value = "projects", nickname = "getProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<HttpEntity<PagedModel<ProjectResource>>> getProjects(
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @ApiParam(value = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @ApiParam(value = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortConditions", defaultValue = PrideArchiveField.SUBMISSION_DATE, required = false) String sortFields) {
        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        int pageFinal = pageParams.getKey();
        int pageSizeFinal = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }
        final Sort.Direction sortDirectionFinal = direction;

        List<String> submissionType = new ArrayList<>(2);
        submissionType.add("COMPLETE");
        submissionType.add("PARTIAL");
        Mono<Page<MongoPrideProject>> allProjectsPageMono = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
        return allProjectsPageMono.map(mongoProjectsPage -> {
            PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class, ProjectResource.class, fileMongoClient);
            CollectionModel<ProjectResource> resources = assembler.toCollectionModel(mongoProjectsPage.getContent());

            long totalElements = mongoProjectsPage.getTotalElements();
            int totalPages = mongoProjectsPage.getTotalPages();
            PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSizeFinal, pageFinal, totalElements, totalPages);

            PagedModel<ProjectResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, pageFinal, sortDirectionFinal.name(), sortFields)).withSelfRel(),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(pageFinal + 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.next.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(pageFinal - 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.previous.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, 0, sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.first.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getProjects(pageSizeFinal, WsUtils.validatePage(totalPages - 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.last.name())
            );

            return new HttpEntity<>(pagedResources);

        });

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
    }

    @ApiOperation(notes = "Get all the Files for an specific project in PRIDE.", value = "projects", nickname = "getFilesByProject", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<HttpEntity<PagedModel<PrideFileResource>>> getFilesByProject(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession") String projectAccession,
            @RequestParam(value = "filenameFilter", required = false, defaultValue = "") String filenameFilter,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) Integer pageSize,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0", required = false) Integer page,
            @ApiParam(value = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @ApiParam(value = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortConditions", defaultValue = PrideArchiveField.FILE_NAME, required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        int pageFinal = pageParams.getKey();
        int pageSizeFinal = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortDirection.equalsIgnoreCase("ASC")) {
            direction = Sort.Direction.ASC;
        }
        Sort.Direction sortDirectionFinal = direction;

        Mono<Page<MongoPrideFile>> mongoFilesPageMono = fileMongoClient.findByProjectAccessionsAndFileNameContainsIgnoreCase(projectAccession, filenameFilter, pageSizeFinal, pageFinal);
        return mongoFilesPageMono.map(projectFilesPage -> {
            ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
            CollectionModel<PrideFileResource> resources = assembler.toCollectionModel(projectFilesPage.getContent());
            long totalElements = projectFilesPage.getTotalElements();
            int totalPages = projectFilesPage.getTotalPages();
            PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSizeFinal, pageFinal, totalElements, totalPages);

            PagedModel<PrideFileResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, pageFinal, sortDirectionFinal.name(), sortFields)).withSelfRel(),
                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, WsUtils.validatePage(pageFinal + 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.next.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, WsUtils.validatePage(pageFinal - 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.previous.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, 0, sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.first.name()),
                    linkTo(methodOn(MassSpecProjectController.class).getFilesByProject(projectAccession, filenameFilter, pageSizeFinal, WsUtils.validatePage(totalPages - 1, totalPages), sortDirectionFinal.name(), sortFields))
                            .withRel(WsContastants.HateoasEnum.last.name())
            );

            return new HttpEntity<>(pagedResources);
        });


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
    }


    @ApiOperation(notes = "Get Similar projects taking into account the metadata", value = "projects", nickname = "getSimilarProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/projects/{accession}/similarProjects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedModel<CompactProjectModel>> getSimilarProjects(
            @ApiParam(value = "The Accession id associated with this project")
            @PathVariable(value = "accession") String projectAccession,
            @ApiParam(value = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @ApiParam(value = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        List<PrideSolrProject> solrProjects = solrProjectService.findSimilarProjects(projectAccession, pageSize, page);
        solrProjects = solrProjects.stream().filter(project -> project.getSubmissionType().equals("COMPLETE") || project.getSubmissionType().equals("PARTIAL")).collect(Collectors.toList());
        CompactProjectResourceAssembler assembler = new CompactProjectResourceAssembler(MassSpecProjectController.class, CompactProjectModel.class);

        CollectionModel<CompactProjectModel> resources = assembler.toCollectionModel(solrProjects);

        long totalElements = solrProjects.size();
        int totalPages = (int)(totalElements / pageSize);
        if (totalElements % pageSize > 0)
            totalPages++;
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedModel<CompactProjectModel> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, pageSize, page))
                        .withSelfRel(),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, WsUtils.validatePage(page + 1, totalPages), pageSize))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, WsUtils.validatePage(page - 1, totalPages), pageSize))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession, 0, pageSize))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(MassSpecProjectController.class).getSimilarProjects(projectAccession,  WsUtils.validatePage(pageSize - 1, totalPages), pageSize))
                        .withRel(WsContastants.HateoasEnum.last.name())
        );

        return new HttpEntity<>(pagedResources);
    }


    @ApiOperation(notes = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "projects", nickname = "searchProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> projects(
            @ApiParam(value = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword") String keyword) {

        List<String> terms = solrProjectService.findAutoComplete(keyword);
        return new HttpEntity<>(terms);
    }

    @GetMapping(value = "/status/{accession}", produces = {MediaType.TEXT_PLAIN_VALUE})
    public String getProjectStatus(@Valid @PathVariable String accession) throws IOException {
        ProjectStatus status = projectRepoClient.getProjectStatus(accession);
        return status.name();
    }

    @ApiOperation(notes = "List of paged PRIDE Archive Projects with metadata", value = "projects", nickname = "getProjectsMetadata", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects/metadata", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<List<PrideProjectMetadata>> getProjectsMetadata(@ApiParam(value = "Identifies which page of results to fetch")
                                                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                                @ApiParam(value = "Number of results to fetch in a page")
                                                          @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Mono<Page<MongoPrideProject>> allProjectsPageMono = projectMongoClient.getAllProjects(pageSize, page);
        return allProjectsPageMono.map(allProjectsPage -> {
            List<MongoPrideProject> projects = allProjectsPage.getContent();
            return projects.stream().map(
                    project -> new PrideProjectMetadata(project.getAccession(), project.getTitle(), project.getSubmissionType(), project.getDescription(), project.getSampleProcessing(), project.getDataProcessing())
            ).toList();
        });


//        return mongoProjectService.findAll(PageRequest.of(page, pageSize)).stream().map(
//                project -> new PrideProjectMetadata(project.getAccession(), project.getTitle(), project.getSubmissionType(), project.getDescription(), project.getSampleProcessingProtocol(), project.getDataProcessingProtocol())
//        ).collect(Collectors.toList());

    }

    @ApiOperation(notes = "List of all data", value = "projects", nickname = "getAllProjects", tags = {"projects"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)})
    @RequestMapping(value = "/projects/stream", method = RequestMethod.GET, produces = {MediaType.APPLICATION_STREAM_JSON_VALUE})
    public Flux<Map<String, Object>> getProjectsStream(@RequestParam(name = "fieldsToReturn") String fieldsToReturn) {

        int batchSize = 1;
        List<String> fields = Arrays.asList(fieldsToReturn.split(","));

        AtomicInteger offset = new AtomicInteger(0);

        Map<String, Object> a = new HashMap<>();
        a.put("Error", "Error in fieldsToReturn");

        return Flux.
                defer(() -> elasticQueryClientService.findAllBy(batchSize, 0, fields)) // Initial call with offset 0
                .expand(batch -> {
                    if (batch.isEmpty()) {
                        return Mono.empty(); // Stop expanding if the batch is empty
                    }
                    int nextOffset = offset.addAndGet(batchSize);
                    return elasticQueryClientService.findAllBy(batchSize, nextOffset, fields); // Fetch the next batch
                })
                .flatMap(Flux::fromIterable)
                .map(item -> getFieldValues(item, fields))
                .onErrorReturn(a);
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
