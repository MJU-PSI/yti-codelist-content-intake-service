package fi.vm.yti.codelist.intake.dao;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.common.dto.AnnotationDTO;
import fi.vm.yti.codelist.intake.model.Annotation;

public interface AnnotationDao {

    void delete(final Annotation annotation);

    void save(final Annotation annotation);

    void save(final Set<Annotation> annotations);

    Annotation findById(final UUID id);

    Annotation findByCodeValue(final String codeValue);

    Set<Annotation> findAll();

    Annotation updateAnnotationFromDto(final AnnotationDTO annotationDto);

    Set<Annotation> updateAnnotationsFromDto(final Set<AnnotationDTO> annotationDtos);
/* 
    Set<Annotation> findByCodeRegistryCodeValue(final String codeRegistryCodeValue);

    Annotation findByCodeRegistryAndCodeValue(final CodeRegistry codeRegistry,
                                              final String codeValue);

    Annotation findByCodeRegistryCodeValueAndCodeValue(final String codeRegistryCodeValue,
                                                       final String annotationCodeValue);



    Annotation updateAnnotationFromDto(final CodeRegistry codeRegistry,
                                       final AnnotationDTO annotationDto);

    Set<Annotation> updateAnnotationsFromDtos(final boolean isAuthorized,
                                              final CodeRegistry codeRegistry,
                                              final Set<AnnotationDTO> annotationDtos,
                                              final boolean updateExternalReferences);
 */
    // void updateContentModified(final UUID annotationId,
    //                            final Date timeStamp);
}
