package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideProjectFieldEnum;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.solr.indexes.pride.utils.PrideSolrConstants;
import uk.ac.ebi.pride.ws.pride.hateoas.Facet;
import uk.ac.ebi.pride.ws.pride.hateoas.Facets;
import uk.ac.ebi.pride.ws.pride.models.dataset.FacetResource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ypriverol
 */
@Slf4j
public class FacetResourceAssembler extends ResourceAssemblerSupport<PrideSolrProject, FacetResource> {


    private final String facetGap;

    public FacetResourceAssembler(Class<?> controller, Class<FacetResource> resourceType, String dateFacetGap) {
        super(controller, resourceType);
        this.facetGap = dateFacetGap;
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

            PrideSolrConstants.AllowedDateGapConstants facetEnum = PrideSolrConstants.AllowedDateGapConstants.findByString(facetGap);

            Arrays.stream(PrideProjectFieldEnum
                    .values())
                    .filter(PrideProjectFieldEnum::getFacet)
                    .filter(x -> x.getType() == PrideSolrConstants.ConstantsSolrTypes.DATE)
                    .forEach( fieldGroup -> {
                        if( facetPages.getRangeFacetResultPage(fieldGroup.getValue()) != null && facetPages.getRangeFacetResultPage(fieldGroup.getValue()).getSize() > 0){
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            if(facetEnum == PrideSolrConstants.AllowedDateGapConstants.YEARLY)
                                dateFormat = new SimpleDateFormat("yyyy");
                            else if(facetEnum == PrideSolrConstants.AllowedDateGapConstants.MONTHLY)
                                dateFormat = new SimpleDateFormat("yyyy-MM");
                            SimpleDateFormat finalDateFormat = dateFormat;
                            values.put(fieldGroup.getValue(), facetPages.getRangeFacetResultPage(fieldGroup.getValue()).getContent()
                                    .stream()
                                    .map( entry -> {
                                        Date date = null;
                                        try {
                                            date = finalDateFormat.parse(entry.getValue());
                                            return new SimpleFacetFieldEntry(entry.getField(), finalDateFormat.format(date), entry.getValueCount());
                                        } catch (ParseException e) {
                                            log.error(e.getMessage(),e);
                                        }
                                        return null;
                                    }).filter(Objects::nonNull)
                                    .collect(Collectors.toList()));
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