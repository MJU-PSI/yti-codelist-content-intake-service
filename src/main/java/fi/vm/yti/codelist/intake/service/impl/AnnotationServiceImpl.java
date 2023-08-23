package fi.vm.yti.codelist.intake.service.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.AnnotationDTO;
import fi.vm.yti.codelist.common.dto.ErrorModel;
import fi.vm.yti.codelist.intake.dao.AnnotationDao;
import fi.vm.yti.codelist.intake.exception.YtiCodeListException;
import fi.vm.yti.codelist.intake.model.Annotation;
import fi.vm.yti.codelist.intake.parser.AnnotationParser;
import fi.vm.yti.codelist.intake.service.AnnotationService;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.*;

@Singleton
@Service
public class AnnotationServiceImpl implements AnnotationService, AbstractBaseService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationServiceImpl.class);

    private final AnnotationDao annotationDao;
    private final DtoMapperService dtoMapperService;
    private final AnnotationParser annotationParser;

    @Inject
    public AnnotationServiceImpl(final AnnotationDao annotationDao,
                                 final DtoMapperService dtoMapperService,
                                 AnnotationParser annotationParser) {
        this.annotationDao = annotationDao;
        this.dtoMapperService = dtoMapperService;
        this.annotationParser = annotationParser;
    }

    @Transactional
    public Set<AnnotationDTO> findAll() {
        return dtoMapperService.mapDeepAnnotationDtos(annotationDao.findAll());
    }

    @Transactional
    public AnnotationDTO findById(final UUID id) {
        final Annotation annotation = annotationDao.findById(id);
        if (annotation != null) {
            return dtoMapperService.mapDeepAnnotationDto(annotation);
        }
        return null;
    }

    @Transactional
    public AnnotationDTO findByCodeValue(final String codeValue) {
        final Annotation annotation = annotationDao.findByCodeValue(codeValue);
        if (annotation != null) {
            return dtoMapperService.mapDeepAnnotationDto(annotation);
        }
        return null;
    }

    @Transactional
    public AnnotationDTO deleteAnnotation(final String annotationCodeValue) {

        return null;
    }

    @Transactional
    public AnnotationDTO updateAnnotationFromDto(final AnnotationDTO annotationDto) {
        return null;
    }

    @Transactional
    public Set<AnnotationDTO> parseAndPersistAnnotationFromJson(final String jsonPayload) {
        Annotation annotation;
        if (jsonPayload != null && !jsonPayload.isEmpty()) {
            final AnnotationDTO annotationDto = annotationParser.parseAnnotationFromJson(jsonPayload);
            annotation = annotationDao.updateAnnotationFromDto(annotationDto);
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_JSON_PAYLOAD_EMPTY));
        }
        if (annotation == null) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_ANNOTATIONS_ARE_EMPTY));
        }
        Set<Annotation> annotations = new HashSet<>();
        annotations.add(annotation);
        return dtoMapperService.mapDeepAnnotationDtos(annotations);
    }

    @Transactional
    public Set<AnnotationDTO> parseAndPersistAnnotationsSourceData(final String jsonPayload) {
        final Set<Annotation> annotations;
        if (jsonPayload != null && !jsonPayload.isEmpty()) {
            final Set<AnnotationDTO> annotationsDto = annotationParser.parseAnnotationsFromJson(jsonPayload);
            annotations = annotationDao.updateAnnotationsFromDto(annotationsDto);
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_JSON_PAYLOAD_EMPTY));
        }
        if (annotations == null) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_ANNOTATIONS_ARE_EMPTY));
        }
        return dtoMapperService.mapDeepAnnotationDtos(annotations);
    }
}
