package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.DefaultCvParam;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.ws.pride.controllers.FileController;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFile;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFileResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ypriverol
 */
public class ProjectFileResourceAssembler extends ResourceAssemblerSupport<MongoPrideFile, PrideFileResource> {

    public ProjectFileResourceAssembler(Class<?> controller, Class<PrideFileResource> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public PrideFileResource toResource(MongoPrideFile mongoFile) {

        List<CvParamProvider> additionalAttributes = mongoFile.getAdditionalAttributes()!=null?mongoFile.getAdditionalAttributes().stream()
                .map( x-> new DefaultCvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue())).collect(Collectors.toList()) : Collections.emptyList();
        List<CvParamProvider> publicFileLocations = mongoFile.getPublicFileLocations() != null? mongoFile.getPublicFileLocations().stream()
                .map( x -> new DefaultCvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue())).collect(Collectors.toList()) : Collections.emptyList();

        System.out.println(mongoFile);

        CvParamProvider category = mongoFile.getFileCategory() != null? new DefaultCvParam(mongoFile.getFileCategory().getCvLabel(),
                mongoFile.getFileCategory().getAccession(), mongoFile.getFileCategory().getName(), mongoFile.getFileCategory().getValue()): null;

        PrideFile file = PrideFile.builder()
                .accession(mongoFile.getAccession())
                .additionalAttributes(additionalAttributes)
                .analysisAccessions(mongoFile.getAnalysisAccessions())
                .projectAccessions(mongoFile.getProjectAccessions())
                .compress(mongoFile.isCompress())
                .fileCategory(category)
                .fileName(mongoFile.getFileName())
                .fileSizeBytes(mongoFile.getFileSizeBytes())
                .md5Checksum(mongoFile.getMd5Checksum())
                .publicationDate(mongoFile.getPublicationDate())
                .publicFileLocations(publicFileLocations)
                .updatedDate(mongoFile.getUpdatedDate())
                .submissionDate(mongoFile.getSubmissionDate())
                .build();
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(FileController.class).getFile(mongoFile.getAccession())).withSelfRel());
        return new PrideFileResource(file, links);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PrideFileResource> toResources(Iterable<? extends MongoPrideFile> entities) {

        List<PrideFileResource> datasets = new ArrayList<>();

        for(MongoPrideFile mongoFile: entities){
            datasets.add(toResource(mongoFile));
        }

        return datasets;
    }
}
