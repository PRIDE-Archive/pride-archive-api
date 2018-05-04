package uk.ac.ebi.pride.ws.pride.hateoas;

import org.springframework.data.solr.core.query.result.SimpleFacetFieldEntry;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.pride.ws.pride.utils.WsContastants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author ypriverol
 */
@Component
public class FacetResourceAssembler implements ResourceAssembler<SimpleFacetFieldEntry, Resource<Facet>> {

    @Override
    public Resource<Facet> toResource(SimpleFacetFieldEntry facetFieldEntry) {
        return new Resource<>(new Facet(facetFieldEntry.getValue(), facetFieldEntry.getValueCount()), buildRelFacet(facetFieldEntry));
    }

    private static Link buildRelFacet(SimpleFacetFieldEntry facetFieldEntry) {
        UriComponentsBuilder uriComponentsBuilder = componentsBuilderFromCurrentRequest();
        MultiValueMap<String, String> newQueryParams = removePaginationParameters(cloneQueryParameters(uriComponentsBuilder.build().getQueryParams()));

        String facetField = facetFieldEntry.getKey().getName();
        String facetValue = facetFieldEntry.getValue();
        List<String> queryParameter = newQueryParams.get(WsContastants.FACET_PARAM_NAME);
        String facetConstraint = buildFacetConstraint(facetField, facetValue);

        if (queryParameter != null && queryParameter.contains(facetConstraint)) {
            queryParameter.remove(facetConstraint);
            if (queryParameter.isEmpty()) {
                newQueryParams.remove(WsContastants.FACET_PARAM_NAME);
            }
        } else {
            newQueryParams.add(WsContastants.FACET_PARAM_NAME, facetConstraint);
        }
        uriComponentsBuilder.replaceQueryParams(newQueryParams);
        return new Link(uriComponentsBuilder.build().toUriString(), WsContastants.FACET_PARAM_NAME);
    }

    private static UriComponentsBuilder componentsBuilderFromCurrentRequest() {
        return ServletUriComponentsBuilder.fromCurrentRequest();
    }

    private static MultiValueMap<String, String> cloneQueryParameters(MultiValueMap<String, String> actualQueryParams) {
        MultiValueMap<String, String> newQueryParams = new LinkedMultiValueMap<>();
        for (Map.Entry<String, List<String>> queryParameters : actualQueryParams.entrySet()) {
            newQueryParams.put(queryParameters.getKey(), new ArrayList<>(urlDecode(queryParameters.getValue())));
        }

        return newQueryParams;
    }

    private static MultiValueMap<String, String> removePaginationParameters(MultiValueMap<String, String> actualQueryParams) {
        actualQueryParams.remove(WsContastants.PAGE_PARAM_NAME);
        return actualQueryParams;
    }

    private static String buildFacetConstraint(String facetKey, String facetValue) {
        return facetKey + ":" + facetValue;
    }

    private static List<String> urlDecode(List<String> encodedStrings) {
        List<String> decodedStrings = new ArrayList<>(encodedStrings.size());
        for (String encodedString : encodedStrings) {
            try {
                decodedStrings.add(URLDecoder.decode(encodedString, UTF_8.name()));
            } catch (UnsupportedEncodingException exception) {
                decodedStrings.add(encodedString);
            }
        }
        return decodedStrings;
    }
}