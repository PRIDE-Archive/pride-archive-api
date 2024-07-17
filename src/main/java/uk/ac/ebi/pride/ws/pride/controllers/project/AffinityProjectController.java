package uk.ac.ebi.pride.ws.pride.controllers.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.ArrayList;
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
    public Flux<PrideProject> getProjects(
            @RequestParam(value = "pageSize", defaultValue = "100", required = false) int pageSize,
            @RequestParam(value = "page", defaultValue = "0", required = false) int page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        final int pageFinal = pageParams.getKey();
        final int pageSizeFinal = pageParams.getValue();
        List<String> submissionType = Collections.singletonList("AFFINITY");
        Flux<MongoPrideProject> allProjectsFlux = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
        return allProjectsFlux.map(PrideProjectResourceAssembler::toModel);
    }

    @Operation(description = "Get total number all the Files for an specific project in PRIDE.", tags = {"projects"})
    @RequestMapping(value = "/projects/count", method = RequestMethod.GET)
    public Mono<Long> getProjectsCount() {
        List<String> submissionType = Collections.singletonList("AFFINITY");
        return projectMongoClient.countAllBySubmissionTypeIn(submissionType);
    }

//
//        Flux<MongoPrideProject> allProjectsPageMono = projectMongoClient.findAllBySubmissionTypeIn(submissionType, pageSizeFinal, pageFinal);
//        return allProjectsPageMono.map(mongoProjectsPage -> {
//            PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(MassSpecProjectController.class, ProjectResource.class, fileMongoClient);
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
//    }

    //        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
//        page = pageParams.getKey();
//        pageSize = pageParams.getValue();
//        Sort.Direction direction = Sort.Direction.DESC;
//        if (sortDirection.equalsIgnoreCase("ASC")) {
//            direction = Sort.Direction.ASC;
//        }
//
//        Page<MongoPrideProject> mongoProjects = mongoProjectService.findAll(PageRequest.of(page, pageSize, direction, sortFields.split(",")));

    //FIXME TODO fileter for AFFINITY SubmissionType should be done at the DB query .. Otherwise it will end up with lots of empty pages.
//        List<MongoPrideProject> filteredList = mongoProjects.stream().filter(project -> project.getSubmissionType().equals("AFFINITY")).collect(Collectors.toList());
//        mongoProjects = new PageImpl<>(filteredList, PageRequest.of(page, pageSize, direction, sortFields.split(",")), filteredList.size());
//
//        PrideProjectResourceAssembler assembler = new PrideProjectResourceAssembler(AffinityProjectController.class, ProjectResource.class, mongoFileService);
//
//        CollectionModel<ProjectResource> resources = assembler.toCollectionModel(mongoProjects);
//
//        long totalElements = mongoProjects.getTotalElements();
//        long totalPages = mongoProjects.getTotalPages();
//        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(pageSize, page, totalElements, totalPages);
//
//        PagedModel<ProjectResource> pagedResources = PagedModel.of(resources.getContent(), pageMetadata,
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, page, sortDirection, sortFields)).withSelfRel(),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page + 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.next.name()),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) WsUtils.validatePage(page - 1, totalPages), sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.previous.name()),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, 0, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.first.name()),
//                linkTo(methodOn(AffinityProjectController.class).getProjects(pageSize, (int) totalPages, sortDirection, sortFields))
//                        .withRel(WsContastants.HateoasEnum.last.name())
//        );
//
//        return new HttpEntity<>(pagedResources);
//    }

}
