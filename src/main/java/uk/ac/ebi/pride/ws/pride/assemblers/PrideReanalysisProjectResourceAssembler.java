package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideReanalysisProject;
import uk.ac.ebi.pride.ws.pride.controllers.project.MassSpecProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectReanalysisResource;

import java.util.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * @author Suresh Hewapathirana
 */
@Slf4j
public class PrideReanalysisProjectResourceAssembler extends ResourceAssemblerSupport<MongoPrideReanalysisProject, ProjectReanalysisResource> {

    public PrideReanalysisProjectResourceAssembler(Class<?> controllerClass, Class<ProjectReanalysisResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public ProjectReanalysisResource toResource(MongoPrideReanalysisProject reanalysisProject) {
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(MassSpecProjectController.class).getReanalysisProject(reanalysisProject.getAccession())).withSelfRel());

        return new ProjectReanalysisResource(reanalysisProject, links);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProjectReanalysisResource> toResources(Iterable<? extends MongoPrideReanalysisProject> entities) {

        List<ProjectReanalysisResource> projects = new ArrayList<>();

        for(MongoPrideReanalysisProject reanalysisProject: entities){
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(MassSpecProjectController.class).getReanalysisProject(reanalysisProject.getAccession())).withSelfRel());
            projects.add(new ProjectReanalysisResource(reanalysisProject, links));
        }
        return projects;
    }
}
