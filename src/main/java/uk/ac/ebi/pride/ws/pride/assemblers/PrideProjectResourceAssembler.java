package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.solr.indexes.pride.utils.StringUtils;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.controllers.project.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProject;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.lang.reflect.Method;
import java.util.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * @author ypriverol
 */
@Slf4j
public class PrideProjectResourceAssembler extends ResourceAssemblerSupport<MongoPrideProject, ProjectResource> {

    public PrideProjectResourceAssembler(Class<?> controllerClass, Class<ProjectResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public ProjectResource toResource(MongoPrideProject mongoPrideProject) {
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(mongoPrideProject.getAccession())).withSelfRel());

        // This needs to be build in a different way

        Method method = null;
        try {
            method = ProjectController.class.getMethod("getFilesByProject", String.class, String.class, Integer.class, Integer.class, String.class, String.class);
            Link link = ControllerLinkBuilder.linkTo(method, mongoPrideProject.getAccession(), "", WsContastants.MAX_PAGINATION_SIZE, 0, "DESC" , PrideArchiveField.SUBMISSION_DATE).withRel(WsContastants.HateoasEnum.files.name());
            links.add(link);
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(),e);
        }

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
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getFilesByProject(mongoPrideProject.getAccession(), "", WsContastants.MAX_PAGINATION_SIZE, 0,"DESC",PrideArchiveField.SUBMISSION_DATE)).withRel(WsContastants.HateoasEnum.files.name()));
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
        return PrideProject.builder()
                .accession(mongoPrideProject.getAccession())
                .title(mongoPrideProject.getTitle())
                .references(new HashSet<>(mongoPrideProject.getCompleteReferences()))
                .projectDescription(mongoPrideProject.getDescription())
                .projectTags(mongoPrideProject.getProjectTags())
                .additionalAttributes(mongoPrideProject.getAttributes())
                .affiliations(mongoPrideProject.getAllAffiliations())
                .identifiedPTMStrings(new HashSet<>(mongoPrideProject.getPtmList()))
                .sampleProcessingProtocol(mongoPrideProject.getSampleProcessingProtocol())
                .dataProcessingProtocol(mongoPrideProject.getDataProcessingProtocol())
                .countries(mongoPrideProject.getCountries() != null ? new HashSet<>(mongoPrideProject.getCountries()) : Collections.EMPTY_SET)
                .keywords(mongoPrideProject.getKeywords())
                .doi(mongoPrideProject.getDoi().isPresent()?mongoPrideProject.getDoi().get():null)
                .publicationDate(mongoPrideProject.getPublicationDate())
                .submissionDate(mongoPrideProject.getSubmissionDate())
                .instruments(new ArrayList<>(mongoPrideProject.getInstrumentsCvParams()))
                .quantificationMethods(new ArrayList<>(mongoPrideProject.getQuantificationParams()))
                .softwares(new ArrayList<>(mongoPrideProject.getSoftwareParams()))
                .submitters(new ArrayList<>(mongoPrideProject.getSubmittersContacts()))
                .labPIs(new ArrayList<>(mongoPrideProject.getLabHeadContacts()))
                .organisms(getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_ORGANISM))
                .diseases(getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_DISEASE))
                .organismParts(getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_ORGANISM_PART))
                .sampleAttributes(mongoPrideProject.getSampleAttributes() !=null? new ArrayList(mongoPrideProject.getSampleAttributes()): Collections.emptyList())
                .build();
    }

    private Collection<CvParamProvider> getCvTermsValues(List<Tuple<CvParam, Set<CvParam>>> samplesDescription, CvTermReference efoTerm) {
        Set<CvParamProvider> resultTerms = new HashSet<>();
        samplesDescription.stream()
                .filter(x -> x.getKey().getAccession().equalsIgnoreCase(efoTerm.getAccession()))
                .forEach( y-> y.getValue().forEach(z-> resultTerms.add( new CvParam(z.getCvLabel(), z.getAccession(), StringUtils.convertSentenceStyle(z.getName()), z.getValue()))));
        return resultTerms;
    }






}
