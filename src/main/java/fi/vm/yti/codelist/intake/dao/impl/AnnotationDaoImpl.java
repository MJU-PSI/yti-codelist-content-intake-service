package fi.vm.yti.codelist.intake.dao.impl;

import java.util.*;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.AnnotationDTO;
import fi.vm.yti.codelist.common.dto.ErrorModel;
import fi.vm.yti.codelist.intake.dao.AnnotationDao;
import fi.vm.yti.codelist.intake.exception.YtiCodeListException;
import fi.vm.yti.codelist.intake.jpa.AnnotationRepository;
import fi.vm.yti.codelist.intake.language.LanguageService;
import fi.vm.yti.codelist.intake.log.EntityChangeLogger;
import fi.vm.yti.codelist.intake.model.Annotation;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.*;
import static fi.vm.yti.codelist.intake.parser.impl.AbstractBaseParser.validateCodeValue;

@Component
public class AnnotationDaoImpl extends AbstractDao implements AnnotationDao {

    private final EntityChangeLogger entityChangeLogger;
    private final AnnotationRepository annotationRepository;
    private final LanguageService languageService;

    @Inject
    public AnnotationDaoImpl(final EntityChangeLogger entityChangeLogger,
                             final AnnotationRepository annotationRepository,
                             final LanguageService languageService) {
        super(languageService);
        this.entityChangeLogger = entityChangeLogger;
        this.annotationRepository = annotationRepository;
        this.languageService = languageService;
    }

    @Transactional
    public void delete(final Annotation annotation) {
        entityChangeLogger.logAnnotationChange(annotation);
        annotationRepository.delete(annotation);
    }

    @Transactional
    public void save(final Annotation annotation) {
        annotationRepository.save(annotation);
        entityChangeLogger.logAnnotationChange(annotation);
    }

    @Transactional
    public void save(final Set<Annotation> annotations) {
        annotationRepository.saveAll(annotations);
    }

    @Transactional
    public Annotation findById(final UUID id) {
        return annotationRepository.findById(id);
    }

    @Transactional
    public Annotation findByCodeValue(final String codeValue) {
        return annotationRepository.findByCodeValueIgnoreCase(codeValue);
    }

    @Transactional
    public Set<Annotation> findAll() {
        return annotationRepository.findAll();
    }

    @Transactional
    public Annotation updateAnnotationFromDto(final AnnotationDTO annotationDto) {
        final Annotation annotation = createOrUpdateAnnotation(annotationDto);
        annotationRepository.save(annotation);
        entityChangeLogger.logAnnotationChange(annotation);
        return annotation;
    }

    @Transactional
    public Set<Annotation> updateAnnotationsFromDto(final Set<AnnotationDTO> annotationDtos) {
        final Set<Annotation> addedOrUpdatedAnnotations = new HashSet<>();
        for (final AnnotationDTO annotationDto : annotationDtos) {
            final Annotation annotation = createOrUpdateAnnotation(annotationDto);
            annotationRepository.save(annotation);
            entityChangeLogger.logAnnotationChange(annotation);
            addedOrUpdatedAnnotations.add(annotation);
        }
        if (!addedOrUpdatedAnnotations.isEmpty()) {
            save(addedOrUpdatedAnnotations);
        }
        return addedOrUpdatedAnnotations;
    }

    @Transactional
    public Annotation createOrUpdateAnnotation(final AnnotationDTO fromAnnotation) {

        final Annotation existingAnnotation;
        existingAnnotation = annotationRepository.findByCodeValueIgnoreCase(fromAnnotation.getCodeValue());

        final Annotation annotation;
        if (existingAnnotation != null) {
            validateAnnotationCodeValueForExistingAnnotation(fromAnnotation);
            annotation = updateAnnotation(existingAnnotation, fromAnnotation);
        } else {
            annotation = createAnnotation(fromAnnotation);
        }
        return annotation;
    }

    private Annotation updateAnnotation(final Annotation existingAnnotation,
                                        final AnnotationDTO fromAnnotation) {
        final Date timeStamp = new Date(System.currentTimeMillis());

        mapPrefLabel(fromAnnotation, existingAnnotation);
        mapDescription(fromAnnotation, existingAnnotation);
        existingAnnotation.setModified(timeStamp);

        return existingAnnotation;
    }

    private Annotation createAnnotation(final AnnotationDTO fromAnnotation) {
        final Date timeStamp = new Date(System.currentTimeMillis());
        final Annotation annotation = new Annotation();
        if (fromAnnotation.getId() != null) {
            annotation.setId(fromAnnotation.getId());
        } else {
            final UUID uuid = UUID.randomUUID();
            annotation.setId(uuid);
        }
        final String codeValue = fromAnnotation.getCodeValue();
        validateCodeValue(codeValue);
        annotation.setCodeValue(codeValue);
        mapPrefLabel(fromAnnotation, annotation);
        mapDescription(fromAnnotation, annotation);
        annotation.setCreated(timeStamp);
        annotation.setModified(timeStamp);
        return annotation;
    }

    private void validateAnnotationCodeValueForExistingAnnotation(final AnnotationDTO annotation) {
        if (annotation.getId() != null) {
            final Annotation existingAnnotation = annotationRepository.findById(annotation.getId());
            if (existingAnnotation != null && !existingAnnotation.getCodeValue().equalsIgnoreCase(annotation.getCodeValue())) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_EXISTING_CODE_MISMATCH));
            }
        }
    }

    private void mapPrefLabel(final AnnotationDTO fromAnnotation,
                              final Annotation annotation) {

        final Map<String, String> prefLabel = validateLanguagesForLocalizable(fromAnnotation.getPrefLabel());
        annotation.setPrefLabel(prefLabel);
    }

    private void mapDescription(final AnnotationDTO fromAnnotation,
                                final Annotation annotation) {
        final Map<String, String> description = validateLanguagesForLocalizable(fromAnnotation.getDescription());
        annotation.setDescription(description);
    }
}


