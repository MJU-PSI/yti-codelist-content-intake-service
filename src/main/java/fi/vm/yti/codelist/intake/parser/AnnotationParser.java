package fi.vm.yti.codelist.intake.parser;

import java.util.Set;

import fi.vm.yti.codelist.common.dto.AnnotationDTO;

public interface AnnotationParser {

    AnnotationDTO parseAnnotationFromJson(final String jsonPayload);

    Set<AnnotationDTO> parseAnnotationsFromJson(final String jsonPayload);
}
