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
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.service.projects.PrideFileMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectFileResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideFileResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
import java.util.Optional;

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
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/search/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources<PrideFileResource>> files(@RequestParam(value="filter", required = false, defaultValue = "''") String filter,
                                                                @RequestParam(value="pageSize", defaultValue = "100", required = false) int pageSize,
                                                                @RequestParam(value="page", defaultValue = "0" ,  required = false) int page){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();

        Page<MongoPrideFile> projectFiles = mongoFileService.searchFiles(filter, PageRequest.of(page, pageSize));
        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(FileController.class).files(filter, pageSize, page))
                        .withSelfRel(),
                linkTo(methodOn(FileController.class).files(filter, pageSize, page + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(FileController.class).files(filter, pageSize, page - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(FileController.class).files(filter, pageSize, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(FileController.class).files(filter, pageSize, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }

    @ApiOperation(notes = "Get a File from PRIDE database", value = "files", nickname = "getFile", tags = {"files"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/files/{fileAccession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PrideFileResource> getFile(@PathVariable(value="fileAccession") String fileAccession) {

        Optional<MongoPrideFile> file = mongoFileService.findByFileAccession(fileAccession);

        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
        PrideFileResource resource = assembler.toResource(file.get());

        return new HttpEntity<>(resource);
    }

    @ApiOperation(notes = "Get all Files in PRIDE Archive", value = "files", nickname = "getFiles", tags = {"files"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE})
    public HttpEntity<PagedResources> getFiles(@RequestParam(value="pageSize", defaultValue = "100", required = false) int pageSize,
                                                  @RequestParam(value="page", defaultValue = "0" ,  required = false) int page) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();


        Page<MongoPrideFile> projectFiles = mongoFileService.findAll(PageRequest.of(page, pageSize));
        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(FileController.class).getFiles(pageSize, page))
                        .withSelfRel(),
                linkTo(methodOn(FileController.class).getFiles( pageSize, page + 1))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(FileController.class).getFiles(pageSize, page - 1))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(FileController.class).getFiles( pageSize, 0))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(FileController.class).getFiles(pageSize, (int) totalPages))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;

        return new HttpEntity<>(pagedResources);
    }
}
