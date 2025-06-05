package uk.ac.ebi.pride.ws.pride.controllers.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.elastic.client.ElasticAPProjectClient;
import uk.ac.ebi.pride.archive.elastic.commons.models.ElasticPrideAPProject;
import uk.ac.ebi.pride.archive.elastic.commons.models.Protein;
import uk.ac.ebi.pride.archive.elastic.commons.util.CustomPageImpl;
import uk.ac.ebi.pride.archive.elastic.commons.util.PrideArchiveType;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.transformers.ElasticPrideAPProjectMapper;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectModel through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
@RequestMapping("/pride-ap")
public class AffinityProjectController {

    private final ElasticAPProjectClient elasticAPProjectClient;
    private final ElasticPrideAPProjectMapper elasticAPPrideProjectMapper;

    @Autowired
    public AffinityProjectController(ElasticAPProjectClient elasticAPProjectClient,
                                     ElasticPrideAPProjectMapper elasticAPPrideProjectMapper) {
        this.elasticAPProjectClient = elasticAPProjectClient;
        this.elasticAPPrideProjectMapper = elasticAPPrideProjectMapper;
    }

//    @Operation(description = "List of PRIDE Archive AP Projects. The following method do not allows to perform search, for search functionality you will need to use the search/projects. The result " +
//            "list is Paginated using the _pageSize_ and _page_.", tags = {"affinity-projects"})
//    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    public Mono<ResponseEntity<Flux<PrideProject>>> getProjects(
//            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
//            @RequestParam(value = "page", defaultValue = "0", required = false) int page) {
//
//        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
//        final int pageFinal = pageParams.getKey();
//        final int pageSizeFinal = pageParams.getValue();
//        List<String> submissionType = Collections.singletonList("AFFINITY");
//        HttpHeaders headers = new HttpHeaders();
//        elasticAPProjectClient.findAllBy(PrideArchiveType.AP,pageSize,page).map(
//                elasticPrideProject -> {
//
//
//                    return ResponseEntity.ok().headers(headers).body(allProjectsFlux.map(PrideProjectResourceAssembler::toModel));
//                }
//
//        )
//        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
//        HttpHeaders headers = new HttpHeaders();
//        Mono<Long> countMono = projectMongoClient.countAllBySubmissionTypeIn(submissionType);
//        return countMono.map(c -> {
//            headers.set(WsContastants.TOTAL_RECORDS_HEADER, c.toString());
//            return ResponseEntity.ok().headers(headers).body(allProjectsFlux.map(PrideProjectResourceAssembler::toModel));
//        });
//
//    }
//

    /// /    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"affinity-projects"})
    /// /    @RequestMapping(value = "/projects/count", method = RequestMethod.GET)
    /// /    public Mono<Long> getProjectsCount() {
    /// /        List<String> submissionType = Collections.singletonList("AFFINITY");
    /// /        return projectMongoClient.countAllBySubmissionTypeIn(submissionType);
    /// /    }
//
//    @Operation(description = "Get Similar projects taking into account the metadata", tags = {"affinity-projects"})
//    @RequestMapping(value = "/projects/{accession}/similarProjects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    public Mono<ResponseEntity<List<ElasticPrideProject>>> getSimilarProjects(
//            @Parameter(name = "The Accession id associated with this project")
//            @PathVariable(value = "accession") String projectAccession,
//            @Parameter(name = "Identifies which page of results to fetch")
//            @RequestParam(value = "page", defaultValue = "0") Integer page,
//            @Parameter(name = "Number of results to fetch in a page")
//            @RequestParam(value = "pageSize", defaultValue = "100") Integer pageSize) {
//
//        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
//        page = pageParams.getKey();
//        pageSize = pageParams.getValue();
//        HttpHeaders headers = new HttpHeaders();
//
//        Mono<CustomPageImpl<ElasticPrideProject>> customPageMono = elasticProjectClient.findSimilarProjects(projectAccession, PrideArchiveType.AP, pageSize, page);
//        return customPageMono.map(elasticPrideProjects -> {
//            headers.set(WsContastants.TOTAL_RECORDS_HEADER, String.valueOf(elasticPrideProjects.getTotalHits()));
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(elasticPrideProjects.getContent().stream().map(elasticPrideProjectMapper::toDto).toList());
//        });
//    }
//
//
//    @Operation(description = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
//            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains both keywords. The _filter_ parameter provides allows the method " +
//            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", tags = {"affinity-projects"})
//    @RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    public Mono<List<String>> projects(
//            @Parameter(name = "The entered word will be searched among the fields to fetch matching projects")
//            @RequestParam(name = "keyword") String keyword) {
//        return elasticProjectClient.autoComplete(PrideArchiveType.AP, keyword);
//
//    }
    @Operation(description = "Search all public projects in PRIDE Archive. The _keywords_ are used to search all the projects that at least contains one of the keyword. For example " +
            " if keywords: proteome, cancer are provided the search looks for all the datasets that contains one or both keywords. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", tags = {"affinity-projects"})
    @RequestMapping(value = "/search/projects", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<List<ElasticPrideAPProject>>> projects(
            @Parameter(name = "The entered word will be searched among the fields to fetch matching projects")
            @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
            @Parameter(name = "Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==PRD000001")
            @RequestParam(name = "filter", required = false) String filter,
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

        Mono<CustomPageImpl<ElasticPrideAPProject>> customPageMono = elasticAPProjectClient.findAllByKeyword(keyword, filter, PrideArchiveType.AP, pageSize, page, sortFields, sortDirection);

        HttpHeaders headers = new HttpHeaders();

        return customPageMono.map(elasticPrideProjects -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, String.valueOf(elasticPrideProjects.getTotalHits()));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(elasticPrideProjects.getContent().stream().map(elasticAPPrideProjectMapper::toDto).toList());
        });
    }

    @Operation(description = "Search proteins using a keyword. This looks for matching proteins and supports pagination.", tags = {"affinity-projects"})
    @RequestMapping(value = "/search/proteins", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<List<Protein>>> projects(
            @RequestParam(name = "projectAccession" , required = true) String projectAccession,
            @RequestParam(name = "keyword", defaultValue = "", required = false) String keyword,
            @RequestParam(name = "pageSize", defaultValue = "100") int pageSize,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "accession") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        // Call the Protein search WebClient method
        Mono<CustomPageImpl<Protein>> customPageMono = elasticAPProjectClient
                .searchProteinsByKeyword(projectAccession, keyword, pageSize, page, sortField, sortDirection);

        HttpHeaders headers = new HttpHeaders();

        return customPageMono.map(proteinPage -> {
            headers.set(WsContastants.TOTAL_RECORDS_HEADER, String.valueOf(proteinPage.getTotalHits()));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(proteinPage.getContent());
        });
    }

    @Operation(description = "Get the project details by accession", tags = {"affinity-projects"})
    @RequestMapping("/{accession}")
    public Mono<ResponseEntity<ElasticPrideAPProject>> getProjectByAccession(@PathVariable String accession) {
        return elasticAPProjectClient.findByAccession(accession)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

}
