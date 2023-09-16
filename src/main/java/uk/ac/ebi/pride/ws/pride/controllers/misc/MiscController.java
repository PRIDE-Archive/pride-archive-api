package uk.ac.ebi.pride.ws.pride.controllers.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.ebi.pride.archive.repo.client.ProjectRepoClient;

import java.io.IOException;
import java.util.List;

@Controller
public class MiscController {

    final ProjectRepoClient projectRepoClient;

    @Autowired
    public MiscController(ProjectRepoClient projectRepoClient) {
        this.projectRepoClient = projectRepoClient;
    }

    @GetMapping(value = "/misc/sitemap", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getSitemap() throws IOException {
        List<String> allPublicAccessions = projectRepoClient.getAllPublicAccessions();
        StringBuilder sb = new StringBuilder();
        String prideWeb = "https://www.ebi.ac.uk/pride/archive/projects/";
        allPublicAccessions.forEach(a -> sb.append(prideWeb).append(a).append("\n"));
        return new ResponseEntity(sb.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/misc/sitemap.txt", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getSitemapFile() throws IOException {
        List<String> allPublicAccessions = projectRepoClient.getAllPublicAccessions();
        StringBuilder sb = new StringBuilder();
        String prideWeb = "https://www.ebi.ac.uk/pride/archive/projects/";
        allPublicAccessions.forEach(a -> sb.append(prideWeb).append(a).append("\n"));
        return sb.toString().getBytes();
    }

}
