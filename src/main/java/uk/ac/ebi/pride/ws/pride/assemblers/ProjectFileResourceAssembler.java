package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideFile;
import uk.ac.ebi.pride.ws.pride.controllers.FileController;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideFile;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideFileResource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ypriverol
 */
public class ProjectFileResourceAssembler extends ResourceAssemblerSupport<MongoPrideFile, PrideFileResource> {

    public ProjectFileResourceAssembler(Class<?> controller, Class<PrideFileResource> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public PrideFileResource toResource(MongoPrideFile mongoFile) {
        PrideFile file = PrideFile.builder()
                .accession(mongoFile.getAccession())
                .additionalAttributes(mongoFile.getAdditionalAttributes())
                .analysisAccessions(mongoFile.getAnalysisAccessions())
                .projectAccessions(mongoFile.getProjectAccessions())
                .compress(mongoFile.isCompress())
                .fileCategory(mongoFile.getFileCategory())
                .fileName(mongoFile.getFileName())
                .fileSizeBytes(mongoFile.getFileSizeBytes())
                .md5Checksum(mongoFile.getMd5Checksum())
                .publicationDate(mongoFile.getPublicationDate())
                .publicFileLocations(mongoFile.getPublicFileLocations())
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
