package fi.vm.yti.codelist.intake.dao.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import fi.vm.yti.codelist.common.dto.CodeDTO;
import fi.vm.yti.codelist.common.dto.ErrorModel;
import fi.vm.yti.codelist.common.dto.ExtensionDTO;
import fi.vm.yti.codelist.intake.configuration.UriSuomiProperties;
import fi.vm.yti.codelist.intake.dao.CodeDao;
import fi.vm.yti.codelist.intake.dao.ExtensionDao;
import fi.vm.yti.codelist.intake.dao.ExtensionSchemeDao;
import fi.vm.yti.codelist.intake.exception.YtiCodeListException;
import fi.vm.yti.codelist.intake.jpa.ExtensionRepository;
import fi.vm.yti.codelist.intake.log.EntityChangeLogger;
import fi.vm.yti.codelist.intake.model.Code;
import fi.vm.yti.codelist.intake.model.CodeScheme;
import fi.vm.yti.codelist.intake.model.Extension;
import fi.vm.yti.codelist.intake.model.ExtensionScheme;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.*;

@Component
public class ExtensionDaoImpl implements ExtensionDao {

    private static final int MAX_LEVEL = 10;
    private final EntityChangeLogger entityChangeLogger;
    private final ExtensionRepository extensionRepository;
    private final CodeDao codeDao;
    private final ExtensionSchemeDao extensionSchemeDao;
    private final UriSuomiProperties uriSuomiProperties;

    @Inject
    public ExtensionDaoImpl(final EntityChangeLogger entityChangeLogger,
                            final ExtensionRepository extensionRepository,
                            final CodeDao codeDao,
                            final ExtensionSchemeDao extensionSchemeDao,
                            final UriSuomiProperties uriSuomiProperties) {
        this.entityChangeLogger = entityChangeLogger;
        this.extensionRepository = extensionRepository;
        this.codeDao = codeDao;
        this.extensionSchemeDao = extensionSchemeDao;
        this.uriSuomiProperties = uriSuomiProperties;
    }

    public void delete(final Extension extension) {
        entityChangeLogger.logExtensionChange(extension);
        extensionRepository.delete(extension);
    }

    public void delete(final Set<Extension> extensions) {
        extensions.forEach(entityChangeLogger::logExtensionChange);
        extensionRepository.delete(extensions);
    }

    public void save(final Extension extension) {
        extensionRepository.save(extension);
        entityChangeLogger.logExtensionChange(extension);
    }

    public void save(final Set<Extension> extensions) {
        extensionRepository.save(extensions);
        extensions.forEach(entityChangeLogger::logExtensionChange);
    }

    public Set<Extension> findAll() {
        return extensionRepository.findAll();
    }

    public Extension findById(final UUID id) {
        return extensionRepository.findById(id);
    }

    public Set<Extension> findByCodeId(final UUID id) {
        return extensionRepository.findByCodeId(id);
    }

    public Set<Extension> findByExtensionId(final UUID id) {
        return extensionRepository.findByExtensionId(id);
    }

    public Set<Extension> findByExtensionSchemeId(final UUID id) {
        return extensionRepository.findByExtensionSchemeId(id);
    }

    @Transactional
    public Extension updateExtensionEntityFromDto(final ExtensionScheme extensionScheme,
                                                  final ExtensionDTO fromExtensionDto) {
        final Extension extension = createOrUpdateExtension(extensionScheme, fromExtensionDto);
        fromExtensionDto.setId(extension.getId());
        save(extension);
        resolveExtensionRelation(extensionScheme, extension, fromExtensionDto);
        return extension;
    }

    @Transactional
    public Set<Extension> updateExtensionEntitiesFromDtos(final ExtensionScheme extensionScheme,
                                                          final Set<ExtensionDTO> extensionDtos) {
        final Set<Extension> extensions = new HashSet<>();
        if (extensionDtos != null) {
            for (final ExtensionDTO extensionDto : extensionDtos) {
                final Extension extension = createOrUpdateExtension(extensionScheme, extensionDto);
                extensionDto.setId(extension.getId());
                extensions.add(extension);
                save(extension);
            }
            resolveExtensionRelations(extensionScheme, extensionDtos);
        }
        return extensions;
    }

