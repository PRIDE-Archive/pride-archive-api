package uk.ac.ebi.pride.ws.pride;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import uk.ac.ebi.pride.mongodb.archive.model.files.idsettings.IdSetting;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrProject;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.hateoas.FacetsResourceAssembler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Retrieve the projects {@link uk.ac.ebi.pride.archive.dataprovider.project.ProjectProvider} from PRIDE Archive and the corresponding information.
 *
 * @author ypriverol
 *
 */

@EnableSwagger2

@SpringBootApplication
@ComponentScan({"uk.ac.ebi.pride.ws.pride","uk.ac.ebi.tsc.aap.client.security"})
public class Application {

    public static void main(String[] args) {

        /*String jsonData = "{      \"id\": \"Protocol_1\",      \"FixedModifications\": [        {          \"massDelta\":57.021464,          \"residues\":[\"C\"],          \"composition\":\"H(3)C(2)NO\",          \"position\":\"Anywhere\",          \"name\":{            \"accession\":\"UNIMOD:4\",            \"name\":\"Carbamidomethyl\",            \"cvLabel\":\"UNIMOD\"          }        }      ],      \"VariableModifications\": [        {          \"massDelta\":0.984016,          \"residues\":[\"N\", \"Q\"],          \"position\":\"Anywhere\",          \"composition\":\"H(-1)N(-1)O\",          \"name\":{            \"accession\":\"UNIMOD:7\",            \"name\":\"Deamidated\",            \"cvLabel\":\"UNIMOD\"          }        },        {          \"massDelta\":15.994915,          \"residues\":[\"M\"],          \"position\":\"Anywhere\",          \"composition\":\"O\",          \"name\":{            \"accession\":\"UNIMOD:35\",            \"name\":\"Oxidation\",            \"cvLabel\":\"UNIMOD\"          }        }      ],      \"Enzymes\":[        {          \"id\":\"ENZ_0\",          \"cTermGain\":\"OH\",          \"nTermGain\":\"H\",          \"missedCleavages\":2,          \"semiSpecific\":\"0\",          \"SiteRegexp\":\"![CDATA[(?=[KR])(?!P)]]\",          \"name\":          {            \"accession\":\"MS:1001251\",            \"name\":\"Trypsin\",            \"cvLabel\":\"MS\"          }        }      ],      \"FragmentTolerance\":[        {          \"tolerance\":{            \"accession\":\"MS:1001413\",            \"name\":\"search tolerance minus value\",            \"value\":\"0.6\",            \"cvLabel\":\"MS\"          },          \"unit\":{            \"accession\":\"UO:0000221\",            \"name\":\"dalton\",            \"cvLabel\": \"UO\"          }        }      ],      \"ParentTolerance\":[        {          \"tolerance\":{            \"accession\":\"MS:1001412\",            \"name\":\"search tolerance plus value\",            \"value\":\"20\",            \"cvLabel\":\"MS\"          },          \"unit\":{            \"accession\":\"UO:0000169\",            \"name\":\"parts per million\",            \"cvLabel\": \"UO\"          }        },        {          \"tolerance\":{            \"accession\":\"MS:1001413\",            \"name\":\"search tolerance minus value\",            \"value\":\"20\",            \"cvLabel\":\"MS\"          },          \"unit\":{            \"accession\":\"UO:0000169\",            \"name\":\"parts per million\",            \"cvLabel\": \"UO\"          }        }      ]    } ";
        IdSetting idSettings = new IdSetting();
        Gson gson = new Gson();
        idSettings = gson.fromJson(jsonData, idSettings.getClass());

        System.out.println(idSettings.getId());
        System.out.println(idSettings.getEnzymes());
        System.out.println(idSettings.getFixedModifications());
        System.out.println(idSettings.getParentTolerance());*/

        SpringApplication.run(Application.class, args);
    }

    @Component
    @Primary
    private class CustomObjectMapper extends ObjectMapper {
        public CustomObjectMapper() {
            setSerializationInclusion(JsonInclude.Include.NON_NULL);
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
            enable(SerializationFeature.INDENT_OUTPUT);

        }
    }

    @Bean
    public RelProvider relProvider() {
        return new EvoInflectorRelProvider();
    }

    @Bean
    public ResourceAssembler facetResourceAssembler(){
        return new FacetsResourceAssembler();
    }

    @Bean
    public HateoasPageableHandlerMethodArgumentResolver pageableResolver() {
        return new HateoasPageableHandlerMethodArgumentResolver(sortResolver());
    }

    @Bean
    public HateoasSortHandlerMethodArgumentResolver sortResolver() {
        return new HateoasSortHandlerMethodArgumentResolver();
    }

    @SuppressWarnings("unchecked")
    @Bean
    public CustomPagedResourcesAssembler<PrideSolrProject> customPagedResourcesAssembler(){
        return new CustomPagedResourcesAssembler<PrideSolrProject>(pageableResolver(), facetResourceAssembler());
    }
}
