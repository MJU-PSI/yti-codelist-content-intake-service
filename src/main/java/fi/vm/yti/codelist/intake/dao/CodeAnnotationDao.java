package fi.vm.yti.codelist.intake.dao;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.intake.model.CodeAnnotation;

public interface CodeAnnotationDao {

    void delete(final CodeAnnotation annotation);

    void save(final CodeAnnotation annotation);

    void save(final Set<CodeAnnotation> annotations);

    Set<CodeAnnotation> findByCodeId(final UUID codeId);

    CodeAnnotation findByCodeIdAndAnnotationId(final UUID codeId, final UUID annotationId);

    CodeAnnotation findByCodeIdAndAnnotationIdAndLanguage(final UUID codeId, final UUID annotationId, final String language);

    Set<CodeAnnotation> findAll();

}
