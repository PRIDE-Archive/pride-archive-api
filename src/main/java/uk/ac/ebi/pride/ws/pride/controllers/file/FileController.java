package uk.ac.ebi.pride.ws.pride.controllers.file;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.ProjectFileResourceAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFileResource;
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

    @ApiOperation(notes = "Get a File from PRIDE database by FileName", value = "files", nickname = "fileByName", tags = {"files"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class),
            @ApiResponse(code = 204, message = "Content not found with the given parameters", response = APIError.class)
    })
    @RequestMapping(value = "/files/fileByName", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity getFileByName(@RequestParam(value="fileName") String fileName,
                                        @RequestParam(value = "projectAccession", defaultValue = "",
                                                required = false) String projectAccession
    ) {
        Page<MongoPrideFile> file = null;
        if(projectAccession != null && !projectAccession.isEmpty())
            file = mongoFileService.searchFiles("fileName==" + fileName + ",projectAccessions=in=" + projectAccession, PageRequest.of(0, 100));
        else
            file = mongoFileService.searchFiles("fileName==" + fileName, PageRequest.of(0, 100));

        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
        PrideFileResource resource = null;
        if(file.getTotalElements() == 1){
            resource = assembler.toResource(file.getContent().get(0));
        }
        if(resource == null)
            return new ResponseEntity(null, HttpStatus.NO_CONTENT);

        return new ResponseEntity(resource, HttpStatus.OK);
    }

    @ApiOperation(notes = "Get a File from PRIDE database", value = "files", nickname = "getFile", tags = {"files"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/files/{accession}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PrideFileResource> getFile(@PathVariable(value="accession") String accession) {

        Optional<MongoPrideFile> file = mongoFileService.findByFileAccession(accession);

        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
        PrideFileResource resource = assembler.toResource(file.get());

        return new HttpEntity<>(resource);
    }

    @ApiOperation(notes = "Get a Files for Pride Project", value = "files", nickname = "byProject", tags = {"files"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/files/byProject", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<PrideFileResource>> getByProject(@RequestParam(value="accession") String accession) {

        List<MongoPrideFile> files = mongoFileService.findFilesByProjectAccession(accession);

        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);
        List<PrideFileResource> resources = null;

        if(files != null && !files.isEmpty()){
            resources = assembler.toResources(files);
            return new ResponseEntity<>(resources, HttpStatus.OK);
        }

        return new ResponseEntity<>(resources, HttpStatus.NO_CONTENT);
    }

    @ApiOperation(notes = "Get all Files in PRIDE Archive", value = "files", nickname = "getFiles", tags = {"files"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/files", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<PagedResources> getFiles(@RequestParam(value="filter", required = false,
            defaultValue = "''") String filter,
                                               @RequestParam(value="pageSize", defaultValue = "100",
                                                       required = false) int pageSize,
                                               @RequestParam(value="page", defaultValue = "0",
                                                       required = false) int page,
                                               @RequestParam(value="sortDirection", defaultValue = "DESC",
                                                       required = false) String sortDirection,
                                               @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.SUBMISSION_DATE,  required = false) String sortFields) {

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<MongoPrideFile> projectFiles;
        if(filter!=null && filter.trim().length()>0){
            projectFiles = mongoFileService.searchFiles(filter, PageRequest.of(page, pageSize,direction,sortFields.split(",")));
        }else{
            projectFiles = mongoFileService.findAll(PageRequest.of(page, pageSize,direction,sortFields.split(",")));
        }

        ProjectFileResourceAssembler assembler = new ProjectFileResourceAssembler(FileController.class, PrideFileResource.class);

        List<PrideFileResource> resources = assembler.toResources(projectFiles);

        long totalElements = projectFiles.getTotalElements();
        long totalPages = totalElements / pageSize;
        PagedResources.PageMetadata pageMetadata = new PagedResources.PageMetadata(pageSize, page, totalElements, totalPages);

        PagedResources<PrideFileResource> pagedResources = new PagedResources<>(resources, pageMetadata,
                linkTo(methodOn(FileController.class).getFiles(filter, pageSize, page,sortDirection,sortFields))
                        .withSelfRel(),
                linkTo(methodOn(FileController.class).getFiles(filter,  pageSize, (int) WsUtils.validatePage(page + 1, totalPages),sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.next.name()),
                linkTo(methodOn(FileController.class).getFiles(filter, pageSize, (int) WsUtils.validatePage(page - 1, totalPages),sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.previous.name()),
                linkTo(methodOn(FileController.class).getFiles(filter,  pageSize, 0,sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.first.name()),
                linkTo(methodOn(FileController.class).getFiles(filter, pageSize, (int) totalPages,sortDirection,sortFields))
                        .withRel(WsContastants.HateoasEnum.last.name())
        ) ;
        return new HttpEntity<>(pagedResources);
    }
}
