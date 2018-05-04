package uk.ac.ebi.pride.ws.pride.hateoas;

/**
 * @author ypriverol
 */
public class Facet {

    private final String value;
    private final long count;

    public Facet(String value, long count) {
        this.value = value;
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public long getCount() {
        return count;
    }
}