    private UUID getUuidFromString(final String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private void checkDuplicateCode(final Set<Extension> extensions,
                                    final String identifier) {
        boolean found = false;
        for (final Extension extension : extensions) {
            final Code code = extension.getCode();
            if (code == null) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSION_CODE_NOT_FOUND));
            }
            if ((identifier.startsWith(uriSuomiProperties.getUriSuomiAddress()) && code.getUri().equalsIgnoreCase(identifier)) || code.getCodeValue().equalsIgnoreCase(identifier)) {
                if (found) {
                    throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSIONS_HAVE_DUPLICATE_CODE_USE_UUID));
                }
                found = true;
            }
        }
    }

    private void linkExtensionWithId(final Extension extension,
                                     final UUID id) {
        final Extension relatedExtension = findById(id);
        linkExtensions(extension, relatedExtension);
    }

    private void linkExtensions(final Extension extension,
                                final Extension relatedExtension) {
        if (relatedExtension != null) {
            extension.setExtension(relatedExtension);
        }
    }

    private void resolveExtensionRelation(final ExtensionScheme extensionScheme,
                                          final Extension extension,
                                          final ExtensionDTO fromExtension) {
        final ExtensionDTO relatedExtension = fromExtension.getExtension();
        if (relatedExtension != null && relatedExtension.getId() != null && fromExtension.getId() != null && extension.getId().equals(relatedExtension.getId())) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_406));
        }
        final Set<Extension> linkedExtensions = new HashSet<>();
        if (relatedExtension != null && relatedExtension.getId() != null) {
            linkExtensionWithId(extension, relatedExtension.getId());
            linkedExtensions.add(extension);
        } else if (relatedExtension != null && relatedExtension.getCode() != null) {
            final Set<Extension> extensions = findByExtensionSchemeId(extensionScheme.getId());
            final String identifier = relatedExtension.getCode().getCodeValue();
            final UUID uuid = getUuidFromString(identifier);
            if (uuid != null) {
                linkExtensionWithId(extension, uuid);
                linkedExtensions.add(extension);
            }
            for (final Extension extensionSchemeExtension : extensions) {
                if ((identifier.startsWith(uriSuomiProperties.getUriSuomiAddress()) && extensionSchemeExtension.getCode() != null && extensionSchemeExtension.getCode().getUri().equalsIgnoreCase(identifier)) ||
                    (extensionSchemeExtension.getCode() != null && extensionSchemeExtension.getCode().getCodeValue().equalsIgnoreCase(identifier))) {
                    checkDuplicateCode(extensions, identifier);
                    linkExtensions(extension, extensionSchemeExtension);
                    linkedExtensions.add(extension);
                }
            }
        }
        linkedExtensions.forEach(linkedExtension -> checkExtensionHierarchyLevels(linkedExtension, 1));
        save(linkedExtensions);
    }

    private void checkExtensionHierarchyLevels(final Extension extension,
                                               final int level) {
        if (level > MAX_LEVEL) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSION_HIERARCHY_MAXLEVEL_REACHED));
        }
        if (extension.getExtension() != null) {
            checkExtensionHierarchyLevels(extension.getExtension(), level + 1);
        }
    }

    private void resolveExtensionRelations(final ExtensionScheme extensionScheme,
                                           final Set<ExtensionDTO> fromExtensions) {
        fromExtensions.forEach(fromExtension -> {
            final Extension extension = findById(fromExtension.getId());
            resolveExtensionRelation(extensionScheme, extension, fromExtension);
        });
    }

    @Transactional
    public Extension createOrUpdateExtension(final ExtensionScheme extensionSchemeDto,
                                             final ExtensionDTO fromExtension) {
        final Extension existingExtension;
        final ExtensionScheme extensionScheme = extensionSchemeDao.findById(extensionSchemeDto.getId());
        if (extensionScheme != null) {
            if (fromExtension.getId() != null) {
                existingExtension = extensionRepository.findByExtensionSchemeAndId(extensionScheme, fromExtension.getId());
            } else {
                existingExtension = null;
            }
            final Extension extension;
            if (existingExtension != null) {
                extension = updateExtension(extensionScheme.getParentCodeScheme(), extensionScheme, existingExtension, fromExtension);
            } else {
                extension = createExtension(extensionScheme.getParentCodeScheme(), extensionScheme, fromExtension);
            }
            return extension;
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_406));
        }
    }

    private Extension updateExtension(final CodeScheme codeScheme,
                                      final ExtensionScheme extensionScheme,
                                      final Extension existingExtension,
                                      final ExtensionDTO fromExtension) {
        final String extensionValue = fromExtension.getExtensionValue();
        validateExtensionValue(extensionValue);
        if (!Objects.equals(existingExtension.getExtensionValue(), extensionValue)) {
            existingExtension.setExtensionValue(extensionValue);
        }
        for (final Map.Entry<String, String> entry : fromExtension.getPrefLabel().entrySet()) {
            final String language = entry.getKey();
            final String value = entry.getValue();
            if (!Objects.equals(existingExtension.getPrefLabel(language), value)) {
                existingExtension.setPrefLabel(language, value);
            }
        }
        if (fromExtension.getOrder() != null && !Objects.equals(existingExtension.getOrder(), fromExtension.getOrder())) {
            checkOrderIsNotInUse(extensionScheme, fromExtension.getOrder());
            existingExtension.setOrder(fromExtension.getOrder());
        } else if (existingExtension.getOrder() == null && fromExtension.getOrder() == null) {
            existingExtension.setOrder(getNextOrderInSequence(extensionScheme));
        }
        setRelatedExtension(fromExtension, existingExtension);
        if (fromExtension.getCode() != null) {
            final Code code = findCodeUsingCodeValueOrUri(codeScheme, extensionScheme, fromExtension);
            if (!Objects.equals(existingExtension.getCode(), code)) {
                existingExtension.setCode(code);
            }
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_406));
        }
        existingExtension.setModified(new Date(System.currentTimeMillis()));
        return existingExtension;
    }

    private Extension createExtension(final CodeScheme codeScheme,
                                      final ExtensionScheme extensionScheme,
                                      final ExtensionDTO fromExtension) {
        final Extension extension = new Extension();
        if (fromExtension.getId() != null) {
            extension.setId(fromExtension.getId());
        } else {
            final UUID uuid = UUID.randomUUID();
            extension.setId(uuid);
        }
        final String extensionValue = fromExtension.getExtensionValue();
        validateExtensionValue(extensionValue);
        extension.setExtensionValue(extensionValue);
        for (final Map.Entry<String, String> entry : fromExtension.getPrefLabel().entrySet()) {
            extension.setPrefLabel(entry.getKey(), entry.getValue());
        }
        if (fromExtension.getOrder() != null) {
            checkOrderIsNotInUse(extensionScheme, fromExtension.getOrder());
            extension.setOrder(fromExtension.getOrder());
        } else {
            extension.setOrder(getNextOrderInSequence(extensionScheme));
        }
        if (fromExtension.getCode() != null) {
            final Code code = findCodeUsingCodeValueOrUri(codeScheme, extensionScheme, fromExtension);
            extension.setCode(code);
        }
        setRelatedExtension(fromExtension, extension);
        extension.setExtensionScheme(extensionScheme);
        final Date timeStamp = new Date(System.currentTimeMillis());
        extension.setCreated(timeStamp);
        extension.setModified(timeStamp);
        return extension;
    }

    private void validateExtensionValue(final String extensionValue) {
        if (extensionValue == null || extensionValue.isEmpty()) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSIONVALUE_NOT_SET));
        }
    }

    private void setRelatedExtension(final ExtensionDTO fromExtension,
                                     final Extension extension) {
        if (fromExtension.getExtension() != null && fromExtension.getExtension().getId() != null) {
            final UUID relatedExtensionId = fromExtension.getExtension().getId();
            if (relatedExtensionId != null && relatedExtensionId == fromExtension.getId()) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_406));
            }
            if (relatedExtensionId != null) {
                final Extension relatedExtension = findById(relatedExtensionId);
                if (relatedExtension != null) {
                    extension.setExtension(extension);
                } else {
                    throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_406));
                }
            }
        }
    }

    private Code findCodeUsingCodeValueOrUri(final CodeScheme codeScheme,
                                             final ExtensionScheme extensionScheme,
                                             final ExtensionDTO extension) {
        final CodeDTO fromCode = extension.getCode();
        final Code code;
        if (fromCode != null && fromCode.getUri() != null && !fromCode.getUri().isEmpty()) {
            code = codeDao.findByUri(fromCode.getUri());
            if (code != null) {
                checkThatCodeIsInAllowedCodeScheme(code.getCodeScheme(), codeScheme, extensionScheme);
            } else {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSION_CODE_NOT_FOUND));
            }
        } else if (fromCode != null && codeScheme != null && fromCode.getCodeValue() != null && !fromCode.getCodeValue().isEmpty()) {
            code = codeDao.findByCodeSchemeAndCodeValue(codeScheme, extension.getCode().getCodeValue());
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSION_CODE_NOT_FOUND));
        }
        if (code == null) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSION_CODE_NOT_FOUND));
        }
        return code;
    }

    private void checkThatCodeIsInAllowedCodeScheme(final CodeScheme codeSchemeForCode,
                                                    final CodeScheme parentCodeScheme,
                                                    final ExtensionScheme extensionScheme) {
        final Set<CodeScheme> codeSchemes = extensionScheme.getCodeSchemes();
        if (codeSchemeForCode == parentCodeScheme || (codeSchemes != null && codeSchemes.contains(codeSchemeForCode))) {
            return;
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSION_CODE_NOT_ALLOWED));
        }
    }

    private void checkOrderIsNotInUse(final ExtensionScheme extensionScheme,
                                      final Integer order) {
        final Extension extension = extensionRepository.findByExtensionSchemeAndOrder(extensionScheme, order);
        if (extension != null) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_CODE_ORDER_ALREADY_IN_USE));
        }
    }

    private Integer getNextOrderInSequence(final ExtensionScheme extensionScheme) {
        final Integer maxOrder = extensionRepository.getExtensionMaxOrder(extensionScheme.getId());
        if (maxOrder == null) {
            return 1;
        } else {
            return maxOrder + 1;
        }
    }
}
