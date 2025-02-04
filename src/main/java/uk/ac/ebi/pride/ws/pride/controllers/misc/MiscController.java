package uk.ac.ebi.pride.ws.pride.controllers.misc;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;

import java.io.IOException;
import java.util.List;

@Controller
public class MiscController {

    final ProjectRepoClient projectRepoClient;

    @Value("${bluesky-posts-url}")
    private String blueskyPostsUrl;

    @Autowired
    public MiscController(ProjectRepoClient projectRepoClient) {
        this.projectRepoClient = projectRepoClient;
    }

    @Hidden
    @GetMapping(value = "/misc/sitemap", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getSitemap() throws IOException {
        List<String> allPublicAccessions = projectRepoClient.getAllPublicAccessions();
        StringBuilder sb = new StringBuilder();
        String prideWeb = "https://www.ebi.ac.uk/pride/archive/projects/";
        allPublicAccessions.forEach(a -> sb.append(prideWeb).append(a).append("\n"));
        return new ResponseEntity(sb.toString(), HttpStatus.OK);
    }

    @Hidden
    @GetMapping(value = "/misc/sitemap.txt", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getSitemapFile() throws IOException {
        List<String> allPublicAccessions = projectRepoClient.getAllPublicAccessions();
        StringBuilder sb = new StringBuilder();
        String prideWeb = "https://www.ebi.ac.uk/pride/archive/projects/";
        allPublicAccessions.forEach(a -> sb.append(prideWeb).append(a).append("\n"));
        return sb.toString().getBytes();
    }

    @Hidden
    @GetMapping(value = "/misc/bluesky-posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getBlueskyPosts() throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(blueskyPostsUrl, String.class);
        return new ResponseEntity(forObject, HttpStatus.OK);

    }

}
