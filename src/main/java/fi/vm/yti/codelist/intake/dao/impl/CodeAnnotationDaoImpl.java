package fi.vm.yti.codelist.intake.dao.impl;

import java.util.*;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.intake.dao.CodeAnnotationDao;
import fi.vm.yti.codelist.intake.jpa.CodeAnnotationRepository;
import fi.vm.yti.codelist.intake.language.LanguageService;
import fi.vm.yti.codelist.intake.log.EntityChangeLogger;
import fi.vm.yti.codelist.intake.model.CodeAnnotation;

@Component
public class CodeAnnotationDaoImpl extends AbstractDao implements CodeAnnotationDao {

    private final EntityChangeLogger entityChangeLogger;
    private final CodeAnnotationRepository codeAnnotationRepository;
    private final LanguageService languageService;

    @Inject
    public CodeAnnotationDaoImpl(final EntityChangeLogger entityChangeLogger,
                             final CodeAnnotationRepository codeAnnotationRepository,
                             final LanguageService languageService) {
        super(languageService);
        this.entityChangeLogger = entityChangeLogger;
        this.codeAnnotationRepository = codeAnnotationRepository;
        this.languageService = languageService;
    }

    @Transactional
    public void delete(final CodeAnnotation codeAnnotation) {
        codeAnnotationRepository.delete(codeAnnotation);
    }

    @Transactional
    public void save(final CodeAnnotation codeAnnotation) {
        codeAnnotationRepository.save(codeAnnotation);
    }

    @Transactional
    public void save(final Set<CodeAnnotation> codeAnnotations) {
        codeAnnotationRepository.saveAll(codeAnnotations);
    }

    @Transactional
    public CodeAnnotation findByCodeIdAndAnnotationId(final UUID codeId, final UUID annotationId) {
        return codeAnnotationRepository.findByCodeIdAndAnnotationId(codeId, annotationId);
    }

    @Transactional
    public CodeAnnotation findByCodeIdAndAnnotationIdAndLanguage(final UUID codeId, final UUID annotationId, final String language) {
        return codeAnnotationRepository.findByCodeIdAndAnnotationIdAndLanguage(codeId, annotationId, language);
    }

    @Transactional
    public Set<CodeAnnotation> findByCodeId(final UUID codeId) {
        return codeAnnotationRepository.findByCodeId(codeId);
    }

    @Transactional
    public Set<CodeAnnotation> findAll() {
        return codeAnnotationRepository.findAllByOrderByCodeIdAscAnnotationIdAscLanguageAsc();
    }

}


