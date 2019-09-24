package uk.ac.ebi.pride.ws.pride.utils;

import uk.ac.ebi.pride.utilities.util.Triple;
import uk.ac.ebi.pride.utilities.util.Tuple;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 23/05/2018.
 */
public class WsUtils {

    public static Tuple<Integer, Integer> validatePageLimit(int start, int size) {
        if(size > WsContastants.MAX_PAGINATION_SIZE || size < 0 )
            size = WsContastants.MAX_PAGINATION_SIZE;
        if(start < 0)
            start = 0;
        return new Tuple<>(start, size);
    }

    public static Tuple<Integer, Integer> validatePageLimit(int start, int size, int maxPageSize) {
        if(size > maxPageSize || size < 0 )
            size = maxPageSize;
        if(start < 0)
            start = 0;
        return new Tuple<>(start, size);
    }

    public static long validatePage(int page, long totalPages) {
        if(page < 0)
            return 0;
        if(page > totalPages)
            return totalPages;
        return page;
    }

    public static String fixToSizeBold(String x, int gap) {
        int index = x.indexOf("<b>");
        int lastIndex = x.indexOf("</b>");
        index = (index - (gap+3))<0?0:index-(gap+3);
        lastIndex = (lastIndex+(gap+3))>x.length()?x.length():lastIndex+gap+3;
        while (index > 0 && x.charAt(index) != ' '){
            index--;
        }
        while (lastIndex < x.length() && x.charAt(lastIndex) != ' '){
            lastIndex++;
        }
        return x.substring(index, lastIndex);
    }

    /**
     * Get an identifier as the combination of multiple keys.
     *
     * @param keys List of keys
     * @return final identifier
     */
    public static String getIdentifier(String ... keys){
        return String.join(":", keys);
    }

    public static Triple<String, String, String> parseProteinEvidenceAccession(String accession) throws Exception {
        String[] values = accession.split(":");
        if(values.length < 3)
            throw new Exception("No valid accession for ProteinEvidences");
        String projectAccession, assayAccession;
        StringBuilder reportedProtein = new StringBuilder(values[2]);
        projectAccession = values[0];
        assayAccession = values[1];
        for(int i = 3; i < values.length; i++)
            reportedProtein.append(":").append(values[i]);
        return new Triple<>(projectAccession, assayAccession, reportedProtein.toString());
    }

    public static String[] parsePeptideEvidenceAccession(String accession) throws Exception {
        String[] values = accession.split(":");
        if(values.length < 4)
            throw new Exception("No valid accession for PeptideEvidences");
        String[] valueKeys = new String[4];
        valueKeys[0] = values[0];
        valueKeys[1] = values[1];
        valueKeys[2] = values[2];
        valueKeys[3] = peptideEvidenceUiToMongoPeptideUi(values[3]);
        return valueKeys;
    }

    public static String peptideEvidenceUiToMongoPeptideUi(String value) {
        return value.replace("|", ";");
    }

    public static String mongoPeptideUiToPeptideEvidence(String value) {
        return value.replace(";", "|");
    }


}
