package uk.ac.ebi.pride.ws.pride.mappers;

import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

/**
 * @author ypriverol
 */
public abstract class EntityMapper<T, R extends ResourceSupport> extends ResourceAssemblerSupport<T, R> {

    private final PagedResourcesAssembler<T> pagedResourcesAssembler;

    protected <C> EntityMapper(Class<C> controllerClass, Class<R> resourceClass,
                               PagedResourcesAssembler<T> pagedResourcesAssembler) {
        super(controllerClass, resourceClass);
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    public PagedResources<R> toPagedResources(Page<T> page) {
        return pagedResourcesAssembler.toResource(page, this);
    }

    public abstract T toEntity(R resource);

}
