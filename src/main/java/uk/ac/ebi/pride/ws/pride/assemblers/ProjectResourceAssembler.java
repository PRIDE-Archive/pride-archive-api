package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.ws.pride.controllers.ProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProject;
import uk.ac.ebi.pride.ws.pride.models.dataset.ProjectResource;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author ypriverol
 */
public class ProjectResourceAssembler extends ResourceAssemblerSupport<PrideSolrProject, ProjectResource> {

    public ProjectResourceAssembler(Class<?> controller, Class<ProjectResource> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public ProjectResource toResource(PrideSolrProject prideSolrDataset) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProjectResource> toResources(Iterable<? extends PrideSolrProject> entities) {

        List<ProjectResource> datasets = new ArrayList<>();

        for(PrideSolrProject prideSolrDataset: entities){

            CompactProject dataset = CompactProject.builder()
                    .accession(prideSolrDataset.getAccession())
                    .title(prideSolrDataset.getTitle())
                    .projectDescription(prideSolrDataset.getProjectDescription())
                    .additionalAttributes(prideSolrDataset.getAdditionalAttributes())
                    .affiliations(prideSolrDataset.getAffiliations())
                    .dataProcessingProtocol(prideSolrDataset.getDataProcessingProtocol())
                    .sampleProcessingProtocol(prideSolrDataset.getSampleProcessingProtocol())
                    .diseases(prideSolrDataset.getDiseases())
                    .organisms(prideSolrDataset.getOrganisms())
                    .organismParts(prideSolrDataset.getOrganismPart())
                    .instruments(new ArrayList<>(prideSolrDataset.getInstruments()))
                    .submitters(prideSolrDataset.getSubmitters())
                    .keywords(prideSolrDataset.getKeywords())
                    .projectTags(prideSolrDataset.getProjectTags())
                    .labPIs(prideSolrDataset.getLabPIs())
                    .identifiedPTMStrings(prideSolrDataset.getIdentifiedPTMStrings())
                    .publicationDate(prideSolrDataset.getPublicationDate())
                    .quantificationMethods(prideSolrDataset.getQuantificationMethods())
                    .references(prideSolrDataset.getReferences())
                    .softwares(prideSolrDataset.getSoftwares())
                    .submissionDate(prideSolrDataset.getSubmissionDate())
                    .updatedDate(prideSolrDataset.getUpdatedDate())
                    .build();
            if(entities instanceof FacetAndHighlightPage){
                FacetAndHighlightPage<PrideSolrProject> facetPages = (FacetAndHighlightPage<PrideSolrProject>) entities;
                dataset.setHighlights(facetPages.getHighlights(prideSolrDataset).stream().collect(Collectors.toMap(x -> x.getField().getName(), HighlightEntry.Highlight::getSnipplets)));
            }
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(ProjectController.class).getProject(prideSolrDataset.getAccession())).withSelfRel());
            datasets.add(new ProjectResource(dataset, links));
        }

        return datasets;
    }


}
