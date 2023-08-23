package fi.vm.yti.codelist.intake.dao.impl;

import java.util.*;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.CodeSchemeAnnotationDTO;
import fi.vm.yti.codelist.common.dto.ErrorModel;
import fi.vm.yti.codelist.intake.dao.CodeSchemeAnnotationDao;
import fi.vm.yti.codelist.intake.exception.YtiCodeListException;
import fi.vm.yti.codelist.intake.jpa.CodeSchemeAnnotationRepository;
import fi.vm.yti.codelist.intake.language.LanguageService;
import fi.vm.yti.codelist.intake.log.EntityChangeLogger;
import fi.vm.yti.codelist.intake.model.CodeSchemeAnnotation;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.*;
import static fi.vm.yti.codelist.intake.parser.impl.AbstractBaseParser.validateCodeValue;

@Component
public class CodeSchemeAnnotationDaoImpl extends AbstractDao implements CodeSchemeAnnotationDao {

    private final EntityChangeLogger entityChangeLogger;
    private final CodeSchemeAnnotationRepository codeSchemeAnnotationRepository;
    private final LanguageService languageService;

    @Inject
    public CodeSchemeAnnotationDaoImpl(final EntityChangeLogger entityChangeLogger,
                             final CodeSchemeAnnotationRepository codeSchemeAnnotationRepository,
                             final LanguageService languageService) {
        super(languageService);
        this.entityChangeLogger = entityChangeLogger;
        this.codeSchemeAnnotationRepository = codeSchemeAnnotationRepository;
        this.languageService = languageService;
    }

    @Transactional
    public void delete(final CodeSchemeAnnotation codeSchemeAnnotation) {
        codeSchemeAnnotationRepository.delete(codeSchemeAnnotation);
    }

    @Transactional
    public void save(final CodeSchemeAnnotation codeSchemeAnnotation) {
        codeSchemeAnnotationRepository.save(codeSchemeAnnotation);
    }

    @Transactional
    public void save(final Set<CodeSchemeAnnotation> codeSchemeAnnotations) {
        codeSchemeAnnotationRepository.saveAll(codeSchemeAnnotations);
    }

    @Transactional
    public CodeSchemeAnnotation findByCodeschemeIdAndAnnotationId(final UUID codeschemeId, final UUID annotationId) {
        return codeSchemeAnnotationRepository.findByCodeschemeIdAndAnnotationId(codeschemeId, annotationId);
    }

    @Transactional
    public CodeSchemeAnnotation findByCodeschemeIdAndAnnotationIdAndLanguage(final UUID codeschemeId, final UUID annotationId, final String language) {
        return codeSchemeAnnotationRepository.findByCodeschemeIdAndAnnotationIdAndLanguage(codeschemeId, annotationId, language);
    }

    @Transactional
    public Set<CodeSchemeAnnotation> findByCodeschemeId(final UUID codeschemeId) {
        return codeSchemeAnnotationRepository.findByCodeschemeId(codeschemeId);
    }

