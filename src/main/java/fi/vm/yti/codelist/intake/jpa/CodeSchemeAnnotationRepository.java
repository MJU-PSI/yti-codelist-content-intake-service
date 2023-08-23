package fi.vm.yti.codelist.intake.jpa;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.intake.model.CodeSchemeAnnotation;

@Repository
@Transactional
public interface CodeSchemeAnnotationRepository extends CrudRepository<CodeSchemeAnnotation, String> {

    CodeSchemeAnnotation findByCodeschemeIdAndAnnotationId(final UUID codeschemeId, final UUID annotationId);

    CodeSchemeAnnotation findByCodeschemeIdAndAnnotationIdAndLanguage(final UUID codeschemeId, final UUID annotationId, final String language);

    Set<CodeSchemeAnnotation> findAll();

    Set<CodeSchemeAnnotation> findAllByOrderByCodeschemeIdAscAnnotationIdAscLanguageAsc();

    Set<CodeSchemeAnnotation> findByCodeschemeId(final UUID codeschemeId);

}
