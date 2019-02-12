package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.mongodb.archive.model.msrun.MongoPrideMSRun;
import uk.ac.ebi.pride.ws.pride.controllers.file.FileController;
import uk.ac.ebi.pride.ws.pride.models.file.PrideMSRun;
import uk.ac.ebi.pride.ws.pride.models.file.PrideMSRunResource;
import uk.ac.ebi.pride.ws.pride.transformers.Transformer;

import java.util.ArrayList;
import java.util.List;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 *
 * @author ypriverol on 24/10/2018.
 */
public class ProjectMSRunResourceAssembler extends ResourceAssemblerSupport<MongoPrideMSRun, PrideMSRunResource> {

    public ProjectMSRunResourceAssembler(Class<?> controller, Class<PrideMSRunResource> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public PrideMSRunResource toResource(MongoPrideMSRun mongoFile) {

        PrideMSRun msRun = Transformer.transformMSRun(mongoFile);
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(FileController.class).getFile(mongoFile.getAccession())).withSelfRel());
        return new PrideMSRunResource(msRun, links);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PrideMSRunResource> toResources(Iterable<? extends MongoPrideMSRun> entities) {

        List<PrideMSRunResource> datasets = new ArrayList<>();

        for(MongoPrideMSRun mongoFile: entities){
            datasets.add(toResource(mongoFile));
        }

        return datasets;
    }
}
