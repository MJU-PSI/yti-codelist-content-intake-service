package fi.vm.yti.codelist.intake.jpa;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.intake.model.Annotation;

@Repository
@Transactional
public interface AnnotationRepository extends CrudRepository<Annotation, String> {

    Annotation findByCodeValueIgnoreCase(final String codeValue);

    Annotation findById(final UUID id);

    Set<Annotation> findAll();

}
