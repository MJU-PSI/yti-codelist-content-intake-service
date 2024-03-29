package fi.vm.yti.codelist.intake.log;

import java.util.Set;

import fi.vm.yti.codelist.intake.model.Annotation;
import fi.vm.yti.codelist.intake.model.Code;
import fi.vm.yti.codelist.intake.model.CodeRegistry;
import fi.vm.yti.codelist.intake.model.CodeScheme;
import fi.vm.yti.codelist.intake.model.Extension;
import fi.vm.yti.codelist.intake.model.ExternalReference;
import fi.vm.yti.codelist.intake.model.Member;
import fi.vm.yti.codelist.intake.model.PropertyType;
import fi.vm.yti.codelist.intake.model.ValueType;

public interface EntityChangeLogger {

    void logCodeRegistryChange(final CodeRegistry codeRegistry);

    void logCodeSchemeChange(final CodeScheme codeScheme);

    void logAnnotationChange(final Annotation annotation);

    void logCodeChange(final Code code);

    void logCodesChange(final Set<Code> code);

    void logExternalReferenceChange(final ExternalReference externalReference);

    void logPropertyTypeChange(final PropertyType propertyType);

    void logPropertyTypeChange(final Set<PropertyType> propertyTypes);

    void logExtensionChange(final Extension extension);

    void logMemberChange(final Member member);

    void logMemberChanges(final Set<Member> members);

    void logValueTypeChange(final ValueType member); 

    void logValueTypeChange(final Set<ValueType> members); 
}