    @Transactional
    public Set<CodeSchemeAnnotation> findAll() {
        return codeSchemeAnnotationRepository.findAllByOrderByCodeschemeIdAscAnnotationIdAscLanguageAsc();
    }
/* 
    @Transactional
    public CodeSchemeAnnotation updateCodeSchemeAnnotationFromDto(final CodeSchemeAnnotationDTO codeSchemeAnnotationDto) {
        final CodeSchemeAnnotation codeSchemeAnnotation = createOrUpdateCodeSchemeAnnotation(codeSchemeAnnotationDto);
        codeSchemeAnnotationRepository.save(codeSchemeAnnotation);
        return codeSchemeAnnotation;
    }

    @Transactional
    public Set<CodeSchemeAnnotation> updateCodeSchemeAnnotationsFromDto(final Set<CodeSchemeAnnotationDTO> codeSchemeAnnotationDtos) {
        final Set<CodeSchemeAnnotation> addedOrUpdatedCodeSchemeAnnotations = new HashSet<>();
        for (final CodeSchemeAnnotationDTO codeSchemeAnnotationDto : codeSchemeAnnotationDtos) {
            final CodeSchemeAnnotation codeSchemeAnnotation = createOrUpdateCodeSchemeAnnotation(codeSchemeAnnotationDto);
            codeSchemeAnnotationRepository.save(codeSchemeAnnotation);
            addedOrUpdatedCodeSchemeAnnotations.add(codeSchemeAnnotation);
        }
        if (!addedOrUpdatedCodeSchemeAnnotations.isEmpty()) {
            save(addedOrUpdatedCodeSchemeAnnotations);
        }
        return addedOrUpdatedCodeSchemeAnnotations;
    }

    @Transactional
    public CodeSchemeAnnotation createOrUpdateCodeSchemeAnnotation(final CodeSchemeAnnotationDTO fromCodeSchemeAnnotation) {

        final CodeSchemeAnnotation existingCodeSchemeAnnotation;
        existingCodeSchemeAnnotation = codeSchemeAnnotationRepository.findByCodeschemeIdAndAnnotationId(fromCodeSchemeAnnotation.getCodeschemeId(), fromCodeSchemeAnnotation.getAnnotationId());

        final CodeSchemeAnnotation codeSchemeAnnotation;
        if (existingCodeSchemeAnnotation != null) {
            validateCodeSchemeAnnotationCodeValueForExistingCodeSchemeAnnotation(fromCodeSchemeAnnotation);
            codeSchemeAnnotation = updateCodeSchemeAnnotation(existingCodeSchemeAnnotation, fromCodeSchemeAnnotation);
        } else {
            codeSchemeAnnotation = createCodeSchemeAnnotation(fromCodeSchemeAnnotation);
        }
        return codeSchemeAnnotation;
    }

    private CodeSchemeAnnotation updateCodeSchemeAnnotation(final CodeSchemeAnnotation existingCodeSchemeAnnotation,
                                        final CodeSchemeAnnotationDTO fromCodeSchemeAnnotation) {
        final Date timeStamp = new Date(System.currentTimeMillis());

        mapPrefLabel(fromCodeSchemeAnnotation, existingCodeSchemeAnnotation);
        mapDescription(fromCodeSchemeAnnotation, existingCodeSchemeAnnotation);
        existingCodeSchemeAnnotation.setModified(timeStamp);

        return existingCodeSchemeAnnotation;
    }

    private CodeSchemeAnnotation createCodeSchemeAnnotation(final CodeSchemeAnnotationDTO fromCodeSchemeAnnotation) {
        final Date timeStamp = new Date(System.currentTimeMillis());
        final CodeSchemeAnnotation codeSchemeAnnotation = new CodeSchemeAnnotation();
        if (fromCodeSchemeAnnotation.getId() != null) {
            codeSchemeAnnotation.setId(fromCodeSchemeAnnotation.getId());
        } else {
            final UUID uuid = UUID.randomUUID();
            codeSchemeAnnotation.setId(uuid);
        }
        final String codeValue = fromCodeSchemeAnnotation.getCodeValue();
        validateCodeValue(codeValue);
        codeSchemeAnnotation.setCodeValue(codeValue);
        mapPrefLabel(fromCodeSchemeAnnotation, codeSchemeAnnotation);
        mapDescription(fromCodeSchemeAnnotation, codeSchemeAnnotation);
        codeSchemeAnnotation.setCreated(timeStamp);
        codeSchemeAnnotation.setModified(timeStamp);
        return codeSchemeAnnotation;
    }

    private void validateCodeSchemeAnnotationCodeValueForExistingCodeSchemeAnnotation(final CodeSchemeAnnotationDTO codeSchemeAnnotation) {
        if (codeSchemeAnnotation.getId() != null) {
            final CodeSchemeAnnotation existingCodeSchemeAnnotation = codeSchemeAnnotationRepository.findById(codeSchemeAnnotation.getId());
            if (existingCodeSchemeAnnotation != null && !existingCodeSchemeAnnotation.getCodeValue().equalsIgnoreCase(codeSchemeAnnotation.getCodeValue())) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_EXISTING_CODE_MISMATCH));
            }
        }
    }

    private void mapPrefLabel(final CodeSchemeAnnotationDTO fromCodeSchemeAnnotation,
                              final CodeSchemeAnnotation codeSchemeAnnotation) {

        final Map<String, String> prefLabel = validateLanguagesForLocalizable(fromCodeSchemeAnnotation.getPrefLabel());
        codeSchemeAnnotation.setPrefLabel(prefLabel);
    }

    private void mapDescription(final CodeSchemeAnnotationDTO fromCodeSchemeAnnotation,
                                final CodeSchemeAnnotation codeSchemeAnnotation) {
        final Map<String, String> description = validateLanguagesForLocalizable(fromCodeSchemeAnnotation.getDescription());
        codeSchemeAnnotation.setDescription(description);
    } */
}


