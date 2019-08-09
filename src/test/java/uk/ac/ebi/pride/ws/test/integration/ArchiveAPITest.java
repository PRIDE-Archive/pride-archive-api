package uk.ac.ebi.pride.ws.test.integration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.configs.ArchiveMongoConfig;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideProjectField;
import uk.ac.ebi.pride.ws.pride.Application;
import uk.ac.ebi.pride.ws.pride.configs.SolrCloudConfig;
import uk.ac.ebi.pride.ws.pride.configs.SwaggerConfig;
import uk.ac.ebi.pride.ws.pride.service.user.AAPService;
import uk.ac.ebi.pride.ws.test.integration.util.DocumentationUtils;
import uk.ac.ebi.pride.ws.test.integration.util.TestConstants;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, ArchiveMongoConfig.class, SolrCloudConfig.class, SwaggerConfig.class/*,TestService.class*/})
@PropertySource(value = {"classpath:application.properties", "classpath:application.yml"}, ignoreResourceNotFound = true)
@AutoConfigureRestDocs
public class ArchiveAPITest {

    private MockMvc mockMvc;

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private WebApplicationContext context;

    @Value("${app.vhost}")
    private String appVhost;

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    @Autowired
    private AAPService aapService;

    /*@Autowired
    private TestService testService;*/

    private Map<String, String> testValuesMap;

    @Before
    public void setUp() {

        String host = appVhost;
        host += contextPath;

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity()) // this is the key
                .apply(documentationConfiguration(this.restDocumentation).uris()
                        .withScheme("https")
                        .withHost(host)
                        .withPort(443)
                )
                .build();

        /*Populate required values for testing endpoints*/
        testValuesMap = new HashMap<>();

        String fileAccession = "PXF00000963831";//testService.getFileAccession();
        testValuesMap.put(TestConstants.FILE_ACCESSION, fileAccession);

        testValuesMap.put(TestConstants.PROJECT_ACCESSION, "PRD000001");

