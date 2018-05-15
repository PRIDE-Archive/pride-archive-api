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
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * The facet Assembler Resource Component allows to convert Resource Facet into a Hateoas.
 *
 * @author ypriverol
 */
@Component
public class FacetsResourceAssembler implements ResourceAssembler<SimpleFacetFieldEntry, Resource<Facet>> {

    @Override
    public Resource<Facet> toResource(SimpleFacetFieldEntry facetFieldEntry) {
        return new Resource<>(new Facet(facetFieldEntry.getValue(), facetFieldEntry.getValueCount()), buildRelFacet(facetFieldEntry));
    }

    private static Link buildRelFacet(SimpleFacetFieldEntry facetFieldEntry) {
        UriComponentsBuilder uriComponentsBuilder = componentsBuilderFromCurrentRequest();
        MultiValueMap<String, String> newQueryParams = removePaginationParameters(cloneQueryParameters(uriComponentsBuilder.build().getQueryParams()));

        String facetField = Objects.requireNonNull(facetFieldEntry.getKey()).getName();
        String facetValue = facetFieldEntry.getValue();
        List<String> queryParameter = newQueryParams.get(WsContastants.HateoasEnum.facets.name());
        String facetConstraint = buildFacetConstraint(facetField, facetValue);

        if (queryParameter != null && queryParameter.contains(facetConstraint)) {
            queryParameter.remove(facetConstraint);
            if (queryParameter.isEmpty()) {
                newQueryParams.remove(WsContastants.HateoasEnum.facets.name());
            }
        } else {
            newQueryParams.add(WsContastants.HateoasEnum.facets.name(), facetConstraint);
        }
        uriComponentsBuilder.replaceQueryParams(newQueryParams);
        return new Link(uriComponentsBuilder.build().toUriString(), WsContastants.HateoasEnum.facets.name());
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
        actualQueryParams.remove(WsContastants.HateoasEnum.facets.name());
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