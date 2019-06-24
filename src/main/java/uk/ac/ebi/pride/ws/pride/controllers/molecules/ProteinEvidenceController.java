package uk.ac.ebi.pride.ws.pride.controllers.molecules;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.molecules.model.protein.PrideMongoProteinEvidence;
import uk.ac.ebi.pride.mongodb.molecules.service.molecules.PrideMoleculesMongoService;
import uk.ac.ebi.pride.utilities.util.Triple;
import uk.ac.ebi.pride.utilities.util.Tuple;
import uk.ac.ebi.pride.ws.pride.assemblers.ProteinEvidenceAssembler;
import uk.ac.ebi.pride.ws.pride.models.molecules.ProteinEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.APIError;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.List;
import java.util.Optional;

@RestController
public class ProteinEvidenceController {

    final PrideMoleculesMongoService moleculesMongoService;

    @Autowired
    public ProteinEvidenceController(PrideMoleculesMongoService moleculesMongoService) {
        this.moleculesMongoService = moleculesMongoService;
    }


    @ApiOperation(notes = "Get the protein evidence for the specific accession",
            value = "molecules", nickname = "getProteinEvidence", tags = {"molecules"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/proteinevidences/{accession}", method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> getProteinEvidence(@PathVariable(value = "accession") String accession){

        Optional<PrideMongoProteinEvidence> mongoProteinEvidence = Optional.empty();
        ProteinEvidenceAssembler assembler = new ProteinEvidenceAssembler(ProteinEvidenceController.class,
                ProteinEvidenceResource.class);
        try {
            Triple<String, String, String> usi = WsUtils.parseProteinEvidenceAccession(accession);
             mongoProteinEvidence = moleculesMongoService.findProteinsEvidence(usi.getFirst(),
                    usi.getSecond(), usi.getThird());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return mongoProteinEvidence.<ResponseEntity<Object>>map(mongoPrideProject ->
                new ResponseEntity<>(assembler.toResource(mongoPrideProject), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(WsContastants.PROTEIN_NOT_FOUND
                        + accession + WsContastants.CONTACT_PRIDE, new HttpHeaders(), HttpStatus.BAD_REQUEST));
    }




    @ApiOperation(notes = "Get all the protein evidences", value = "molecules", nickname = "getProteinEvidences", tags = {"molecules"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/proteinevidences", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public HttpEntity<Object> projects(
            @RequestParam(value = "projectAccession", required = false) String projectAccession,
            @RequestParam(value = "assayAccession" , required = false) String assayAccession,
            @RequestParam(value = "reportedAccession", required = false) String reportedAccession,
            @RequestParam(value="pageSize", defaultValue = "100", required = false) int pageSize,
            @RequestParam(value="page", defaultValue = "0" ,  required = false) int page,
            @RequestParam(value="sortDirection", defaultValue = "DESC" ,  required = false) String sortDirection,
            @RequestParam(value="sortConditions", defaultValue = PrideArchiveField.EXTERNAL_PROJECT_ACCESSION,  required = false) String sortFields){

        Tuple<Integer, Integer> pageParams = WsUtils.validatePageLimit(page, pageSize);
        page = pageParams.getKey();
        pageSize = pageParams.getValue();
        Sort.Direction direction = Sort.Direction.DESC;
        if(sortDirection.equalsIgnoreCase("ASC")){
            direction = Sort.Direction.ASC;
        }

        Page<PrideMongoProteinEvidence> mongoProteins = moleculesMongoService.findAllProteinEvidences(PageRequest.of(page, pageSize, direction, sortFields.split(",")));


        return new HttpEntity<>(mongoProteins);
    }
}
