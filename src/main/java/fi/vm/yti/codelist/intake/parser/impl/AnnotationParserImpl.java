package fi.vm.yti.codelist.intake.parser.impl;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.vm.yti.codelist.common.dto.AnnotationDTO;
import fi.vm.yti.codelist.intake.configuration.UriProperties;
import fi.vm.yti.codelist.intake.exception.JsonParsingException;
import fi.vm.yti.codelist.intake.parser.AnnotationParser;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.*;

@Component
public class AnnotationParserImpl extends AbstractBaseParser implements AnnotationParser {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationParserImpl.class);

    @Autowired
    UriProperties uriProperties;

    public AnnotationDTO parseAnnotationFromJson(final String jsonPayload) {
        final ObjectMapper mapper = createObjectMapper();
        final AnnotationDTO annotation;
        try {
            annotation = mapper.readValue(jsonPayload, AnnotationDTO.class);
        } catch (final IOException e) {
            LOG.error("Annotation parsing failed from JSON!", e);
            throw new JsonParsingException(ERR_MSG_USER_ANNOTATION_PARSING_FAILED);
        }
        return annotation;
    }

    public Set<AnnotationDTO> parseAnnotationsFromJson(final String jsonPayload) {
        final ObjectMapper mapper = createObjectMapper();
        final Set<AnnotationDTO> annotations;
        try {
            annotations = mapper.readValue(jsonPayload, new TypeReference<Set<AnnotationDTO>>() {
            });
        } catch (final IOException e) {
            LOG.error("Annotation parsing failed from JSON!", e);
            throw new JsonParsingException(ERR_MSG_USER_ANNOTATION_PARSING_FAILED);
        }
        return annotations;
    }
}
