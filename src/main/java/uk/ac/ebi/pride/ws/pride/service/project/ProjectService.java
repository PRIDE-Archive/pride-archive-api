package uk.ac.ebi.pride.ws.pride.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.archive.repo.repos.project.Project;
import uk.ac.ebi.pride.archive.repo.repos.project.ProjectRepository;
import uk.ac.ebi.pride.archive.repo.repos.user.UserRepository;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Project> findUserProjects(String userReference, boolean isPublic) {
        //TODO remove hard coded value
        Long userId = userRepository.findByUserRef(/*userReference"usr-7a201de8-7acf-4e6f-a2fd-c30073d9c249"*/"usr-55ef870a-fdae-4a37-9d68-952a907b273a").getId();
        List<Project> projectsList = projectRepository.findFilteredBySubmitterIdAndIsPublic(userId,isPublic);
        return projectsList;
    }

    public List<Project> findReviewerProjects(String userReference) {
        List<Project> projectsList = projectRepository.findFilteredByReviewer(userReference);
        return projectsList;
    }
}
