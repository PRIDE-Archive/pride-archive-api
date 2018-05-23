package uk.ac.ebi.pride.ws.pride.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideFileMongoService;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectFileResourceAssembler;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideFile;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideFileResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.ErrorInfo;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author ypriverol
 */

@RestController
public class FileController {

    final PrideFileMongoService mongoFileService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    @Autowired
    public FileController(PrideFileMongoService mongoFileService, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.mongoFileService = mongoFileService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }


    @ApiOperation(notes = "Search all public files in PRIDE Archive. The _filter_ parameter provides allows the method " +
            " to filter the results for specific values. The strcuture of the filter _is_: field1==value1, field2==value2.", value = "files", nickname = "searchFiles", tags = {"files"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = ErrorInfo.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorInfo.class)
    })
    @RequestMapping(value = "/search/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<PrideFileResource>> files(@RequestParam(value="Filter by property", required = false, defaultValue = "''") String filter,
                                                                @RequestParam(value="Number files per page ", defaultValue = "100", required = false) int size,
                                                                @RequestParam(value="Page number", defaultValue = "0" ,  required = false) int start){

        Page<MongoPrideFile> projectFiles = mongoFileService.searchFiles(filter, PageRequest.of(start, size));
        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = totalElements / size;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(size, start, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(FileController.class).files(filter, size, start))
                        .withSelfRel(),
                linkTo(methodOn(FileController.class).files(filter, size, start + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(FileController.class).files(filter, size, start - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(FileController.class).files(filter, size, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(FileController.class).files(filter, size, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    public HttpEntity<PrideFile> getFile(String fileAccession) {
        return null;
    }
}
