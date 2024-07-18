package uk.ac.ebi.pride.ws.pride.controllers.stats;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import reactor.core.publisher.Mono;
import uk.ac.ebi.pride.archive.mongo.client.StatsMongoClient;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;
import uk.ac.ebi.pride.archive.repo.client.StatRepoClient;

import java.io.IOException;
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

    final StatsMongoClient statsMongoClient;

    final ProjectRepoClient projectRepoClient;

    final StatRepoClient statRepoClient;

    @Autowired
    public StatsController(StatsMongoClient statsMongoClient, ProjectRepoClient projectRepoClient, StatRepoClient statRepoClient) {
        this.statsMongoClient = statsMongoClient;
        this.projectRepoClient = projectRepoClient;
        this.statRepoClient = statRepoClient;
    }

    @Operation(description = "Retrieve statistics by Name", tags = {"stats"})
    @RequestMapping(value = "/stats/{name}", method = RequestMethod.GET)
    public Mono<ResponseEntity> statistics(@PathVariable String name) {
        Mono<Object> statsByName = statsMongoClient.getStatsByName(name);
        return statsByName.map(ResponseEntity::ok);
    }

//    @Operation(description = "Retrieve submissions count per country as TSV", tags = {"stats"})
//    @RequestMapping(value = "/submissions-per-country", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
//    public Mono<String> submissionsPerCountry() {
//        String name = "SUBMISSIONS_PER_COUNTRY";
//        Mono<MongoPrideStats> lastGeneratedStats = statsMongoClient.findLastGeneratedStats();
//        return lastGeneratedStats.map(s -> {
//            List<Tuple<String, Integer>> stats = s.getSubmissionsCount().get(name);
//            StringBuilder statsBuilder = new StringBuilder("Country\tNumber_of_submissions");
//            for (Tuple<String, Integer> tuple : stats) {
//                statsBuilder.append("\n").append(tuple.getKey()).append("\t").append(tuple.getValue());
//            }
//            return statsBuilder.toString();
//        });
//    }


//    @Operation(description = "Retrieve all statistics keys and names", tags = {"stats"})
//    @RequestMapping(value = "/stats/", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    public Mono<Object> getStatisticsNames() {
//
//        Mono<MongoPrideStats> lastGeneratedStats = statsMongoClient.findLastGeneratedStats();
//        return lastGeneratedStats.hasElement().flatMap(isPresent -> {
//            if(isPresent) {
//                return lastGeneratedStats.map(s -> {
//                    List<String> statNames = new ArrayList<>();
//                    Map<String, List<Tuple<String, Integer>>> submissionsCount = s.getSubmissionsCount();
//                    if (submissionsCount != null)
//                        statNames.addAll(new ArrayList<>(submissionsCount.keySet()));
//
//                    Map<String, Object> complexStats = s.getComplexStats();
//                    if (complexStats != null)
//                        statNames.addAll(new ArrayList<>(complexStats.keySet()));
//
//                    return statNames;
//                });
//            }
//            return Mono.empty();
//        });
//    }

    @Operation(description = "Retrieve month wise submissions count", tags = {"stats"})
    @RequestMapping(value = "/stats/submissions-monthly", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> submissionsMonthly() throws IOException {

        List<List<String>> results = projectRepoClient.findMonthlySubmissions();

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @Operation(description = "Retrieve month wise submissions count", tags = {"stats"})
    @RequestMapping(value = "/stats/submissions-monthly-tsv", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> submissionsMonthlyTsv() throws IOException {

        List<List<String>> results = projectRepoClient.findMonthlySubmissions();
        StringBuilder statsBuilder = new StringBuilder("Month\tNumber_of_submissions");
        for (List<String> row : results) {
            statsBuilder.append("\n").append(row.get(0)).append("\t").append(row.get(1));
        }
        return new ResponseEntity<>(statsBuilder.toString(), HttpStatus.OK);
    }

//    @Operation(description = "Retrieve peptidome stats", value = "peptidome-stats", nickname = "peptidome-stats", tags = {"stats"})
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "OK", response = APIError.class),
//            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
//    })
//    @RequestMapping(value = "/stats/peptidome-stats", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
//    public ResponseEntity<Object> peptidomeStats() throws IOException {
//
//        List<MongoPeptidomeStats> results = peptidomeStatsMongoService.findall();
//
//        return new ResponseEntity<>(results, HttpStatus.OK);
//    }

    @Operation(description = "Retrieve month wise submissions data size", tags = {"stats"})
    @RequestMapping(value = "/stats/submitted-data", method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<Object> getSubmittedDataStats() throws IOException {
        String submittedDataStats = statRepoClient.getSubmittedDataStats();
        return new ResponseEntity<>(submittedDataStats, HttpStatus.OK);
    }
}
