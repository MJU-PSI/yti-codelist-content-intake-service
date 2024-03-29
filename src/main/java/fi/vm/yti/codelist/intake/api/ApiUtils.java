package fi.vm.yti.codelist.intake.api;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import fi.vm.yti.codelist.common.dto.CodeDTO;
import fi.vm.yti.codelist.common.dto.CodeRegistryDTO;
import fi.vm.yti.codelist.common.dto.CodeSchemeDTO;
import fi.vm.yti.codelist.common.dto.ExternalReferenceDTO;
import fi.vm.yti.codelist.common.dto.PropertyTypeDTO;
import fi.vm.yti.codelist.common.dto.ValueTypeDTO;
import fi.vm.yti.codelist.intake.configuration.CodelistProperties;
import fi.vm.yti.codelist.intake.configuration.CommentsProperties;
import fi.vm.yti.codelist.intake.configuration.ContentIntakeServiceProperties;
import fi.vm.yti.codelist.intake.configuration.DataModelProperties;
import fi.vm.yti.codelist.intake.configuration.FrontendProperties;
import fi.vm.yti.codelist.intake.configuration.GroupManagementProperties;
import fi.vm.yti.codelist.intake.configuration.MessagingProperties;
import fi.vm.yti.codelist.intake.configuration.TerminologyProperties;
import fi.vm.yti.codelist.intake.configuration.UriProperties;
import fi.vm.yti.codelist.intake.model.Code;
import fi.vm.yti.codelist.intake.model.CodeRegistry;
import fi.vm.yti.codelist.intake.model.CodeScheme;
import fi.vm.yti.codelist.intake.model.Extension;
import fi.vm.yti.codelist.intake.model.Member;
import fi.vm.yti.codelist.intake.model.PropertyType;
import fi.vm.yti.codelist.intake.model.ValueType;

import static fi.vm.yti.codelist.common.constants.ApiConstants.*;
import static fi.vm.yti.codelist.intake.util.EncodingUtils.urlEncodeCodeValue;

@Component
public class ApiUtils {

    private final String DEFAULT_URI_HOST = "uri.suomi.fi";

    private final UriProperties uriProperties;
    private final CodelistProperties codelistProperties;
    private final ContentIntakeServiceProperties contentIntakeServiceProperties;
    private final GroupManagementProperties groupManagementProperties;
    private final TerminologyProperties terminologyProperties;
    private final DataModelProperties dataModelProperties;
    private final CommentsProperties commentsProperties;
    private final FrontendProperties frontendProperties;
    private final MessagingProperties messagingProperties;

    @Inject
    public ApiUtils(final CodelistProperties codelistProperties,
                    final ContentIntakeServiceProperties contentIntakeServiceProperties,
                    final GroupManagementProperties groupManagementProperties,
                    final TerminologyProperties terminologyProperties,
                    final DataModelProperties dataModelProperties,
                    final UriProperties uriProperties,
                    final CommentsProperties commentsProperties,
                    final FrontendProperties frontendProperties,
                    final MessagingProperties messagingProperties) {
        this.uriProperties = uriProperties;
        this.codelistProperties = codelistProperties;
        this.contentIntakeServiceProperties = contentIntakeServiceProperties;
        this.groupManagementProperties = groupManagementProperties;
        this.terminologyProperties = terminologyProperties;
        this.dataModelProperties = dataModelProperties;
        this.commentsProperties = commentsProperties;
        this.frontendProperties = frontendProperties;
        this.messagingProperties = messagingProperties;
    }

    public String getEnv() {
        return contentIntakeServiceProperties.getEnv();
    }

    private String createResourceUrl(final String apiPath,
                                     final String resourceId) {
        final StringBuilder builder = new StringBuilder();
        builder.append(codelistProperties.getPublicUrl());
        builder.append(API_CONTEXT_PATH_RESTAPI);
        builder.append(API_BASE_PATH);
        builder.append("/");
        builder.append(API_VERSION);
        builder.append(apiPath);
        builder.append("/");
        if (resourceId != null) {
            builder.append(resourceId);
        }
        return builder.toString();
    }

    private String createResourceUri(final String resourcePath) {
        final String port = uriProperties.getPort();
        final StringBuilder builder = new StringBuilder();
        builder.append(uriProperties.getScheme());
        builder.append("://");
        builder.append(uriProperties.getHost());
        appendPortToUrlIfNotEmpty(port, builder);
        builder.append(uriProperties.getContextPath());
        builder.append("/");
        if (resourcePath != null) {
            builder.append(resourcePath);
        }
        return builder.toString();
    }

    public String createCodeRegistryUri(final CodeRegistry codeRegistry) {
        return createResourceUri(codeRegistry.getCodeValue());
    }

    public String createCodeRegistryUrl(final CodeRegistryDTO codeRegistry) {
        return createResourceUrl(API_PATH_CODEREGISTRIES, codeRegistry.getCodeValue());
    }

    public String createCodeSchemeUri(final CodeScheme codeScheme) {
        return createCodeSchemeUri(codeScheme.getCodeRegistry(), codeScheme);
    }

    public String createCodeSchemeUrl(final CodeSchemeDTO codeScheme) {
        return createCodeSchemeUrl(codeScheme.getCodeRegistry(), codeScheme);
    }

    public String createCodeSchemeUri(final CodeRegistry codeRegistry,
                                      final CodeScheme codeScheme) {
        return createResourceUri(codeRegistry.getCodeValue() + "/" + codeScheme.getCodeValue());
    }

