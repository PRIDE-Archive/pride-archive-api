package uk.ac.ebi.pride.ws.pride.hateoas;

import org.springframework.hateoas.core.Relation;

import java.util.List;

/**
 * @author ypriverol
 */
@Relation(collectionRelation = "facets")
public class Facets {

    private String field;
    List<Facet> values;

    public Facets(String field, List<Facet> values) {
        this.field = field;
        this.values = values;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<Facet> getValues() {
        return values;
    }

    public void setValues(List<Facet> values) {
        this.values = values;
    }
}
