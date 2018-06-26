package uk.ac.ebi.pride.ws.pride.assemblers;

import org.apache.solr.client.solrj.response.FacetField;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideProjectFieldEnum;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.pride.utils.PrideSolrConstants;
import uk.ac.ebi.pride.ws.pride.hateoas.Facet;
import uk.ac.ebi.pride.ws.pride.hateoas.Facets;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ypriverol
 */
public class FacetResourceAssembler extends ResourceAssemblerSupport<PrideSolrProject, FacetResource> {



    public FacetResourceAssembler(Class<?> controller, Class<FacetResource> resourceType, long page) {
        super(controller, resourceType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<FacetResource> toResources(Iterable<? extends PrideSolrProject> entities) {

        List<FacetResource> facets = new ArrayList<>();
        FacetPage<PrideSolrProject> facetPages;

        if(entities instanceof FacetAndHighlightPage){
            facetPages = (FacetPage<PrideSolrProject>) entities;
            Map<String, List<? extends FacetEntry>> values = new HashMap<>();
            for(Page<? extends FacetEntry> facet: facetPages.getFacetResultPages()){
               values.putAll(facet.getContent()
                       .stream()
                       .collect(Collectors.groupingBy(entry -> entry.getKey().toString())));
            }

            Arrays.asList(PrideProjectFieldEnum
                    .values())
                    .stream()
                    .filter(PrideProjectFieldEnum::getFacet)
                    .filter(x -> x.getType() == PrideSolrConstants.ConstantsSolrTypes.DATE)
                    .forEach( fieldGroup -> {
                        if( facetPages.getRangeFacetResultPage(fieldGroup.getValue()) != null && facetPages.getRangeFacetResultPage(fieldGroup.getValue()).getSize() > 0){
                            values.put(fieldGroup.getValue(), facetPages.getRangeFacetResultPage(fieldGroup.getValue()).getContent());
                        }
                    });
            facets = values.entrySet()
                    .stream()
                    .map( x-> new FacetResource(new Facets(x.getKey(), x.getValue()
                            .stream()
                            .map(xValue -> new Facet(xValue.getValue(), xValue.getValueCount()))
                            .collect(Collectors.toList())), new ArrayList<>()))
                    .collect(Collectors.toList());
        }
        return facets;

    }


    @Override
    public FacetResource toResource(PrideSolrProject prideSolrDataset) {
        return null;
    }}