    private String createCodeSchemeUrl(final CodeRegistryDTO codeRegistry,
                                       final CodeSchemeDTO codeScheme) {
        return createResourceUrl(API_PATH_CODEREGISTRIES + "/" + codeRegistry.getCodeValue() + API_PATH_CODESCHEMES, codeScheme.getCodeValue());
    }

    public String createCodeUri(final Code code) {
        return createCodeUri(code.getCodeScheme().getCodeRegistry(), code.getCodeScheme(), code);
    }

    public String createCodeUri(final CodeRegistry codeRegistry,
                                final CodeScheme codeScheme,
                                final Code code) {
        return createResourceUri(codeRegistry.getCodeValue() + "/" + codeScheme.getCodeValue() + "/code/" + urlEncodeCodeValue(code.getCodeValue()));
    }

    public String createExtensionUri(final Extension extension) {
        return createResourceUri(extension.getParentCodeScheme().getCodeRegistry().getCodeValue() + "/" + extension.getParentCodeScheme().getCodeValue() + "/extension/" + urlEncodeCodeValue(extension.getCodeValue()));
    }

    public String createMemberUri(final Member member) {
        String theEndOfUri = member.getSequenceId() == null ? member.getId().toString() : member.getSequenceId().toString();
        return createResourceUri(member.getExtension().getParentCodeScheme().getCodeRegistry().getCodeValue() + "/" + member.getExtension().getParentCodeScheme().getCodeValue() + "/extension/" + urlEncodeCodeValue(member.getExtension().getCodeValue()) + "/member/" + theEndOfUri);
    }

    public String createCodeUrl(final CodeDTO code) {
        return createCodeUrl(code.getCodeScheme().getCodeRegistry().getCodeValue(), code.getCodeScheme().getCodeValue(), code.getCodeValue());
    }

    public String createCodeUrl(final String codeRegistryCodeValue,
                                final String codeSchemeCodeValue,
                                final String codeValue) {
        return createResourceUrl(API_PATH_CODEREGISTRIES + "/" + codeRegistryCodeValue + API_PATH_CODESCHEMES + "/" + codeSchemeCodeValue + API_PATH_CODES, urlEncodeCodeValue(codeValue));
    }

    public String createExternalReferenceUrl(final ExternalReferenceDTO externalReference) {
        return createResourceUrl(API_PATH_EXTERNALREFERENCES, externalReference.getId().toString());
    }

    public String createPropertyTypeUrl(final PropertyTypeDTO propertyType) {
        return createResourceUrl(API_PATH_PROPERTYTYPES, propertyType.getId().toString());
    }

    public String createValueTypeUrl(final ValueTypeDTO valueType) {
        return createResourceUrl(API_PATH_VALUETYPES, valueType.getId().toString());
    }

    public String createExtensionUrl(final Extension extension) {
        return createExtensionUrl(extension.getParentCodeScheme().getCodeRegistry().getCodeValue(), extension.getParentCodeScheme().getCodeValue(), extension.getCodeValue());
    }

    public String createExtensionUrl(final String codeRegistryCodeValue,
                                     final String codeSchemeCodeValue,
                                     final String codeValue) {
        return createResourceUrl(API_PATH_CODEREGISTRIES + "/" + codeRegistryCodeValue + API_PATH_CODESCHEMES + "/" + codeSchemeCodeValue + API_PATH_EXTENSIONS, urlEncodeCodeValue(codeValue));
    }

    public String createMemberUrl(final Member member) {
        return createResourceUrl(API_PATH_CODEREGISTRIES + "/" + member.getExtension().getParentCodeScheme().getCodeRegistry().getCodeValue() +
            API_PATH_CODESCHEMES + "/" + member.getExtension().getParentCodeScheme().getCodeValue() +
            API_PATH_EXTENSIONS + "/" + urlEncodeCodeValue(member.getExtension().getCodeValue()) +
            API_PATH_MEMBERS, member.getSequenceId() == null ? member.getId().toString() : member.getSequenceId().toString());
    }

    public String getContentIntakeServiceHostname() {
        final StringBuilder builder = new StringBuilder();
        final String port = contentIntakeServiceProperties.getPort();
        builder.append(contentIntakeServiceProperties.getHost());
        appendPortToUrlIfNotEmpty(port, builder);
        return builder.toString();
    }

    public String getDefaultStatus() {
        return frontendProperties.getDefaultStatus();
    }

    public String getCodeSchemeSortMode() {
        return frontendProperties.getCodeSchemeSortMode();
    }

    public String getGroupmanagementPublicUrl() {
        return groupManagementProperties.getPublicUrl();
    }

    public String getTerminologyPublicUrl() {
        return terminologyProperties.getPublicUrl();
    }

    public String getCommentsPublicUrl() {
        return commentsProperties.getPublicUrl();
    }

    public String getDataModelPublicUrl() {
        return dataModelProperties.getPublicUrl();
    }

    public boolean getMessagingEnabled() {
        return messagingProperties.getEnabled();
    }

    private void appendPortToUrlIfNotEmpty(final String port,
                                           final StringBuilder builder) {
        if (port != null && !port.isEmpty()) {
            builder.append(":");
            builder.append(port);
        }
    }

    public String replaceDomainInUrl(final ValueType valueType){
       return valueType.getUri().replace(DEFAULT_URI_HOST, uriProperties.getHost()); 
    }

    public String replaceDomainInUrl(final PropertyType propertyType){
        return propertyType.getUri().replace(DEFAULT_URI_HOST, uriProperties.getHost()); 
     }
}