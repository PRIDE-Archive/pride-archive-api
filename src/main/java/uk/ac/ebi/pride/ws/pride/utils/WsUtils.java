package uk.ac.ebi.pride.ws.pride.utils;

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

    public static Tuple<Integer, Integer> validatePageLimit(int start, int size){
        if(size > WsContastants.MAX_PAGINATION_SIZE || size < 0 )
            size = WsContastants.MAX_PAGINATION_SIZE;
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
}
