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
import uk.ac.ebi.pride.archive.repo.repos.project.ProjectRepository;
import uk.ac.ebi.pride.mongodb.archive.model.stats.MongoPrideStats;
import uk.ac.ebi.pride.mongodb.archive.service.stats.PrideStatsMongoService;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * Created by ypriverol (ypriverol@gmail.com) on 28/06/2018.
 */
@Controller
public class StatsController {

    final PrideStatsMongoService mongoStatsService;

    final CustomPagedResourcesAssembler customPagedResourcesAssembler;

    final ProjectRepository projectRepo;

    @Autowired
    public StatsController(PrideStatsMongoService mongoStatsService, CustomPagedResourcesAssembler customPagedResourcesAssembler,ProjectRepository projectRepo) {
        this.mongoStatsService = mongoStatsService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
        this.projectRepo = projectRepo;
    }


    @ApiOperation(notes = "Retrieve statistics by Name", value = "statistics", nickname = "getStatsByName", tags = {"stats"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/{name}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> statistics(@PathVariable(value = "name", name = "name") String name){

        Object stats = mongoStatsService.findLastGeneratedStats().getSubmissionsCount().get(name);
        if (stats == null || ((List)stats).size() == 0)
            stats = mongoStatsService.findLastGeneratedStats().getComplexStats().get(name);

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }


    @ApiOperation(notes = "Retrieve all statistics keys and names", value = "statistics", nickname = "getStatNames", tags = {"stats"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> getStatisticsNames(){

        List<String> statNames = new ArrayList<>();
        MongoPrideStats stats = mongoStatsService.findLastGeneratedStats();
        if (stats != null){
            if(stats.getSubmissionsCount() != null)
                statNames.addAll(stats.getSubmissionsCount().keySet().stream().collect(Collectors.toList()));
            if(stats.getComplexStats() != null)
                statNames.addAll(stats.getComplexStats().keySet().stream().collect(Collectors.toList()));
        }

        return new ResponseEntity<>(statNames, HttpStatus.OK);
    }

    @ApiOperation(notes = "Retrieve month wise submissions count", value = "submissions-monthly", nickname = "submissions-monthly", tags = {"stats"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/submissions-monthly", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> submissionsMonthly(){

        List<List<String>> results = projectRepo.findMonthlySubmissions();

        return new ResponseEntity<>(results, HttpStatus.OK);
    }



}
