package fi.vm.yti.codelist.intake.service.impl;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.CodeAnnotationDTO;
import fi.vm.yti.codelist.intake.dao.CodeAnnotationDao;
import fi.vm.yti.codelist.intake.model.CodeAnnotation;
import fi.vm.yti.codelist.intake.service.CodeAnnotationService;

@Singleton
@Service
public class CodeAnnotationServiceImpl implements CodeAnnotationService, AbstractBaseService {

    private final CodeAnnotationDao codeAnnotationDao;
    private final DtoMapperService dtoMapperService;

    @Inject
    public CodeAnnotationServiceImpl(final CodeAnnotationDao codeAnnotationDao,
                                 final DtoMapperService dtoMapperService) {
        this.codeAnnotationDao = codeAnnotationDao;
        this.dtoMapperService = dtoMapperService;
    }

    @Transactional
    public Set<CodeAnnotationDTO> findAll() {
        return dtoMapperService.mapDeepCodeAnnotationDtos(codeAnnotationDao.findAll());
    }

    @Transactional
    public Set<CodeAnnotationDTO> findByCodeId(final UUID codeId) {
        final Set<CodeAnnotation> codeAnnotations = codeAnnotationDao.findByCodeId(codeId);
        if (codeAnnotations != null) {
            return dtoMapperService.mapDeepCodeAnnotationDtos(codeAnnotations);
        }
        return null;
    }

}
