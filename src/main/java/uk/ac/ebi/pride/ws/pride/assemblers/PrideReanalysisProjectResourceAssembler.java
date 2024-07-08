package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideReanalysisProject;
import uk.ac.ebi.pride.ws.pride.controllers.project.MassSpecProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectReanalysisResource;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 *
 * @author Suresh Hewapathirana
 */
@Slf4j
public class PrideReanalysisProjectResourceAssembler extends RepresentationModelAssemblerSupport<MongoPrideReanalysisProject, ProjectReanalysisResource> {

    public PrideReanalysisProjectResourceAssembler(Class<?> controllerClass, Class<ProjectReanalysisResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public ProjectReanalysisResource toModel(MongoPrideReanalysisProject reanalysisProject) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(MassSpecProjectController.class).getReanalysisProject(reanalysisProject.getAccession())).withSelfRel());

        return new ProjectReanalysisResource(reanalysisProject, links);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionModel<ProjectReanalysisResource> toCollectionModel(Iterable<? extends MongoPrideReanalysisProject> entities) {

        List<ProjectReanalysisResource> projects = new ArrayList<>();

        for (MongoPrideReanalysisProject reanalysisProject : entities) {
            List<Link> links = new ArrayList<>();
            links.add(linkTo(methodOn(MassSpecProjectController.class).getReanalysisProject(reanalysisProject.getAccession())).withSelfRel());
            projects.add(new ProjectReanalysisResource(reanalysisProject, links));
        }
        return CollectionModel.of(projects);
    }
}
