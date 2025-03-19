package uk.ac.ebi.pride.ws.pride.transformers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.ac.ebi.pride.archive.elastic.commons.models.ElasticPrideProject;

@Mapper(componentModel = "spring")
public interface ElasticPrideProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "allCountries", ignore = true)
    @Mapping(target = "suggestField", ignore = true)
    @Mapping(target = "prideArchiveType", ignore = true)
    ElasticPrideProject toDto(ElasticPrideProject project);
}