package uk.ac.ebi.pride.ws.pride.assemblers;

import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;
import uk.ac.ebi.pride.ws.pride.controllers.DatasetController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactDataset;
import uk.ac.ebi.pride.ws.pride.models.dataset.DatasetREsource;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author ypriverol
 */
public class DatasetResourceAssembler extends ResourceAssemblerSupport<PrideSolrDataset, DatasetREsource> {

    public DatasetResourceAssembler(Class<?> controller, Class<DatasetREsource> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public DatasetREsource toResource(PrideSolrDataset prideSolrDataset) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DatasetREsource> toResources(Iterable<? extends PrideSolrDataset> entities) {

        List<DatasetREsource> datasets = new ArrayList<>();

        for(PrideSolrDataset prideSolrDataset: entities){

            CompactDataset dataset = CompactDataset.builder()
                    .accession(prideSolrDataset.getAccession())
                    .title(prideSolrDataset.getTitle())
                    .projectDescription(prideSolrDataset.getProjectDescription())
                    .additionalAttributes(new ArrayList<>(prideSolrDataset.getAdditionalAttributes()))
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
                FacetAndHighlightPage<PrideSolrDataset> facetPages = (FacetAndHighlightPage<PrideSolrDataset>) entities;
                dataset.setHighlights(facetPages.getHighlights(prideSolrDataset).stream().collect(Collectors.toMap(x -> x.getField().getName(), HighlightEntry.Highlight::getSnipplets)));
            }
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).getDataset(prideSolrDataset.getAccession())).withSelfRel());
            datasets.add(new DatasetREsource(dataset, links));
        }

        return datasets;
    }


}
