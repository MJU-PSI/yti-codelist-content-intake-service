package fi.vm.yti.codelist.intake.service;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.common.dto.AnnotationDTO;

public interface AnnotationService {

    Set<AnnotationDTO> findAll();

    AnnotationDTO findById(final UUID id);

    AnnotationDTO findByCodeValue(final String codeValue);

    AnnotationDTO deleteAnnotation(final String annotationCodeValue);

    AnnotationDTO updateAnnotationFromDto(final AnnotationDTO annotationDTO);

    Set<AnnotationDTO> parseAndPersistAnnotationFromJson(final String jsonPayload);

    Set<AnnotationDTO> parseAndPersistAnnotationsSourceData(final String jsonPayload);
}