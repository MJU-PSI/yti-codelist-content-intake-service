package fi.vm.yti.codelist.intake.service.impl;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.CodeSchemeAnnotationDTO;
import fi.vm.yti.codelist.intake.dao.CodeSchemeAnnotationDao;
import fi.vm.yti.codelist.intake.model.CodeSchemeAnnotation;
import fi.vm.yti.codelist.intake.service.CodeSchemeAnnotationService;

@Singleton
@Service
public class CodeSchemeAnnotationServiceImpl implements CodeSchemeAnnotationService, AbstractBaseService {

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

}
