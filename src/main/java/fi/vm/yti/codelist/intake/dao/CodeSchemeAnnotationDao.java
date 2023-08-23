package fi.vm.yti.codelist.intake.dao;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.intake.model.CodeSchemeAnnotation;

public interface CodeSchemeAnnotationDao {

    void delete(final CodeSchemeAnnotation annotation);

    void save(final CodeSchemeAnnotation annotation);

    void save(final Set<CodeSchemeAnnotation> annotations);

    Set<CodeSchemeAnnotation> findByCodeschemeId(final UUID codeschemeId);

    CodeSchemeAnnotation findByCodeschemeIdAndAnnotationId(final UUID codeschemeId, final UUID annotationId);

    CodeSchemeAnnotation findByCodeschemeIdAndAnnotationIdAndLanguage(final UUID codeschemeId, final UUID annotationId, final String language);

    Set<CodeSchemeAnnotation> findAll();

}
