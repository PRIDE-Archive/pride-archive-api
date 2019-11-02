package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.archive.dataprovider.data.ptm.IdentifiedModification;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParam;
import uk.ac.ebi.pride.mongodb.molecules.model.psm.PrideMongoPsmSummaryEvidence;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.SpectraEvidenceController;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumEvidenceResource;
import uk.ac.ebi.pride.ws.pride.transformers.Transformer;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.*;
import java.util.stream.Collectors;

public class SpectraResourceAssembler extends ResourceAssemblerSupport<PSMProvider, SpectrumEvidenceResource> {

    public SpectraResourceAssembler(Class<?> controllerClass, Class<SpectrumEvidenceResource> resourceType) {
        super(controllerClass, resourceType);
    }

    @Override
    public SpectrumEvidenceResource toResource(PSMProvider archiveSpectrum) {

        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(SpectraEvidenceController.class)
                        .getSpectrum(WsUtils.getIdentifier(archiveSpectrum.getUsi())))
                .withSelfRel());

        return new SpectrumEvidenceResource(transform(archiveSpectrum), links);
    }

    @Override
    public List<SpectrumEvidenceResource> toResources(Iterable<? extends PSMProvider> entities) {
        List<SpectrumEvidenceResource> spectrumList = new ArrayList<>();

        for(PSMProvider psmProvider : entities){
            SpectrumEvidence spectrum = transform(psmProvider);
            List<Link> links = new ArrayList<>();
            links.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SpectraEvidenceController.class).getSpectrum(spectrum.getUsi())).withSelfRel());
            spectrumList.add(new SpectrumEvidenceResource(spectrum, links));
        }
        return spectrumList;
    }

    private SpectrumEvidence transform(PSMProvider archiveSpectrum){

        Set<CvParam> attributes = archiveSpectrum.getAttributes().stream()
                .map( x -> new CvParam(x.getCvLabel(), x.getAccession(), x.getName(), x.getValue()))
                .collect(Collectors.toSet());

        List<IdentifiedModification> ptms = new ArrayList<>();
        if(archiveSpectrum.getModifications() != null)
            ptms = Transformer.transformModifications(archiveSpectrum.getModifications());

        HashMap<Double, Double> peaks = new HashMap<>();
        for(int i=0; i < archiveSpectrum.getMasses().length; i++){
            peaks.put(archiveSpectrum.getMasses()[i], archiveSpectrum.getIntensities()[i]);
        }

        peaks = peaks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        Iterator<Map.Entry<Double, Double>> it = peaks.entrySet().iterator();
        int count = 0;
        Map.Entry<Double, Double> entry;
        while(it.hasNext()){
            entry = it.next();
            archiveSpectrum.getMasses()[count] = entry.getKey();
            archiveSpectrum.getIntensities()[count] = entry.getValue();
            count++;
        }

        return SpectrumEvidence.builder()
                .usi(archiveSpectrum.getUsi())
                .peptideSequence(archiveSpectrum.getPeptideSequence())
                .intensities(archiveSpectrum.getIntensities())
                .mzs(archiveSpectrum.getMasses())
                .numPeaks(archiveSpectrum.getIntensities().length)
                .isDecoy(archiveSpectrum.isDecoy())
                .isValid(archiveSpectrum.isValid())
                .qualityMethods(archiveSpectrum.getQualityEstimationMethods()
                        .stream()
                        .map( x-> new CvParam(((CvParamProvider) x).getCvLabel(), ((CvParamProvider) x).getAccession(),
                                ((CvParamProvider) x).getName(), ((CvParamProvider) x).getValue()))
                        .collect(Collectors.toSet()))
                .attributes(attributes)
                .ptms(ptms)
                .charge(archiveSpectrum.getPrecursorCharge())
                .precursorMZ(archiveSpectrum.getPrecursorMz())
                .build();

    }

    public SpectrumEvidenceResource toResource(PrideMongoPsmSummaryEvidence psmEvidence) {

        Set<CvParam> attributes = new HashSet<>();
        if(psmEvidence.getAdditionalAttributes() != null)
            attributes = psmEvidence.getAdditionalAttributes();


        SpectrumEvidence spectrum = SpectrumEvidence.builder()
                .usi(psmEvidence.getUsi())
                .peptideSequence(psmEvidence.getPeptideSequence())
                .isDecoy(psmEvidence.getIsDecoy())
                .isValid(psmEvidence.getIsValid())
                .attributes(attributes)
                .charge(psmEvidence.getCharge())
                .precursorMZ(psmEvidence.getPrecursorMass())
                .attributes(attributes)
                .build();

        List<Link> links = new ArrayList<>();
        links.add(ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(SpectraEvidenceController.class)
                        .getSpectrum(spectrum.getUsi()))
                .withSelfRel());

        return new SpectrumEvidenceResource(spectrum, links);
    }
}
