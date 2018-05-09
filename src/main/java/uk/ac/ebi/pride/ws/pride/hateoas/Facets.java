package uk.ac.ebi.pride.ws.pride.hateoas;

import java.util.List;

/**
 * @author ypriverol
 */
public class Facets {

    String field;
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
