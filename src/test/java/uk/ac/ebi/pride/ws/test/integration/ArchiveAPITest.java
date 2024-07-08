package uk.ac.ebi.pride.ws.test.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ebi.pride.mongodb.archive.model.PrideArchiveField;
import uk.ac.ebi.pride.mongodb.configs.ArchiveMongoConfig;
import uk.ac.ebi.pride.solr.commons.PrideProjectField;
import uk.ac.ebi.pride.ws.pride.Application;
import uk.ac.ebi.pride.ws.pride.configs.SolrCloudConfig;
import uk.ac.ebi.pride.ws.pride.configs.SwaggerConfig;
import uk.ac.ebi.pride.ws.test.integration.util.TestConstants;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnableAutoConfiguration
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(classes = {Application.class, ArchiveMongoConfig.class, SolrCloudConfig.class, SwaggerConfig.class/*,TestService.class*/})
@PropertySource(value = {"classpath:application.properties", "classpath:application.yml"}, ignoreResourceNotFound = true)
@AutoConfigureRestDocs
public class ArchiveAPITest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Value("${app.vhost}")
    private String appVhost;

    @Value("${server.servlet.contextPath}")
    private String contextPath;

    /*@Autowired
    private TestService testService;*/

    private Map<String, String> testValuesMap;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {

        String host = appVhost;
        host += contextPath;

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(springSecurity()) // this is the key
                .apply(documentationConfiguration(restDocumentation).uris()
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
                        preprocessResponse(prettyPrint()), formParameters(
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
                        preprocessResponse(prettyPrint()), formParameters(
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
                        preprocessResponse(prettyPrint()), pathParameters(parameterWithName("accession").description("The Accession id associated with this project")), formParameters(
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
                        preprocessResponse(prettyPrint()), pathParameters(parameterWithName("accession").description("The Accession id associated with this project")), formParameters(
                                parameterWithName("pageSize").description("Number of results to fetch in a page"),
                                parameterWithName("page").description("Identifies which page of results to fetch"))));
    }

    @Test
    public void getProjectSearchResultsTest() throws Exception {

        this.mockMvc.perform(get("/search/projects?keyword=*:*&filter=submission_date==2013-10-20&pageSize=5&page=0&dateGap=+1YEAR&sortDirection=DESC&sortConditions=submissionDate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-project-search", preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()), formParameters(
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
                        preprocessResponse(prettyPrint()), formParameters(
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
                        preprocessResponse(prettyPrint()), formParameters(
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
}
