package uk.ac.ebi.pride.ws.pride.controllers.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.elastic.client.ElasticProjectClient;
import uk.ac.ebi.pride.archive.elastic.commons.models.ElasticPrideProject;
import uk.ac.ebi.pride.archive.elastic.commons.util.CustomPageImpl;
import uk.ac.ebi.pride.archive.elastic.commons.util.PrideArchiveType;
import uk.ac.ebi.pride.archive.mongo.client.FileMongoClient;
import uk.ac.ebi.pride.archive.mongo.client.ProjectMongoClient;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.PrideProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProject;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectModel through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
@RequestMapping("/pride-ap")
public class AffinityProjectController {
    final ProjectMongoClient projectMongoClient;
    final ProjectRepoClient projectRepoClient;
    final FileMongoClient fileMongoClient;

    private final ElasticProjectClient elasticProjectClient;

    @Autowired
    public AffinityProjectController(ProjectMongoClient projectMongoClient,
                                     ProjectRepoClient projectRepoClient,
                                     ElasticProjectClient elasticProjectClient,
                                     FileMongoClient fileMongoClient) {
        this.fileMongoClient = fileMongoClient;
        this.projectMongoClient = projectMongoClient;
        this.projectRepoClient = projectRepoClient;
        this.elasticProjectClient = elasticProjectClient;
    }

    @Operation(description = "List of PRIDE Archive Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
            "list is Paginated using the _pageSize_ and _page_.", tags = {"affinity-projects"})
    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Flux<PrideProject>>> getProjects(
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();
        List<String> submissionType = Collections.singletonList("AFFINITY");
        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
        HttpHeaders headers = new HttpHeaders();
        Mono<Long> countMono = projectMongoClient.countAllBySubmissionTypeIn(submissionType);
        return countMono.map(c -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, c.toString());
            return ResponseEntity.ok().headers(headers).body(allProjectsFlux.map(PrideProjectResourceAssembler::toModel));
        });

    }

//    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"affinity-projects"})
//    @RequestMapping(value = "/projects/count", method = RequestMethod.GET)
//    public Mono<Long> getProjectsCount() {
//        List<String> submissionType = Collections.singletonList("AFFINITY");
//        return projectMongoClient.countAllBySubmissionTypeIn(submissionType);
//    }

    @Operation(description = "Get Similar projects taking into account the metadata", tags = {"affinity-projects"})
    @RequestMapping(value = "/projects/{accession}/similarProjects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<CustomPageImpl<ElasticPrideProject>> getSimilarProjects(
            @Parameter(name = "The Accession id associated with this project")
            @PathVariable(value = "accession") String projectAccession,
            @Parameter(name = "Identifies which page of results to fetch")
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(name = "Number of results to fetch in a page")
            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        return elasticProjectClient.findSimilarProjects(projectAccession, PrideArchiveType.AP, pageSize, page);
    }


    @Operation(description = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", tags = {"affinity-projects"})
    @RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<List<String>> projects(
            @Parameter(name = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword") String keyword) {
        return elasticProjectClient.autoComplete(PrideArchiveType.AP, keyword);

    }

    @Operation(description = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", tags = {"affinity-projects"})
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<CustomPageImpl<ElasticPrideProject>> projects(
            @Parameter(name = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
            @Parameter(name = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(name = "filter") String filter,
            @Parameter(name = "Number of results to fetch in a page")
            @RequestParam(name = "pageSize", defaultValue = "100") int pageSize,
            @Parameter(name = "Identifies which page of results to fetch")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(name = "A date range field with possible values of +1MONTH, +1YEAR")
            @RequestParam(name = "dateGap", defaultValue = "") String dateGap,
            @Parameter(name = "Sorting direction: ASC or DESC")
            @RequestParam(value = "sortDirection", defaultValue = "DESC", required = false) String sortDirection,
            @Parameter(name = "Field(s) for sorting the results on. Default for this request is submission_date. More fields can be separated by comma and passed. Example: submission_date,project_title")
            @RequestParam(value = "sortFields", defaultValue = "submissionDate", required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        return elasticProjectClient.findAllByKeyword(keyword, filter, PrideArchiveType.AP, pageSize, page, sortFields, sortDirection);
    }

    @Operation(description = "Return the facets for an specific search query. This method is " +
            "fully-aligned to the entry point search/projects with the parameters: _keywords_, _filter_, _pageSize_, _page_. ", tags = {"affinity-projects"})
    @RequestMapping(value = "/facet/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<Map<String, Map<String, Long>>> facets(
            @Parameter(name = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(value = "keyword", defaultValue = "", required = false) String keyword,
            @Parameter(name = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(value = "filter", required = false) String filter,
            @Parameter(name = "Number of results to fetch in a page")
            @RequestParam(value = "facetPageSize", defaultValue = "100", required = false) int facetPageSize,
            @Parameter(name = "Identifies which page of results to fetch")
            @RequestParam(value = "facetPage", defaultValue = "0", required = false) int facetPage,
            @Parameter(name = "A date range field with possible values of +1MONTH, +1YEAR")
            @RequestParam(value = "dateGap", defaultValue = "", required = false) String dateGap) {

        Tuple<Integer, Integer> facetPageParams = WsUtils.validatePageLimit(facetPage, facetPageSize);
        facetPage = facetPageParams.getKey();
        facetPageSize = facetPageParams.getValue();

        return elasticProjectClient.findFacetByKeyword(keyword, filter, PrideArchiveType.AP, facetPageSize, facetPage, dateGap);
    }
}
