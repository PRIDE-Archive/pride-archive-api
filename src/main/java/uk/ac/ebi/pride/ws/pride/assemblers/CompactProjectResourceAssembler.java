package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.solr.core.query.result.FacetAndHighlightPage;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import uk.ac.ebi.pride.solr.commons.PrideSolrProject;
import uk.ac.ebi.pride.ws.pride.controllers.project.MassSpecProjectController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProject;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactProjectModel;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author ypriverol
 */
@Slf4j
public class CompactProjectResourceAssembler extends RepresentationModelAssemblerSupport<PrideSolrProject, CompactProjectModel> {

    public CompactProjectResourceAssembler(Class<?> controller, Class<CompactProjectModel> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public CompactProjectModel toModel(PrideSolrProject prideSolrDataset) {
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(MassSpecProjectController.class).getProject(prideSolrDataset.getAccession())).withSelfRel());
        return new CompactProjectModel(transform(prideSolrDataset), links);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CollectionModel<CompactProjectModel> toCollectionModel(Iterable<? extends PrideSolrProject> entities) {

        List<CompactProjectModel> datasets = new ArrayList<>();
        for (PrideSolrProject prideSolrDataset : entities) {
            CompactProject dataset = transform(prideSolrDataset);
            if (entities instanceof FacetAndHighlightPage) {
                FacetAndHighlightPage<PrideSolrProject> facetPages = (FacetAndHighlightPage<PrideSolrProject>) entities;
                dataset.setHighlights(facetPages.getHighlights(prideSolrDataset).stream().collect(Collectors.toMap(x -> x.getField().getName(), HighlightEntry.Highlight::getSnipplets)));
            }
            List<Link> links = new ArrayList<>();
            links.add(linkTo(methodOn(MassSpecProjectController.class).getProject(prideSolrDataset.getAccession())).withSelfRel());
            Date publicationDate = dataset.getPublicationDate();
            SimpleDateFormat year = new SimpleDateFormat("YYYY");
            SimpleDateFormat month = new SimpleDateFormat("MM");
            String ftpPath = "ftp://ftp.pride.ebi.ac.uk/pride/data/archive/" + year.format(publicationDate).toUpperCase() + "/" + month.format(publicationDate).toUpperCase() + "/" + dataset.getAccession();
            links.add(Link.of(UriTemplate.of(ftpPath), "datasetFtpUrl"));

            datasets.add(new CompactProjectModel(dataset, links));
        }

        return CollectionModel.of(datasets);
    }

    /**
     * Transform  Solr Project into Compact Project
     *
     * @param prideSolrDataset solr project
     * @return CompactProject
     */
    private CompactProject transform(PrideSolrProject prideSolrDataset) {

        String license = null;
        try {
            license = WsUtils.getLicenseFromDate(prideSolrDataset.getSubmissionDate());
        } catch (ParseException e) {
            log.info("Error generating the license for dataset -- " + prideSolrDataset.getAccession());
        }

        return CompactProject.builder()
                .accession(prideSolrDataset.getAccession())
                .title(prideSolrDataset.getTitle())
                .projectDescription(prideSolrDataset.getProjectDescription())
                .additionalAttributes(prideSolrDataset.getAdditionalAttributesStrings())
                .affiliations(prideSolrDataset.getAffiliations())
                .dataProcessingProtocol(prideSolrDataset.getDataProcessingProtocol())
                .sampleProcessingProtocol(prideSolrDataset.getSampleProcessingProtocol())
                .diseases(prideSolrDataset.getDiseases())
                .organisms(prideSolrDataset.getOrganisms())
                .organismParts(prideSolrDataset.getOrganismPart())
                .instruments(prideSolrDataset.getInstruments())
                .submitters(prideSolrDataset.getSubmitters())
                .keywords(prideSolrDataset.getKeywords())
                .projectTags(prideSolrDataset.getProjectTags())
                .labPIs(prideSolrDataset.getLabPIs())
                .identifiedPTMS(prideSolrDataset.getIdentifiedPTMStrings())
                .publicationDate(prideSolrDataset.getPublicationDate())
                .quantificationMethods(prideSolrDataset.getQuantificationMethods())
                .references(new HashSet<>(prideSolrDataset.getReferences()))
                .softwares(prideSolrDataset.getSoftwares())
                .submissionDate(prideSolrDataset.getSubmissionDate())
                .updatedDate(prideSolrDataset.getUpdatedDate())
                .sdrf(prideSolrDataset.getSdrf())
                .license(license)
                .queryScore((prideSolrDataset.getScore() != null) ? prideSolrDataset.getScore().doubleValue() : null)
                .build();
    }


}
