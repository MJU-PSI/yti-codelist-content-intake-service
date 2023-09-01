package fi.vm.yti.codelist.intake.dao.impl;

import java.util.*;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.intake.dao.CodeSchemeAnnotationDao;
import fi.vm.yti.codelist.intake.jpa.CodeSchemeAnnotationRepository;
import fi.vm.yti.codelist.intake.language.LanguageService;
import fi.vm.yti.codelist.intake.log.EntityChangeLogger;
import fi.vm.yti.codelist.intake.model.CodeSchemeAnnotation;

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

}


