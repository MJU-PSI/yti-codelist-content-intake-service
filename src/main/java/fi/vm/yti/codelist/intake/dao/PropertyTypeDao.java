package fi.vm.yti.codelist.intake.dao;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.codelist.common.dto.PropertyTypeDTO;
import fi.vm.yti.codelist.intake.model.PropertyType;

public interface PropertyTypeDao {

    void save(final PropertyType propertyType);

    void save(final PropertyType propertyType,
              final boolean logChange);

    void save(final Set<PropertyType> propertyTypes);

    void save(final Set<PropertyType> propertyType,
              final boolean logChange);

    PropertyType findByContextAndLocalName(final String context,
                                           final String localName);

    PropertyType findByLocalName(final String localName);

    PropertyType findById(final UUID id);

    Set<PropertyType> findAll();

    PropertyType updatePropertyTypeFromDto(final PropertyTypeDTO propertyTypeDTO);

    Set<PropertyType> updatePropertyTypesFromDtos(final Set<PropertyTypeDTO> propertyTypeDtos);
}
