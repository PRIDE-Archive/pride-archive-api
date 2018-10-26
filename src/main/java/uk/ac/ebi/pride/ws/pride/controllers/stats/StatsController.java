package uk.ac.ebi.pride.ws.pride.controllers.stats;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.ac.ebi.pride.archive.dataprovider.utils.Tuple;
import uk.ac.ebi.pride.mongodb.archive.model.stats.MongoPrideStats;
import uk.ac.ebi.pride.mongodb.archive.service.stats.PrideStatsMongoService;
import uk.ac.ebi.pride.ws.pride.hateoas.CustomPagedResourcesAssembler;
import uk.ac.ebi.pride.ws.pride.utils.APIError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

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

    @Autowired
    public StatsController(PrideStatsMongoService mongoStatsService, CustomPagedResourcesAssembler customPagedResourcesAssembler) {
        this.mongoStatsService = mongoStatsService;
        this.customPagedResourcesAssembler = customPagedResourcesAssembler;
    }


    @ApiOperation(notes = "Retrieve statistics by Name", value = "statistics", nickname = "getStatsByName", tags = {"stats"} )
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = APIError.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = APIError.class)
    })
    @RequestMapping(value = "/stats/{name}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> statistics(@PathVariable(value = "name", required = true, name = "name") String name){

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
                statNames.addAll(stats.getSubmissionsCount().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
            if(stats.getComplexStats() != null)
                statNames.addAll(stats.getComplexStats().entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
        }

        return new ResponseEntity<>(statNames, HttpStatus.OK);
    }

}
