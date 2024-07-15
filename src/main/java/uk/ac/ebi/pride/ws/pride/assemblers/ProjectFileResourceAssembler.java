package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.mongo.commons.model.files.MongoPrideFile;
import uk.ac.ebi.pride.ws.pride.controllers.file.FileController;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFile;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFileResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author ypriverol
 */
@Slf4j
public class ProjectFileResourceAssembler extends RepresentationModelAssemblerSupport<MongoPrideFile, PrideFileResource> {

    public ProjectFileResourceAssembler(Class<?> controller, Class<PrideFileResource> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public PrideFileResource toModel(MongoPrideFile mongoFile) {

        Set<CvParamProvider> additionalAttributes = mongoFile.getAdditionalAttributes() != null ? mongoFile.getAdditionalAttributes().stream()
                .map(x -> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue())).collect(Collectors.toSet()) : Collections.emptySet();
        Set<CvParamProvider> publicFileLocations = mongoFile.getPublicFileLocations() != null ? mongoFile.getPublicFileLocations().stream()
                .map(x -> {
                    String value = getFTPUrl(x.getValue());
                    return new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), value);
                }).collect(Collectors.toSet()) : Collections.emptySet();

        log.debug(mongoFile.toString());

        CvParamProvider category = mongoFile.getFileCategory() != null ? new CvParam(mongoFile.getFileCategory().getCvLabel(),
                mongoFile.getFileCategory().getAccession(), mongoFile.getFileCategory().getName(), mongoFile.getFileCategory().getValue()) : null;

        PrideFile file = PrideFile.builder()
                .accession(mongoFile.getAccession())
                .additionalAttributes(additionalAttributes)
                .analysisAccessions(mongoFile.getAnalysisAccessions())
                .projectAccessions(mongoFile.getProjectAccessions())
                .compress(mongoFile.isCompress())
                .fileCategory(category)
                .fileName(mongoFile.getFileName())
                .fileSizeBytes(mongoFile.getFileSizeBytes())
                .checksum(mongoFile.getChecksum())
                .publicationDate(mongoFile.getPublicationDate())
                .publicFileLocations(publicFileLocations)
                .updatedDate(mongoFile.getUpdatedDate())
                .submissionDate(mongoFile.getSubmissionDate())
                .build();
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(FileController.class).getFile(mongoFile.getAccession())).withSelfRel());
        return new PrideFileResource(file, links);
    }

    public static String getFTPUrl(String value) {
        value = value.startsWith("ftp://") ? value.replaceAll("#", "%23") : value;
//        value = value.replace("ftp://ftp.pride.ebi.ac.uk/pride/data/archive/", "ftp://ftp.ebi.ac.uk/pride-archive/");
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionModel<PrideFileResource> toCollectionModel(Iterable<? extends MongoPrideFile> entities) {

        List<PrideFileResource> datasets = new ArrayList<>();

        for (MongoPrideFile mongoFile : entities) {
            datasets.add(toModel(mongoFile));
        }

        return CollectionModel.of(datasets);
    }
}
