package fi.vm.yti.codelist.intake.service;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.common.dto.CodeSchemeAnnotationDTO;

public interface CodeSchemeAnnotationService {

    Set<CodeSchemeAnnotationDTO> findAll();

    // CodeSchemeAnnotationDTO findByCodeschemeIdAndAnnotationId(final UUID codeschemeId, final UUID annotationId);

    Set<CodeSchemeAnnotationDTO> findByCodeschemeId(UUID codeschemeId);

}