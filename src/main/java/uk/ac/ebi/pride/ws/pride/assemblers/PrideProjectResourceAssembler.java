package uk.ac.ebi.pride.ws.pride.assemblers;

import lombok.extern.slf4j.Slf4j;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.archive.mongo.commons.model.projects.MongoPrideProject;
import uk.ac.ebi.pride.utilities.term.CvTermReference;
import uk.ac.ebi.pride.ws.pride.models.dataset.PrideProject;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.text.ParseException;
import java.util.*;


/**
 * This code is licensed under the Apache License, Version 2.0 (the
 *
 * @author ypriverol
 */
@Slf4j
public class PrideProjectResourceAssembler {

    public static PrideProject toModel(MongoPrideProject mongoPrideProject) {
        return transform(mongoPrideProject);
    }

//        return filePageMono.map(page -> {
//            String ftpPath = null;
//            List<MongoPrideFile> mongoFiles = page.getContent();
//            if (mongoFiles != null && !mongoFiles.isEmpty()) {
//                MongoPrideFile mongoPrideFile = mongoFiles.get(0);
//                Set<? extends CvParamProvider> publicFileLocations = mongoPrideFile.getPublicFileLocations();
//                Optional<String> ftpPathOptional = publicFileLocations.stream().filter(l -> l.getAccession().equals("PRIDE:0000469")).map(ParamProvider::getValue).findFirst();
//                if (ftpPathOptional.isPresent()) {
//                    ftpPath = ftpPathOptional.get();
//                }
//                ftpPath = ftpPath.substring(0, ftpPath.lastIndexOf("/"));
//            }
//            if (ftpPath == null) {
//                Date publicationDate = mongoPrideProject.getPublicationDate();
//                SimpleDateFormat year = new SimpleDateFormat("YYYY");
//                SimpleDateFormat month = new SimpleDateFormat("MM");
//                ftpPath = "ftp://ftp.pride.ebi.ac.uk/pride/data/archive/" + year.format(publicationDate).toUpperCase() + "/" + month.format(publicationDate).toUpperCase() + "/" + mongoPrideProject.getAccession();
//            }
//            return ftpPath;
//        }).block();
//        List<MongoPrideFile> mongoFiles = mongoFileService.findFilesByProjectAccession(mongoPrideProject.getAccession());
//        if (mongoFiles != null && !mongoFiles.isEmpty()) {
//            MongoPrideFile mongoPrideFile = mongoFiles.get(0);
//            Set<? extends CvParamProvider> publicFileLocations = mongoPrideFile.getPublicFileLocations();
//            Optional<String> ftpPathOptional = publicFileLocations.stream().filter(l -> l.getAccession().equals("PRIDE:0000469")).map(ParamProvider::getValue).findFirst();
//            if (ftpPathOptional.isPresent()) {
//                ftpPath = ftpPathOptional.get();
//            }
//            ftpPath = ftpPath.substring(0, ftpPath.lastIndexOf("/"));
//        }
//
//        if (ftpPath == null) {
//            Date publicationDate = mongoPrideProject.getPublicationDate();
//            SimpleDateFormat year = new SimpleDateFormat("YYYY");
//            SimpleDateFormat month = new SimpleDateFormat("MM");
//            ftpPath = "ftp://ftp.pride.ebi.ac.uk/pride/data/archive/" + year.format(publicationDate).toUpperCase() + "/" + month.format(publicationDate).toUpperCase() + "/" + mongoPrideProject.getAccession();
//        }
//        return ftpPath;

    /**
     * Transform the original mongo Project to {@link PrideProject} that is used to external users.
     *
     * @param mongoPrideProject {@link MongoPrideProject}
     * @return Pride Project
     */
    public static PrideProject transform(MongoPrideProject mongoPrideProject) {
        Collection<CvParam> additionalAttributes = mongoPrideProject.getAttributes();
        String license = null;
        try {
            license = WsUtils.getLicenseFromDate(mongoPrideProject.getSubmissionDate());
        } catch (ParseException e) {
            log.info("Error generating the license for dataset -- " + mongoPrideProject.getAccession());
        }

        if (additionalAttributes == null)
            additionalAttributes = new ArrayList<>();

//        additionalAttributes.add(new CvParam("PRIDE", "PRIDE:0000411", "Dataset FTP location", getFtpPath(mongoPrideProject)));

        return PrideProject.builder()
                .accession(mongoPrideProject.getAccession())
                .title(mongoPrideProject.getTitle())
                .references(new HashSet<>(mongoPrideProject.getReferences()))
                .projectDescription(mongoPrideProject.getDescription())
                .projectTags(mongoPrideProject.getProjectTags())
                .additionalAttributes(additionalAttributes)
                .identifiedPTMStrings(new HashSet<>(mongoPrideProject.getPtmList()))
                .sampleProcessingProtocol(mongoPrideProject.getSampleProcessing())
                .dataProcessingProtocol(mongoPrideProject.getDataProcessing())
                .countries(mongoPrideProject.getCountries() != null ? new HashSet<>(mongoPrideProject.getCountries()) : Collections.EMPTY_SET)
                .keywords(mongoPrideProject.getKeywords())
                .doi(mongoPrideProject.getDoi())
                .submissionType(mongoPrideProject.getSubmissionType())
                .publicationDate(mongoPrideProject.getPublicationDate())
                .submissionDate(mongoPrideProject.getSubmissionDate())
                .instruments(new ArrayList<>(mongoPrideProject.getInstruments()))
                .quantificationMethods(new ArrayList<>(mongoPrideProject.getQuantificationMethods()))
                .softwares(new ArrayList<>(mongoPrideProject.getSoftwareList()))
                .experimentTypes(new ArrayList<>(mongoPrideProject.getExperimentTypes()))
                .submitters(new ArrayList<>(mongoPrideProject.getSubmitters()))
                .labPIs(new ArrayList<>(mongoPrideProject.getHeadLab()))
                .organisms(WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_ORGANISM))
                .diseases(WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_DISEASE))
                .organismParts(WsUtils.getCvTermsValues(mongoPrideProject.getSamplesDescription(), CvTermReference.EFO_ORGANISM_PART))
                .sampleAttributes(mongoPrideProject.getSamplesDescription() != null ? new ArrayList(mongoPrideProject.getSamplesDescription()) : Collections.emptyList())
                .license(license)
                .totalFileDownloads(mongoPrideProject.getTotalDownloads())
                .build();
    }
}
