package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.ParamProvider;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.archive.model.files.MongoPrideFile;
import uk.ac.ebi.pride.mongodb.archive.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.mongodb.archive.service.files.PrideFileMongoService;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.controllers.project.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProject;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 *
 * @author ypriverol
 */
@Slf4j
public class PrideProjectResourceAssembler extends ResourceAssemblerSupport<MongoPrideProject, ProjectResource> {

    private PrideFileMongoService mongoFileService;

    public PrideProjectResourceAssembler(Class<?> controllerClass, Class<ProjectResource> resourceType,
                                         PrideFileMongoService mongoFileService) {
        super(controllerClass, resourceType);
        this.mongoFileService = mongoFileService;
    }

    @Override
    public ProjectResource toResource(MongoPrideProject mongoPrideProject) {
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(mongoPrideProject.getAccession())).withSelfRel());

        Method method = null;
        try {
            method = ProjectController.class.getMethod("getFilesByProject", String.class, String.class, Integer.class, Integer.class, String.class, String.class);
            Link link = ControllerLinkBuilder.linkTo(method, mongoPrideProject.getAccession(), "", WsContastants.MAX_PAGINATION_SIZE, 0, "DESC", PrideArchiveField.SUBMISSION_DATE).withRel(WsContastants.HateoasEnum.files.name());
            links.add(link);
            links.add(new Link(new UriTemplate(getFtpPath(mongoPrideProject)), "datasetFtpUrl"));
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
        }

        return new ProjectResource(transform(mongoPrideProject), links);
    }

    private String getFtpPath(MongoPrideProject mongoPrideProject) {
        String ftpPath = null;
        //Due to this issue : https://github.com/PRIDE-Archive/pride-archive-api/issues/108 (Datasets made public on 30-12-2021 gets wrong FTP path)
        //We have get FTP path from files ftp path stored in mongo
        List<MongoPrideFile> mongoFiles = mongoFileService.findFilesByProjectAccession(mongoPrideProject.getAccession());
        if (mongoFiles != null && !mongoFiles.isEmpty()) {
            MongoPrideFile mongoPrideFile = mongoFiles.get(0);
            Set<? extends CvParamProvider> publicFileLocations = mongoPrideFile.getPublicFileLocations();
            Optional<String> ftpPathOptional = publicFileLocations.stream().filter(l -> l.getAccession().equals("PRIDE:0000469")).map(ParamProvider::getValue).findFirst();
            if (ftpPathOptional.isPresent()) {
                ftpPath = ftpPathOptional.get();
            }
            ftpPath = ftpPath.substring(0, ftpPath.lastIndexOf("/"));
        }

        if (ftpPath == null) {
            Date publicationDate = mongoPrideProject.getPublicationDate();
            SimpleDateFormat year = new SimpleDateFormat("YYYY");
            SimpleDateFormat month = new SimpleDateFormat("MM");
            ftpPath = "ftp://ftp.pride.ebi.ac.uk/pride/data/archive/" + year.format(publicationDate).toUpperCase() + "/" + month.format(publicationDate).toUpperCase() + "/" + mongoPrideProject.getAccession();
        }
        return ftpPath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProjectResource> toResources(Iterable<? extends MongoPrideProject> entities) {

        List<ProjectResource> projects = new ArrayList<>();

        for (MongoPrideProject mongoPrideProject : entities) {
            PrideProject project = transform(mongoPrideProject);
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(mongoPrideProject.getAccession())).withSelfRel());
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getFilesByProject(mongoPrideProject.getAccession(), "", WsContastants.MAX_PAGINATION_SIZE, 0, "DESC", PrideArchiveField.SUBMISSION_DATE)).withRel(WsContastants.HateoasEnum.files.name()));
            links.add(new Link(new UriTemplate(getFtpPath(mongoPrideProject)), "datasetFtpUrl"));
            projects.add(new ProjectResource(project, links));
        }

        return projects;
    }

    /**
     * Transform the original mongo Project to {@link PrideProject} that is used to external users.
     *
     * @param mongoPrideProject {@link MongoPrideProject}
     * @return Pride Project
     */
    public PrideProject transform(MongoPrideProject mongoPrideProject) {
        Collection<CvParam> additionalAttributes = mongoPrideProject.getAttributes();
        String license = null;
        try {
            license = WsUtils.getLicenseFromDate(mongoPrideProject.getSubmissionDate());
        } catch (ParseException e) {
            log.info("Error generating the license for dataset -- " + mongoPrideProject.getAccession());
        }

        if (additionalAttributes == null)
            additionalAttributes = new ArrayList<>();

        additionalAttributes.add(new CvParam("PRIDE", "PRIDE:0000411", "Dataset FTP location", getFtpPath(mongoPrideProject)));

        return PrideProject.builder()
                .accession(mongoPrideProject.getAccession())
                .title(mongoPrideProject.getTitle())
                .references(new HashSet<>(mongoPrideProject.getCompleteReferences()))
                .projectDescription(mongoPrideProject.getDescription())
                .projectTags(mongoPrideProject.getProjectTags())
                .additionalAttributes(additionalAttributes)
                .affiliations(mongoPrideProject.getAllAffiliations())
                .identifiedPTMStrings(new HashSet<>(mongoPrideProject.getPtmList()))
                .sampleProcessingProtocol(mongoPrideProject.getSampleProcessingProtocol())
                .dataProcessingProtocol(mongoPrideProject.getDataProcessingProtocol())
                .countries(mongoPrideProject.getCountries() != null ? new HashSet<>(mongoPrideProject.getCountries()) : Collections.EMPTY_SET)
                .keywords(mongoPrideProject.getKeywords())
                .doi(mongoPrideProject.getDoi().isPresent() ? mongoPrideProject.getDoi().get() : null)
                .submissionType(mongoPrideProject.getSubmissionType())
                .publicationDate(mongoPrideProject.getPublicationDate())
                .submissionDate(mongoPrideProject.getSubmissionDate())
                .instruments(new ArrayList<>(mongoPrideProject.getInstrumentsCvParams()))
                .quantificationMethods(new ArrayList<>(mongoPrideProject.getQuantificationParams()))
                .softwares(new ArrayList<>(mongoPrideProject.getSoftwareParams()))
                .experimentTypes(new ArrayList<>(mongoPrideProject.getExperimentTypes()))
                .submitters(new ArrayList<>(mongoPrideProject.getSubmittersContacts()))
                .labPIs(new ArrayList<>(mongoPrideProject.getLabHeadContacts()))
                .organisms(WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_ORGANISM))
                .diseases(WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_DISEASE))
                .organismParts(WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_ORGANISM_PART))
                .sampleAttributes(mongoPrideProject.getSampleAttributes() != null ? new ArrayList(mongoPrideProject.getSampleAttributes()) : Collections.emptyList())
                .license(license)
                .build();
    }
}
