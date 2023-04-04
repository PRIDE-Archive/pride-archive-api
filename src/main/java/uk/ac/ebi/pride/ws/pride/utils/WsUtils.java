package uk.ac.ebi.pride.ws.pride.utils;

import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.solr.commons.Utils.StringUtils;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.utilities.util.Triple;
import uk.ac.ebi.pride.utilities.util.Tuple;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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

    public static Collection<CvParamProvider> getCvTermsValues(List<uk.ac.ebi.pride.archive.dataprovider.common.Tuple<CvParam, Set<CvParam>>> samplesDescription, CvTermReference efoTerm) {
        Set<CvParamProvider> resultTerms = new HashSet<>();
        samplesDescription.stream()
                .filter(x -> x.getKey().getAccession().equalsIgnoreCase(efoTerm.getAccession()))
                .forEach( y-> y.getValue().forEach(z-> resultTerms.add( new CvParam(z.getCvLabel(), z.getAccession(), StringUtils.convertSentenceStyle(z.getName()), z.getValue()))));
        return resultTerms;
    }

//    public static Map<String, String[]> peptideSummaryEnhancePtmsMap(PrideMongoPeptideSummary mongoPeptideSummary) {
//        Map<String, String[]> ptmsMap = mongoPeptideSummary.getPtmsMap();
//        ModReader modReader = ModReader.getInstance();
//        Map<String, String[]> ptmsMapModified = ptmsMap.entrySet().stream()
//                .filter(e -> !e.getKey().contains(":,")) //to filter out cases where key has invalid PTM i.e., "UNIMOD:, 4"
//                .collect(Collectors.toMap(e -> {
//                    String[] split = e.getKey().replaceAll("[\\[\\]]","").split(",");
//                    String mod = split[0].trim();
//                    String position = split[1].trim();
//                    String name;
//                    try {
//                        name = modReader.getPTMbyAccession(mod).getName();
//                    } catch (Exception ex) { //to handle cases where PTM is not found
//                        name = "unknown_modification";
//                    }
//                    return mod + "," + name + "," + position;
//                }, Map.Entry::getValue));
//
//        return ptmsMapModified;
//    }

    public static String getLicenseFromDate(Date submissionDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date licenseDate = formatter.parse("01-07-2018");
        return submissionDate.after(licenseDate)? "Creative Commons Public Domain (CC0)":"EBI terms of use";
    }
}
