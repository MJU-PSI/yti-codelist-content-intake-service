package fi.vm.yti.codelist.intake.service.impl;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.CodeSchemeAnnotationDTO;
import fi.vm.yti.codelist.intake.dao.CodeSchemeAnnotationDao;
import fi.vm.yti.codelist.intake.model.CodeSchemeAnnotation;
import fi.vm.yti.codelist.intake.service.CodeSchemeAnnotationService;

@Singleton
@Service
public class CodeSchemeAnnotationServiceImpl implements CodeSchemeAnnotationService, AbstractBaseService {

    // private static final Logger LOG = LoggerFactory.getLogger(CodeSchemeAnnotationServiceImpl.class);

    private final CodeSchemeAnnotationDao codeSchemeAnnotationDao;
    private final DtoMapperService dtoMapperService;

    @Inject
    public CodeSchemeAnnotationServiceImpl(final CodeSchemeAnnotationDao codeSchemeAnnotationDao,
                                 final DtoMapperService dtoMapperService) {
        this.codeSchemeAnnotationDao = codeSchemeAnnotationDao;
        this.dtoMapperService = dtoMapperService;
    }

    @Transactional
    public Set<CodeSchemeAnnotationDTO> findAll() {
        return dtoMapperService.mapDeepCodeSchemeAnnotationDtos(codeSchemeAnnotationDao.findAll());
    }

    @Transactional
    public Set<CodeSchemeAnnotationDTO> findByCodeschemeId(final UUID codeschemeId) {
        final Set<CodeSchemeAnnotation> codeSchemeAnnotations = codeSchemeAnnotationDao.findByCodeschemeId(codeschemeId);
        if (codeSchemeAnnotations != null) {
            return dtoMapperService.mapDeepCodeSchemeAnnotationDtos(codeSchemeAnnotations);
        }
        return null;
    }

    // @Transactional
    // public CodeSchemeAnnotationDTO findByCodeschemeIdAndAnnotationId(final UUID codeschemeId, final UUID annotationId) {
    //     final CodeSchemeAnnotation codeSchemeAnnotation = codeSchemeAnnotationDao.findByCodeschemeIdAndAnnotationIdAndLanguage(codeschemeId, annotationId, language);
    //     if (codeSchemeAnnotation != null) {
    //         return dtoMapperService.mapDeepCodeSchemeAnnotationDto(codeSchemeAnnotation);
    //     }
    //     return null;
    // }

    // @Transactional
    // public CodeSchemeAnnotationDTO deleteCodeSchemeAnnotation(final String codeSchemeAnnotationCodeValue) {

    //     return null;
    // }

    // @Transactional
    // public CodeSchemeAnnotationDTO updateCodeSchemeAnnotationFromDto(final CodeSchemeAnnotationDTO codeSchemeAnnotationDto) {
    //     return null;
    // }

    // @Transactional
    // public Set<CodeSchemeAnnotationDTO> parseAndPersistCodeSchemeAnnotationFromJson(final String jsonPayload) {
    //     CodeSchemeAnnotation codeSchemeAnnotation;
    //     if (jsonPayload != null && !jsonPayload.isEmpty()) {
    //         final CodeSchemeAnnotationDTO codeSchemeAnnotationDto = codeSchemeAnnotationParser.parseCodeSchemeAnnotationFromJson(jsonPayload);
    //         codeSchemeAnnotation = codeSchemeAnnotationDao.updateCodeSchemeAnnotationFromDto(codeSchemeAnnotationDto);
    //     } else {
    //         throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_JSON_PAYLOAD_EMPTY));
    //     }
    //     if (codeSchemeAnnotation == null) {
    //         throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_ANNOTATIONS_ARE_EMPTY));
    //     }
    //     Set<CodeSchemeAnnotation> codeSchemeAnnotations = new HashSet<>();
    //     codeSchemeAnnotations.add(codeSchemeAnnotation);
    //     return dtoMapperService.mapDeepCodeSchemeAnnotationDtos(codeSchemeAnnotations);
    // }

    // @Transactional
    // public Set<CodeSchemeAnnotationDTO> parseAndPersistCodeSchemeAnnotationsSourceData(final String jsonPayload) {
    //     final Set<CodeSchemeAnnotation> codeSchemeAnnotations;
    //     if (jsonPayload != null && !jsonPayload.isEmpty()) {
    //         final Set<CodeSchemeAnnotationDTO> codeSchemeAnnotationsDto = codeSchemeAnnotationParser.parseCodeSchemeAnnotationsFromJson(jsonPayload);
    //         codeSchemeAnnotations = codeSchemeAnnotationDao.updateCodeSchemeAnnotationsFromDto(codeSchemeAnnotationsDto);
    //     } else {
    //         throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_JSON_PAYLOAD_EMPTY));
    //     }
    //     if (codeSchemeAnnotations == null) {
    //         throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_ANNOTATIONS_ARE_EMPTY));
    //     }
    //     return dtoMapperService.mapDeepCodeSchemeAnnotationDtos(codeSchemeAnnotations);
    // }
}
