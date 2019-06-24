package uk.ac.ebi.pride.ws.pride.assemblers.molecules;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import uk.ac.ebi.pride.archive.dataprovider.data.peptide.PSMProvider;
import uk.ac.ebi.pride.ws.pride.controllers.molecules.SpectraEvidenceController;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumEvidence;
import uk.ac.ebi.pride.ws.pride.models.molecules.SpectrumEvidenceResource;
import uk.ac.ebi.pride.ws.pride.utils.WsUtils;

import java.util.ArrayList;
import java.util.List;

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

    private SpectrumEvidence transform(PSMProvider archiveSpectrum){
        return SpectrumEvidence.builder()
                .usi(archiveSpectrum.getUsi())
                .peptideSequence(archiveSpectrum.getPeptideSequence())
                .intensities(archiveSpectrum.getIntensities())
                .mzs(archiveSpectrum.getMasses())
                .numPeaks(archiveSpectrum.getIntensities().length)
                .isDecoy(archiveSpectrum.isDecoy())
                .build();

    }
}
