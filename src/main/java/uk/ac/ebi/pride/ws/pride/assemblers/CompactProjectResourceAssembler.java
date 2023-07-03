package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.solr.commons.PrideSolrProject;
import uk.ac.ebi.pride.ws.pride.controllers.project.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProject;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectResource;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author ypriverol
 */
@Slf4j
public class CompactProjectResourceAssembler extends ResourceAssemblerSupport<PrideSolrProject, CompactProjectResource> {

    public CompactProjectResourceAssembler(Class<?> controller, Class<CompactProjectResource> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public CompactProjectResource toResource(PrideSolrProject prideSolrDataset) {
        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(prideSolrDataset.getAccession())).withSelfRel());
        return new CompactProjectResource(transform(prideSolrDataset), links);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CompactProjectResource> toResources(Iterable<? extends PrideSolrProject> entities) {

        List<CompactProjectResource> datasets = new ArrayList<>();

        for(PrideSolrProject prideSolrDataset: entities){
            CompactProject dataset = transform(prideSolrDataset);
            if(entities instanceof FacetAndHighlightPage){
                FacetAndHighlightPage<PrideSolrProject> facetPages = (FacetAndHighlightPage<PrideSolrProject>) entities;
                dataset.setHighlights(facetPages.getHighlights(prideSolrDataset).stream().collect(Collectors.toMap(x -> x.getField().getName(), HighlightEntry.Highlight::getSnipplets)));
            }
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(prideSolrDataset.getAccession())).withSelfRel());
            Date publicationDate = dataset.getPublicationDate();
            SimpleDateFormat year = new SimpleDateFormat("YYYY");
            SimpleDateFormat month = new SimpleDateFormat("MM");
            String ftpPath = "ftp://ftp.pride.ebi.ac.uk/pride/data/archive/" + year.format(publicationDate).toUpperCase() + "/" + month.format(publicationDate).toUpperCase() + "/" + dataset.getAccession();
            links.add(new Link(new UriTemplate(ftpPath), "datasetFtpUrl"));

            datasets.add(new CompactProjectResource(dataset, links));
        }

        return datasets;
    }

    /**
     * Transform  Solr Project into Compact Project
     * @param prideSolrDataset solr project
     * @return CompactProject
     */
    private CompactProject transform(PrideSolrProject prideSolrDataset){

        String license = null;
        try {
            license = WsUtils.getLicenseFromDate(prideSolrDataset.getSubmissionDate());
        } catch (ParseException e) {
            log.info("Error generating the license for dataset -- " + prideSolrDataset.getAccession());
        }

        return  CompactProject.builder()
                .accession(prideSolrDataset.getAccession())
                .title(prideSolrDataset.getTitle())
                .projectDescription(prideSolrDataset.getProjectDescription())
                .additionalAttributes(prideSolrDataset.getAdditionalAttributesStrings())
                .affiliations(prideSolrDataset.getAffiliations())
                .dataProcessingProtocol(prideSolrDataset.getDataProcessingProtocol())
                .sampleProcessingProtocol(prideSolrDataset.getSampleProcessingProtocol())
                .diseases(prideSolrDataset.getDiseases())
                .organisms(prideSolrDataset.getOrganisms())
                .organismParts(prideSolrDataset.getOrganismPart())
                .instruments(prideSolrDataset.getInstruments())
                .submitters(prideSolrDataset.getSubmitters())
                .keywords(prideSolrDataset.getKeywords())
                .projectTags(prideSolrDataset.getProjectTags())
                .labPIs(prideSolrDataset.getLabPIs())
                .identifiedPTMS(prideSolrDataset.getIdentifiedPTMStrings())
                .publicationDate(prideSolrDataset.getPublicationDate())
                .quantificationMethods(prideSolrDataset.getQuantificationMethods())
                .references(new HashSet<>(prideSolrDataset.getReferences()))
                .softwares(prideSolrDataset.getSoftwares())
                .submissionDate(prideSolrDataset.getSubmissionDate())
                .updatedDate(prideSolrDataset.getUpdatedDate())
                .license(license)
                .queryScore((prideSolrDataset.getScore()!=null)?prideSolrDataset.getScore().doubleValue():null)
                .build();
    }


}
