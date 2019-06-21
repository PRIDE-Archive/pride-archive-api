package uk.ac.ebi.pride.ws.pride.models.molecules;

import lombok.Builder;
import lombok.Data;
import uk.ac.ebi.pride.archive.dataprovider.common.Tuple;
import uk.ac.ebi.pride.archive.dataprovider.param.CvParamProvider;
import uk.ac.ebi.pride.ws.pride.models.param.CvParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class IdentifiedModification {

    private CvParam neutralLoss;
    private List<Tuple<Integer, List<CvParam>>> positionMap;
    private CvParam modification;
    private List<CvParam> attributes;
}
