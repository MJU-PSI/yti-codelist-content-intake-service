package fi.vm.yti.codelist.intake.dao;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.common.dto.ValueTypeDTO;
import fi.vm.yti.codelist.intake.model.ValueType;

public interface ValueTypeDao {
    void save(final ValueType valueType);

    void save(final ValueType valueType,
              final boolean logChange);

    void save(final Set<ValueType> valueTypes);

    void save(final Set<ValueType> valueTypes,
              final boolean logChange);

    ValueType findByLocalName(final String localName);

    ValueType findById(final UUID id);

    Set<ValueType> findAll();

    ValueType updateValueTypeFromDto(final ValueTypeDTO valueTypeDTO);

    Set<ValueType> updateValueTypesFromDtos(final Set<ValueTypeDTO> valueTypeDtos);
}
