package fi.vm.yti.codelist.intake.service;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.common.dto.CodeAnnotationDTO;

public interface CodeAnnotationService {

    Set<CodeAnnotationDTO> findAll();

    Set<CodeAnnotationDTO> findByCodeId(UUID codeschemeId);

}