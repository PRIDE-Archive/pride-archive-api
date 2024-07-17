package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.mongo.commons.model.files.MongoPrideFile;
import uk.ac.ebi.pride.ws.pride.models.file.PrideFile;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ypriverol
 */
@Slf4j
public class ProjectFileResourceAssembler {

    public static PrideFile toModel(MongoPrideFile mongoFile) {

        Set<CvParamProvider> additionalAttributes = mongoFile.getAdditionalAttributes() != null ? mongoFile.getAdditionalAttributes().stream()
                .map(x -> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue())).collect(Collectors.toSet()) : Collections.emptySet();
        Set<CvParamProvider> publicFileLocations = mongoFile.getPublicFileLocations() != null ? mongoFile.getPublicFileLocations().stream()
                .map(x -> {
                    String value = getFTPUrl(x.getValue());
                    return new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), value);
                }).collect(Collectors.toSet()) : Collections.emptySet();

        log.debug(mongoFile.toString());

        CvParamProvider category = mongoFile.getFileCategory() != null ? new CvParam(mongoFile.getFileCategory().getCvLabel(),
                mongoFile.getFileCategory().getAccession(), mongoFile.getFileCategory().getName(), mongoFile.getFileCategory().getValue()) : null;

        return PrideFile.builder()
                .accession(mongoFile.getAccession())
                .additionalAttributes(additionalAttributes)
                .analysisAccessions(mongoFile.getAnalysisAccessions())
                .projectAccessions(mongoFile.getProjectAccessions())
                .compress(mongoFile.isCompress())
                .fileCategory(category)
                .fileName(mongoFile.getFileName())
                .fileSizeBytes(mongoFile.getFileSizeBytes())
                .checksum(mongoFile.getChecksum())
                .publicationDate(mongoFile.getPublicationDate())
                .publicFileLocations(publicFileLocations)
                .updatedDate(mongoFile.getUpdatedDate())
                .submissionDate(mongoFile.getSubmissionDate())
                .build();
    }

    public static String getFTPUrl(String value) {
        value = value.startsWith("ftp://") ? value.replaceAll("#", "%23") : value;
//        value = value.replace("ftp://ftp.pride.ebi.ac.uk/pride/data/archive/", "ftp://ftp.ebi.ac.uk/pride-archive/");
        return value;
    }
}
