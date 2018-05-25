package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.ws.pride.controllers.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectResource;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProject;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * @author ypriverol
 */
public class PrideProjectResourceAssembler extends ResourceAssemblerSupport<MongoPrideProject, ProjectResource> {

    public PrideProjectResourceAssembler(Class<?> controllerClass, Class<ProjectResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public ProjectResource toResource(MongoPrideProject mongoPrideProject) {
        PrideProject project = PrideProject.builder().accession(mongoPrideProject.getAccession())
                .projectDescription(mongoPrideProject.getDescription())
                .projectTags(mongoPrideProject.getProjectTags())
                .additionalAttributes(mongoPrideProject.getAttributes())
                .affiliations(mongoPrideProject.getAllAffiliations())
                .identifiedPTMStrings(mongoPrideProject.getPtmList().stream().collect(Collectors.toSet()))
                .sampleProcessingProtocol(mongoPrideProject.getSampleProcessingProtocol())
                .dataProcessingProtocol(mongoPrideProject.getDataProcessingProtocol())
                .countries(mongoPrideProject.getCountries().stream().collect(Collectors.toSet()))
                .keywords(mongoPrideProject.getKeywords())
                .doi(mongoPrideProject.getDoi().get())
                .publicationDate(mongoPrideProject.getPublicationDate())
                .submissionDate(mongoPrideProject.getSubmissionDate()).build();
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(mongoPrideProject.getAccession())).withSelfRel());
        return new ProjectResource(project, links);
    }
}
