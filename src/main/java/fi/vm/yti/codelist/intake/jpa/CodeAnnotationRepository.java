package fi.vm.yti.codelist.intake.jpa;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.intake.model.CodeAnnotation;

@Repository
@Transactional
public interface CodeAnnotationRepository extends CrudRepository<CodeAnnotation, String> {

    CodeAnnotation findByCodeIdAndAnnotationId(final UUID codeId, final UUID annotationId);

    CodeAnnotation findByCodeIdAndAnnotationIdAndLanguage(final UUID codeId, final UUID annotationId, final String language);

    Set<CodeAnnotation> findAll();

    Set<CodeAnnotation> findAllByOrderByCodeIdAscAnnotationIdAscLanguageAsc();

    Set<CodeAnnotation> findByCodeId(final UUID codeId);

}
