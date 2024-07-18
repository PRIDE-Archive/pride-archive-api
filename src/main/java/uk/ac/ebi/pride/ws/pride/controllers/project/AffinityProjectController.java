package uk.ac.ebi.pride.ws.pride.controllers.project;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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

/**
 * The Dataset/Project Controller enables to retrieve the information for each PRIDE Project/CompactProjectModel through a RestFull API.
 *
 * @author ypriverol
 */

@RestController
@RequestMapping("/pride-ap")
public class AffinityProjectController {

    private final String DEFAULT_AFFINITY_PROJECTS_FILTER = "project_submission_type==AFFINITY";
    final ProjectMongoClient projectMongoClient;
    final ProjectRepoClient projectRepoClient;
    final FileMongoClient fileMongoClient;

    @Autowired
    public AffinityProjectController(ProjectMongoClient projectMongoClient,
                                     ProjectRepoClient projectRepoClient,
                                     FileMongoClient fileMongoClient) {
        this.fileMongoClient = fileMongoClient;
        this.projectMongoClient = projectMongoClient;
        this.projectRepoClient = projectRepoClient;
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

    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"affinity-projects"})
    @RequestMapping(value = "/projects/count", method = RequestMethod.GET)
    public Mono<Long> getProjectsCount() {
        List<String> submissionType = Collections.singletonList("AFFINITY");
        return projectMongoClient.countAllBySubmissionTypeIn(submissionType);
    }
}
