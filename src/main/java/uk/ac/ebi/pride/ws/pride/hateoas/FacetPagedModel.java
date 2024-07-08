package uk.ac.ebi.pride.ws.pride.hateoas;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Facet and Paged resource in Spring Hateoas. This class enable to implements Paged and Facet in the same resource.
 *
 * @author ypriverol
 */
public class FacetPagedModel<T> extends PagedModel<T> {

    private final Map<String, Collection<RepresentationModel>> facets;

    public FacetPagedModel(PagedModel<T> PagedModel, Map<String, Collection<RepresentationModel>> facets) {
        super(PagedModel.getContent(), PagedModel.getMetadata(), PagedModel.getLinks());
        this.facets = facets;
    }

    @JsonProperty("_facets")
    public Map<String, Collection<RepresentationModel>> getFacets() {
        return facets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FacetPagedModel<?> that = (FacetPagedModel<?>) o;
        return Objects.equals(facets, that.facets);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), facets);
    }
}
