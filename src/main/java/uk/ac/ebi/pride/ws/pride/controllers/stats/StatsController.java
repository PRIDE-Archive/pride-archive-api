package uk.ac.ebi.pride.ws.pride.controllers.stats;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.client.StatRepoClient;
import uk.ac.ebi.pride.mongodb.archive.model.stats.MongoPeptidomeStats;
import uk.ac.ebi.pride.mongodb.archive.model.stats.MongoPrideStats;
import uk.ac.ebi.pride.mongodb.archive.service.stats.PeptidomeStatsMongoService;
import uk.ac.ebi.pride.mongodb.archive.service.stats.PrideStatsMongoService;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 * Created by ypriverol (ypriverol@gmail.com) on 28/06/2018.
 */
@Controller
public class StatsController {

    final PrideStatsMongoService mongoStatsService;
    final PeptidomeStatsMongoService peptidomeStatsMongoService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    final ProjectRepoClient projectRepoClient;

    final StatRepoClient statRepoClient;

    @Autowired
    public StatsController(PrideStatsMongoService mongoStatsService, PeptidomeStatsMongoService peptidomeStatsMongoService, CustomPagedResourcesAssembler customPagedResourcesAssembler, ProjectRepoClient projectRepoClient, StatRepoClient statRepoClient) {
        this.mongoStatsService = mongoStatsService;
        this.peptidomeStatsMongoService = peptidomeStatsMongoService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
        this.projectRepoClient = projectRepoClient;
        this.statRepoClient = statRepoClient;
    }


    @ApiOperation(notes = "Retrieve statistics by Name", value = "statistics", nickname = "getStatsByName", tags = {"stats"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/{name}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> statistics(@PathVariable(value = "name", name = "name") String name) {

        Object stats = mongoStatsService.findLastGeneratedStats().getSubmissionsCount().get(name);
        if (stats == null || ((List) stats).size() == 0)
            stats = mongoStatsService.findLastGeneratedStats().getComplexStats().get(name);

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @ApiOperation(notes = "Retrieve submissions count per country as TSV", value = "submissions-per-country", nickname = "submissions-per-country", tags = {"stats"})
    @RequestMapping(value = "/submissions-per-country", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> submissionsPerCountry() {
        String name = "SUBMISSIONS_PER_COUNTRY";
        List<Tuple<String, Integer>> stats = mongoStatsService.findLastGeneratedStats().getSubmissionsCount().get(name);
        StringBuilder statsBuilder = new StringBuilder("Country\tNumber_of_submissions");
        for (Tuple<String, Integer> tuple : stats) {
            statsBuilder.append("\n").append(tuple.getKey()).append("\t").append(tuple.getValue());
        }
        return new ResponseEntity<>(statsBuilder.toString(), HttpStatus.OK);
    }


    @ApiOperation(notes = "Retrieve all statistics keys and names", value = "statistics", nickname = "getStatNames", tags = {"stats"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getStatisticsNames() {

        List<String> statNames = new ArrayList<>();
        MongoPrideStats stats = mongoStatsService.findLastGeneratedStats();
        if (stats != null) {
            if (stats.getSubmissionsCount() != null)
                statNames.addAll(new ArrayList<>(stats.getSubmissionsCount().keySet()));
            if (stats.getComplexStats() != null)
                statNames.addAll(new ArrayList<>(stats.getComplexStats().keySet()));
        }

        return new ResponseEntity<>(statNames, HttpStatus.OK);
    }

    @ApiOperation(notes = "Retrieve month wise submissions count", value = "submissions-monthly", nickname = "submissions-monthly", tags = {"stats"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/submissions-monthly", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> submissionsMonthly() throws IOException {

        List<List<String>> results = projectRepoClient.findMonthlySubmissions();

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @ApiOperation(notes = "Retrieve month wise submissions count", value = "submissions-monthly-tsv", nickname = "submissions-monthly-tsv", tags = {"stats"})
    @RequestMapping(value = "/stats/submissions-monthly-tsv", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> submissionsMonthlyTsv() throws IOException {

        List<List<String>> results = projectRepoClient.findMonthlySubmissions();
        StringBuilder statsBuilder = new StringBuilder("Month\tNumber_of_submissions");
        for (List<String> row : results) {
            statsBuilder.append("\n").append(row.get(0)).append("\t").append(row.get(1));
        }
        return new ResponseEntity<>(statsBuilder.toString(), HttpStatus.OK);
    }

    @ApiOperation(notes = "Retrieve peptidome stats", value = "peptidome-stats", nickname = "peptidome-stats", tags = {"stats"})
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/peptidome-stats", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> peptidomeStats() throws IOException {

        List<MongoPeptidomeStats> results = peptidomeStatsMongoService.findall();

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @ApiOperation(notes = "Retrieve month wise submissions data size", value = "submissions-data-size-monthly", nickname = "submissions-data-size-monthly", tags = {"stats"})
    @RequestMapping(value = "/stats/submitted-data", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> getSubmittedDataStats() throws IOException {
        String submittedDataStats = statRepoClient.getSubmittedDataStats();
        return new ResponseEntity<>(submittedDataStats, HttpStatus.OK);
    }
}
