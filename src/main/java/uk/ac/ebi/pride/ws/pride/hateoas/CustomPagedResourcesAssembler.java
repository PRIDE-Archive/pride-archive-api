package uk.ac.ebi.pride.ws.pride.hateoas;

import org.springframework.data.domain.Page;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author ypriverol
 */

public class CustomPagedResourcesAssembler<T> extends PagedResourcesAssembler<T> {

    private final RepresentationModelAssembler<T, ? extends RepresentationModel> facetResourceAssembler;

    public CustomPagedResourcesAssembler(HateoasPageableHandlerMethodArgumentResolver resolver, RepresentationModelAssembler<T, ? extends RepresentationModel> facetResourceAssembler) {
        super(resolver, null);
        this.facetResourceAssembler = facetResourceAssembler;
    }

    public <R extends RepresentationModel, S> PagedModel<R> createPagedModel(List<R> resources, PagedModel.PageMetadata metadata, Page<S> page, Link... links) {
        PagedModel<R> pagedModel = super.createPagedModel(resources, metadata, page);
        pagedModel.add(links);
        return remap(page, pagedModel);
    }

    private <R extends RepresentationModel, S> PagedModel<R> remap(Page<S> page, PagedModel<R> pagedModel) {
        if (!(page instanceof FacetPage)) {
            return pagedModel;
        }

        FacetPage<S> facetPage = (FacetPage<S>) page;
        Map<String, Collection<RepresentationModel>> facets = new TreeMap<>();
        Collection<Field> facetFields = facetPage.getFacetFields();

        for (Field field : facetFields) {
            Page facetResultPage = facetPage.getFacetResultPage(field);
            @SuppressWarnings("unchecked")
            PagedModel<RepresentationModel> resourceSupports = toModel(facetResultPage, facetResourceAssembler);
            facets.put(field.getName(), resourceSupports.getContent());
        }

        return new FacetPagedModel<>(pagedModel, facets);
    }
}
