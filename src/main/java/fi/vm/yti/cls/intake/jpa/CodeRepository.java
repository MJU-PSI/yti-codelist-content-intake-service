package fi.vm.yti.cls.intake.jpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import fi.vm.yti.cls.common.model.Code;
import fi.vm.yti.cls.common.model.CodeScheme;

@Repository
public interface CodeRepository extends CrudRepository<Code, String> {

    Code findByCodeSchemeAndCodeValueAndStatus(final CodeScheme codeScheme, final String codeValue, final String status);

    Code findByCodeSchemeAndCodeValue(final CodeScheme codeScheme, final String codeValue);

    Code findById(final String id);

    List<Code> findByCodeScheme(final CodeScheme codeScheme);

    List<Code> findAll();

}