        //List<String> msRunValuesList = testService.getMsRunFileAccession();
        testValuesMap.put(TestConstants.FILE_ACCESSION_WITH_MSRUN, "PXF00000042521"/*msRunValuesList.get(0)*/);
        testValuesMap.put(TestConstants.PROJECT_ACCESSION_WITH_MSRUN, "PXD011455"/*msRunValuesList.get(1)*/);
    }

    /*Files Tests*/
    @Test
    public void getFileTest() throws Exception {


        this.mockMvc.perform(get("/files/{fileAccession}", testValuesMap.get(TestConstants.FILE_ACCESSION)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-file", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), pathParameters(
                                parameterWithName("fileAccession").description("The file accession id"))));
    }

    @Test
    public void getAllFilesTest() throws Exception {
        this.mockMvc.perform(get("/files?filter=accession==" + testValuesMap.get(TestConstants.FILE_ACCESSION) + "&pageSize=5&page=0&sortDirection=DESC&sortConditions=submissionDate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-all-files", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), requestParameters(
                                parameterWithName("filter").description("Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==" + testValuesMap.get(TestConstants.FILE_ACCESSION) + ". This filter allows advance querying and more information can be found at link:#_advance_filter[Advance Filter]"),
                                parameterWithName("pageSize").description("Number of results to fetch in a page"),
                                parameterWithName("page").description("Identifies which page of results to fetch"),
                                parameterWithName("sortDirection").description("Sorting direction: ASC or DESC"),
                                parameterWithName("sortConditions").description("Field(s) for sorting the results on. Default for this request is " + PrideArchiveField.SUBMISSION_DATE + ". More fields can be separated by comma and passed. Example: " + PrideArchiveField.SUBMISSION_DATE + "," + PrideArchiveField.FILE_NAME))));
    }

    /*Projects API Tests*/

    @Test
    public void getAllProjectsTest() throws Exception {
        this.mockMvc.perform(get("/projects?pageSize=5&page=0&sortDirection=DESC&sortConditions=submissionDate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-all-projects", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), requestParameters(
                                parameterWithName("pageSize").description("Number of results to fetch in a page"),
                                parameterWithName("page").description("Identifies which page of results to fetch"),
                                parameterWithName("sortDirection").description("Sorting direction: ASC or DESC"),
                                parameterWithName("sortConditions").description("Field(s) for sorting the results on. Default for this request is " + PrideArchiveField.SUBMISSION_DATE + ". More fields can be separated by comma and passed. Example: " + PrideArchiveField.SUBMISSION_DATE + "," + PrideArchiveField.FILE_NAME))));
    }

    @Test
    public void getProjectTest() throws Exception {

        this.mockMvc.perform(get("/projects/{accession}", testValuesMap.get(TestConstants.PROJECT_ACCESSION)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-project", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), pathParameters(
                                parameterWithName("accession").description("The Accession id associated with this project"))));
    }

    @Test
    public void getProjectFilesTest() throws Exception {

        this.mockMvc.perform(get("/projects/{accession}/files?filter=fileName==PRIDE_Exp_Complete_Ac_1.pride.mgf.gz&pageSize=5&page=0&sortDirection=DESC&sortConditions=submissionDate", testValuesMap.get(TestConstants.PROJECT_ACCESSION)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-project-files", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), pathParameters(parameterWithName("accession").description("The Accession id associated with this project")), requestParameters(
                                parameterWithName("filter").description("Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example `fileName==PRIDE_Exp_Complete_Ac_1.pride.mgf.gz`. This filter allows advance querying and more information can be found at link:#_advance_filter[Advance Filter]"),
                                parameterWithName("pageSize").description("Number of results to fetch in a page"),
                                parameterWithName("page").description("Identifies which page of results to fetch"),
                                parameterWithName("sortDirection").description("Sorting direction: ASC or DESC"),
                                parameterWithName("sortConditions").description("Field(s) for sorting the results on. Default for this request is " + PrideArchiveField.SUBMISSION_DATE + ". More fields can be separated by comma and passed. Example: " + PrideArchiveField.SUBMISSION_DATE + "," + PrideArchiveField.FILE_NAME))));
    }

    @Test
    public void getSimilarProjectsTest() throws Exception {

        this.mockMvc.perform(get("/projects/{accession}/similarProjects?pageSize=5&page=0", testValuesMap.get(TestConstants.PROJECT_ACCESSION)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-similar-project", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), pathParameters(parameterWithName("accession").description("The Accession id associated with this project")), requestParameters(
                                parameterWithName("pageSize").description("Number of results to fetch in a page"),
                                parameterWithName("page").description("Identifies which page of results to fetch"))));
    }

    @Test
    public void getProjectSearchResultsTest() throws Exception {

        this.mockMvc.perform(get("/search/projects?keyword=*:*&filter=submission_date==2013-10-20&pageSize=5&page=0&dateGap=+1YEAR&sortDirection=DESC&sortConditions=submissionDate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-project-search", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), requestParameters(
                                parameterWithName("keyword").description("The entered word will be searched among the fields to fetch matching projects."),
                                parameterWithName("filter").description("Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==" + testValuesMap.get(TestConstants.PROJECT_ACCESSION) + ". More information on this can be found at link:#_filter[Filter]"),
                                parameterWithName("pageSize").description("Number of results to fetch in a page"),
                                parameterWithName("page").description("Identifies which page of results to fetch"),
                                parameterWithName("dateGap").description("A date range field with possible values of +1MONTH, +1YEAR"),
                                parameterWithName("sortDirection").description("Sorting direction: ASC or DESC"),
                                parameterWithName("sortConditions").description("Field(s) for sorting the results on. Default for this request is " + PrideProjectField.PROJECT_SUBMISSION_DATE + ". More fields can be separated by comma and passed. Example: " + PrideProjectField.PROJECT_SUBMISSION_DATE + "," + PrideProjectField.PROJECT_TILE))));
    }

    @Test
    public void getProjectFacetsTest() throws Exception {

        this.mockMvc.perform(get("/facet/projects?keyword=*:*&filter=submission_date==2013-10-20&facetPageSize=5&facetPage=0&dateGap=+1YEAR").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-project-facet", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), requestParameters(
                                parameterWithName("keyword").description("The entered word will be searched among the fields to fetch matching projects."),
                                parameterWithName("filter").description("Parameters to filter the search results. The structure of the filter is: field1==value1, field2==value2. Example accession==" + testValuesMap.get(TestConstants.PROJECT_ACCESSION) + ". More information on this can be found at link:#_filter[Filter]"),
                                parameterWithName("facetPageSize").description("Number of results to fetch in a page"),
                                parameterWithName("facetPage").description("Identifies which page of results to fetch"),
                                parameterWithName("dateGap").description("A date range field with possible values of +1MONTH, +1YEAR"))));
    }

    /*Stats API Tests*/

    @Test
    public void getAllStatsTest() throws Exception {
        this.mockMvc.perform(get("/stats/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-stats-names", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())));
    }

    /*@Test
    public void getStatTest() throws Exception {

        this.mockMvc.perform(get("/stats/{name}","SUBMISSIONS_PER_MONTH").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-stat", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), pathParameters(
                                parameterWithName("name").description("The name of the statistic to be returned."))));
    }*/

    /*MSRun Tests*/

    @Test
    public void getMsRunsByProject() throws Exception {
        this.mockMvc.perform(get("/msruns/byProject?accession=" + testValuesMap.get(TestConstants.PROJECT_ACCESSION_WITH_MSRUN)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-msrun-by-project", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), requestParameters(
                                parameterWithName("accession").description("The unique project identifier."))));
    }

    @Test
    public void getMsRunsByFile() throws Exception {
        this.mockMvc.perform(get("/msruns/{accession}", testValuesMap.get(TestConstants.FILE_ACCESSION_WITH_MSRUN)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andDo(document("get-msrun-by-file", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), pathParameters(
                                parameterWithName("accession").description("The unique file identifier."))));
    }

    @Test
    public void putMsRunsData() throws Exception {
        String aapToken = aapService.getAAPToken();
        String payload = "{\"MSRunMetadata\":{  \"additionalAttributesStrings\": [],  \"FileProperties\": [],  \"IdSettings\": [    {      \"id\": \"Protocol_1\",      \"FixedModifications\": [        {          \"massDelta\":57.021464,          \"residues\":[\"C\"],          \"composition\":\"H(3)C(2)NO\",          \"position\":\"Anywhere\",          \"name\":{            \"accession\":\"UNIMOD:4\",            \"name\":\"Carbamidomethyl\",            \"cvLabel\":\"UNIMOD\"          }        }      ],      \"VariableModifications\": [        {          \"massDelta\":0.984016,          \"residues\":[\"N\", \"Q\"],          \"position\":\"Anywhere\",          \"composition\":\"H(-1)N(-1)O\",          \"name\":{            \"accession\":\"UNIMOD:7\",            \"name\":\"Deamidated\",            \"cvLabel\":\"UNIMOD\"          }        },        {          \"massDelta\":15.994915,          \"residues\":[\"M\"],          \"position\":\"Anywhere\",          \"composition\":\"O\",          \"name\":{            \"accession\":\"UNIMOD:35\",            \"name\":\"Oxidation\",            \"cvLabel\":\"UNIMOD\"          }        }      ],      \"Enzymes\":[        {          \"id\":\"ENZ_0\",          \"cTermGain\":\"OH\",          \"nTermGain\":\"H\",          \"missedCleavages\":2,          \"semiSpecific\":\"0\",          \"SiteRegexp\":\"![CDATA[(?=[KR])(?!P)]]\",          \"name\":          {            \"accession\":\"MS:1001251\",            \"name\":\"Trypsin\",            \"cvLabel\":\"MS\"          }        }      ],      \"FragmentTolerance\":[        {          \"tolerance\":{            \"accession\":\"MS:1001413\",            \"name\":\"search tolerance minus value\",            \"value\":\"0.6\",            \"cvLabel\":\"MS\"          },          \"unit\":{            \"accession\":\"UO:0000221\",            \"name\":\"dalton\",            \"cvLabel\": \"UO\"          }        }      ],      \"ParentTolerance\":[        {          \"tolerance\":{            \"accession\":\"MS:1001412\",            \"name\":\"search tolerance plus value\",            \"value\":\"20\",            \"cvLabel\":\"MS\"          },          \"unit\":{            \"accession\":\"UO:0000169\",            \"name\":\"parts per million\",            \"cvLabel\": \"UO\"          }        },        {          \"tolerance\":{            \"accession\":\"MS:1001413\",            \"name\":\"search tolerance minus value\",            \"value\":\"20\",            \"cvLabel\":\"MS\"          },          \"unit\":{            \"accession\":\"UO:0000169\",            \"name\":\"parts per million\",            \"cvLabel\": \"UO\"          }        }      ]    }  ],  \"InstrumentProperties\": [    {      \"accession\": \"MS:1000494\",      \"name\": \"Thermo Scientific instrument model\",      \"value\": \"LTQ Orbitrap Velos\",      \"cvLabel\": \"MS\"    }  ],  \"MsData\": [],  \"ScanSettings\": []}}";
        this.mockMvc.perform(put("/msruns/{accession}/updateMetadata", testValuesMap.get(TestConstants.FILE_ACCESSION_WITH_MSRUN)/*"PXF00001876616"*/)
                .header("Authorization", "Bearer " + aapToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andDo(document("put-msrun-into-file", preprocessRequest(DocumentationUtils.maskTokenPreProcessor(), prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestHeaders(
                                headerWithName("Authorization").description(
                                        "Access token beased authorization mechanism. Refer to authorization section for more information on obtaining the token")),
                        pathParameters(parameterWithName("accession").description("The unique file identifier."))
                ));
    }


    @Test
    public void putMsRunsDataPart() throws Exception {
        String aapToken = aapService.getAAPToken();
        String payload = "{\"MSRunMetadata\":{     \"FileProperties\": [       {         \"accession\": \"NCIT:C47922\",         \"cvLabel\": \"NCIT\",         \"name\": \"Pathname\",         \"value\": \"/Users/yperez/Downloads/SMXL7_GR24_1.raw\"       },       {         \"accession\": \"NCIT:C25714\",         \"cvLabel\": \"NCIT\",         \"name\": \"Version\",         \"value\": \"64\"       },       {         \"accession\": \"NCIT:C69199\",         \"cvLabel\": \"NCIT\",         \"name\": \"Content Creation Date\",         \"value\": \"15/07/2014 23:58:41\"       },       {         \"accession\": \"NCIT:C25365\",         \"cvLabel\": \"NCIT\",         \"name\": \"Description\",         \"value\": \"\"       }     ]   }   }";
        this.mockMvc.perform(put("/msruns/{accession}/updateMetadataParts?fieldName={fieldName}", "PXF00000042521", "FileProperties")
                .header("Authorization", "Bearer " + aapToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andDo(document("put-msrun-parts-into-file", preprocessRequest(DocumentationUtils.maskTokenPreProcessor(), prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("accession").description("The unique file identifier.")),
                        requestHeaders(
                                headerWithName("Authorization").description(
                                        "Access token beased authorization mechanism. Refer to authorization section for more information on obtaining the token")),
                        requestParameters(parameterWithName("fieldName").description("The field inside MSRunMetadata to be updated. This can be any one of these: " + PrideArchiveField.MS_RUN_ID_SETTINGS + "," + PrideArchiveField.MS_RUN_SCAN_SETTINGS + "," + PrideArchiveField.MS_RUN_FILE_PROPERTIES + "," + PrideArchiveField.MS_RUN_INSTRUMENT_PROPERTIES + "," + PrideArchiveField.MS_RUN_MS_DATA + "."))));
    }


}
