package fi.vm.yti.codelist.intake.dao.impl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.ValueTypeDTO;
import fi.vm.yti.codelist.intake.dao.ValueTypeDao;
import fi.vm.yti.codelist.intake.jpa.ValueTypeRepository;
import fi.vm.yti.codelist.intake.language.LanguageService;
import fi.vm.yti.codelist.intake.log.EntityChangeLogger;
import fi.vm.yti.codelist.intake.model.ValueType;

@Component
public class ValueTypeDaoImpl extends AbstractDao implements ValueTypeDao {

    private final EntityChangeLogger entityChangeLogger;
    private final ValueTypeRepository valueTypeRepository;

    public ValueTypeDaoImpl(final EntityChangeLogger entityChangeLogger,
                            final ValueTypeRepository valueTypeRepository,
                            final LanguageService languageService) {
        super(languageService);
        this.entityChangeLogger = entityChangeLogger;
        this.valueTypeRepository = valueTypeRepository;
    }

    @Transactional
    public ValueType findById(final UUID id) {
        return valueTypeRepository.findById(id);
    }

    @Transactional
    public ValueType findByLocalName(final String valueTypeLocalName) {
        return valueTypeRepository.findByLocalName(valueTypeLocalName);
    }

    @Transactional
    public Set<ValueType> findAll() {
        return valueTypeRepository.findAll();
    }

    @Transactional
    public ValueType updateValueTypeFromDto(final ValueTypeDTO valueTypeDTO) {
        ValueType valueType = createOrUpdateValueType(valueTypeDTO);
        valueTypeRepository.save(valueType);
        entityChangeLogger.logValueTypeChange(valueType);
        return valueType;
    }

    @Transactional
    public Set<ValueType> updateValueTypesFromDtos(final Set<ValueTypeDTO> valueTypeDtos) {
        final Set<ValueType> valueTypes = new HashSet<>();
        for (final ValueTypeDTO valueTypeDto : valueTypeDtos) {
            final ValueType valueType = createOrUpdateValueType(valueTypeDto);
            valueTypes.add(valueType);
            valueTypeRepository.save(valueType);
        }
        return valueTypes;
    }

    private ValueType createOrUpdateValueType(final ValueTypeDTO fromValueType) {
        final ValueType existingValueType;
        if (fromValueType.getId() != null) {
            existingValueType = valueTypeRepository.findByLocalName(fromValueType.getLocalName());
        } else {
            existingValueType = null;
        }
        final ValueType valueType;
        if (existingValueType != null) {
            valueType = updateValueType(existingValueType, fromValueType);
        } else {
            valueType = createValueType(fromValueType);
        }
        return valueType;
    }

    private ValueType updateValueType(final ValueType existingValueType,
                                      final ValueTypeDTO fromValueType) {
        if (!Objects.equals(existingValueType.getUri(), fromValueType.getUri())) {
            existingValueType.setUri(fromValueType.getUri());
        }
        if (!Objects.equals(existingValueType.getTypeUri(), fromValueType.getTypeUri())) {
            existingValueType.setTypeUri(fromValueType.getTypeUri());
        }
        if (!Objects.equals(existingValueType.getLocalName(), fromValueType.getLocalName())) {
            existingValueType.setLocalName(fromValueType.getLocalName());
        }
        if (!Objects.equals(existingValueType.getRegexp(), fromValueType.getRegexp())) {
            existingValueType.setRegexp(fromValueType.getRegexp());
        }
        existingValueType.setRequired(fromValueType.getRequired());
        existingValueType.setPrefLabel(validateLanguagesForLocalizable(fromValueType.getPrefLabel()));
        return existingValueType;
    }

    private ValueType createValueType(final ValueTypeDTO fromValueType) {
        final ValueType valueType = new ValueType();
        if (fromValueType.getId() != null) {
            valueType.setId(fromValueType.getId());
        } else {
            final UUID uuid = UUID.randomUUID();
            valueType.setId(uuid);
        }
        valueType.setUri(fromValueType.getUri());
        valueType.setTypeUri(fromValueType.getTypeUri());
        valueType.setLocalName(fromValueType.getLocalName());
        valueType.setRegexp(fromValueType.getRegexp());
        valueType.setRequired(fromValueType.getRequired());
        valueType.setPrefLabel(validateLanguagesForLocalizable(fromValueType.getPrefLabel()));
        return valueType;
    }

    @Transactional
    public void save(final ValueType valueType) {
        save(valueType, true);
    }

    @Transactional
    public void save(final ValueType valueType,
                     final boolean logChange) {
        valueTypeRepository.save(valueType);
        if (logChange) {
            entityChangeLogger.logValueTypeChange(valueType);
        }
    }

    @Transactional
    public void save(final Set<ValueType> valueTypes,
                     final boolean logChange) {
        valueTypeRepository.saveAll(valueTypes);
        if (logChange) {
            entityChangeLogger.logValueTypeChange(valueTypes);
        }
    }

    @Transactional
    public void save(final Set<ValueType> valueTypes) {
        save(valueTypes, true);
    }

}
