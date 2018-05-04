package uk.ac.ebi.pride.ws.pride.mappers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.solr.indexes.pride.model.PrideSolrDataset;
import uk.ac.ebi.pride.ws.pride.controllers.DatasetController;
import uk.ac.ebi.pride.ws.pride.models.dataset.CompactDataset;
import uk.ac.ebi.pride.ws.pride.models.dataset.ResourceDataset;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ypriverol
 */
public class DatasetResourceMapper extends ResourceAssemblerSupport<PrideSolrDataset, ResourceDataset> {

    public DatasetResourceMapper(Class<?> controller, Class<ResourceDataset> resourceType) {
        super(controller, resourceType);
    }

    @Override
    public ResourceDataset toResource(PrideSolrDataset prideSolrDataset) {
        return null;
    }

    @Override
    public List<ResourceDataset> toResources(Iterable<? extends PrideSolrDataset> entities) {

        List<ResourceDataset> datasets = new ArrayList<>();

        for(PrideSolrDataset prideSolrDataset: entities){
            CompactDataset dataset = CompactDataset.builder()
                    .accession(prideSolrDataset.getAccession())
                    .title(prideSolrDataset.getTitle())
                    .projectDescription(prideSolrDataset.getProjectDescription()).build();
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(DatasetController.class).getDataset(prideSolrDataset.getAccession())).withSelfRel());
            datasets.add(new ResourceDataset(dataset, links));
        }

        return datasets;
    }
}
