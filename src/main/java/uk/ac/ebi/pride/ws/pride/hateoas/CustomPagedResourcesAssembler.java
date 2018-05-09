package uk.ac.ebi.pride.ws.pride.hateoas;

import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.ResourceSupport;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author ypriverol
 */

public class CustomPagedResourcesAssembler<T> extends PagedResourcesAssembler<T> {

    private final ResourceAssembler<T, ResourceSupport> facetResourceAssembler;

    public CustomPagedResourcesAssembler(HateoasPageableHandlerMethodArgumentResolver resolver, ResourceAssembler<T, ResourceSupport> facetResourceAssembler) {
        super(resolver, null);
        this.facetResourceAssembler = facetResourceAssembler;
    }

    public <R extends ResourceSupport, S> PagedResources<R> createPagedResource(List<R> resources, PagedResources.PageMetadata metadata, Page<S> page, Link ... links) {
        PagedResources<R> pagedResource = super.createPagedResource(resources, metadata, page);
        pagedResource.add(links);
        return remap(page, pagedResource);
    }

    private <R extends ResourceSupport, S> PagedResources<R> remap(Page<S> page, PagedResources<R> pagedResource) {
        if (!(page instanceof FacetPage)) {
            return pagedResource;
        }

        FacetPage<S> facetPage = (FacetPage<S>) page;
        Map<String, Collection<ResourceSupport>> facets = new TreeMap<>();
        Collection<Field> facetFields = facetPage.getFacetFields();

        for (Field field : facetFields) {
            Page facetResultPage = facetPage.getFacetResultPage(field);
            @SuppressWarnings("unchecked")
            PagedResources<ResourceSupport> resourceSupports = toResource(facetResultPage, facetResourceAssembler);
            facets.put(field.getName(), resourceSupports.getContent());
        }

        return new FacetPagedResource<>(pagedResource, facets);
    }
}
