package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.ws.pride.controllers.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProject;
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
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(mongoPrideProject.getAccession())).withSelfRel());
        return new ProjectResource(transform(mongoPrideProject), links);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProjectResource> toResources(Iterable<? extends MongoPrideProject> entities) {

        List<ProjectResource> projects = new ArrayList<>();

        for(MongoPrideProject mongoPrideProject: entities){
            PrideProject project = transform(mongoPrideProject);
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(mongoPrideProject.getAccession())).withSelfRel());
            projects.add(new ProjectResource(project, links));
        }

        return projects;
    }

    /**
     * Transform the original mongo Project to {@link PrideProject} that is used to external users.
     * @param mongoPrideProject {@link MongoPrideProject}
     * @return Pride Project
     */
    public PrideProject transform(MongoPrideProject mongoPrideProject){
        return PrideProject.builder().accession(mongoPrideProject.getAccession())
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
    }

}
