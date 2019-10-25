package uk.ac.ebi.pride.ws.pride.utils;

/**
 * @author ypriverol
 */
public class WsContastants {

    public enum HateoasEnum {
        next, previous, last, first, facets, files, peptideevidences, psms;
    }

    public enum ResultType{
        FULL("full"),
        COMPACT("compact");

        String name;

        ResultType(String name){
            this.name = name;
        }

    }

    public enum GAP_DATE {
        DAY("+1DAY"),
        MONTH("+1MONTH"),
        YEAR("+1YEAR");

        String name;

        GAP_DATE(String name) {
            this.name = name;
        }
    }

    public static final String QUERY_PARAM_NAME = "q";
    public static final String FACET_PARAM_NAME = "facet";

    public static final int MAX_PAGINATION_SIZE = 100;
    public static final int MAX_PAGINATION_SIZE_PEPTIDE_EVIDENCES = 1000;
    public static final int MINOR_PAGINATION = 0;

    public static String CONNECTIONS_PER_HOST = "10";

    public static String THREAD_CONNECTION = "10";

    public static String CONNECTION_TIMEOUT = "1000000";

    public static String MAX_WAIT_TIME_OUT = "120000";

    public static String SOCKET_ALIVE = "true";

    public static String SOCKET_TIME_OUT = "1000000";

    public static String WRITE_CONCERN = "NORMAL";

    public static String READ_PREFERENCE = "SECONDARY";

    public static String PX_PROJECT_NOT_FOUND = "The project accession is not in the database -- ";
    public static String PROTEIN_NOT_FOUND = "The protein evidence accession is not in the database -- ";
    public static final String PEPTIDE_USI_NOT_FOUND = "The peptide evidence accession is not in the database --";
    public static String CONTACT_PRIDE = " Contact pride support: pride-support@ebi.ac.uk";
    public static String AAP_TOKEN_MISMATCH_ERROR = "Authorization Token is invalid";
}