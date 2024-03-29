package fi.vm.yti.codelist.intake.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "codescheme")
public class CodeScheme extends AbstractHistoricalCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private String version;
    private String source;
    private String legalBase;
    private String governancePolicy;
    private Map<String, String> prefLabel;
    private Map<String, String> definition;
    private Map<String, String> description;
    private Map<String, String> changeNote;
    private CodeRegistry codeRegistry;
    private Set<Code> codes;
    private Set<Code> infoDomains;
    private Set<ExternalReference> externalReferences;
    private Set<Code> languageCodes;
    private Set<Extension> extensions;
    private Set<Extension> relatedExtensions;
    private String conceptUriInVocabularies;
    private Code defaultCode;
    private Set<CodeScheme> variants;
    private Set<CodeScheme> variantMothers;
    private UUID nextCodeschemeId;
    private UUID prevCodeschemeId;
    private UUID lastCodeschemeId;
    private Set<Organization> organizations;
    private boolean cumulative;
    private Map<String, String> feedbackChannel;
    private Date contentModified;
    private Date statusModified;
    private Set<CodeSchemeAnnotation> codeSchemeAnnotations;

    public CodeScheme() {
        prefLabel = new HashMap<>();
        feedbackChannel = new HashMap<>();
    }

    public CodeScheme(final String codeValue,
                      final String url,
                      final String version,
                      final String status) {
        super.setCodeValue(codeValue);
        super.setUri(url);
        super.setStatus(status);
        this.version = version;
        prefLabel = new HashMap<>();
    }

    @Column(name = "content_modified")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getContentModified() {
        if (contentModified != null) {
            return new Date(contentModified.getTime());
        }
        return null;
    }

    public void setContentModified(final Date contentModified) {
        if (contentModified != null) {
            this.contentModified = new Date(contentModified.getTime());
        } else {
            this.contentModified = null;
        }
    }

    @Column(name = "status_modified")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStatusModified() {
        if (statusModified != null) {
            return new Date(statusModified.getTime());
        }
        return null;
    }

    public void setStatusModified(final Date statusModified) {
        if (statusModified != null) {
            this.statusModified = new Date(statusModified.getTime());
        } else {
            this.statusModified = null;
        }
    }

    @Column(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Column(name = "source")
    public String getSource() {
        return source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    @Column(name = "legalbase")
    public String getLegalBase() {
        return legalBase;
    }

    public void setLegalBase(final String legalBase) {
        this.legalBase = legalBase;
    }

    @Column(name = "governancepolicy")
    public String getGovernancePolicy() {
        return governancePolicy;
    }

    public void setGovernancePolicy(final String governancePolicy) {
        this.governancePolicy = governancePolicy;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coderegistry_id", nullable = false, updatable = false)
    public CodeRegistry getCodeRegistry() {
        return codeRegistry;
    }

    public void setCodeRegistry(final CodeRegistry codeRegistry) {
        this.codeRegistry = codeRegistry;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defaultcode_id")
    public Code getDefaultCode() {
        return defaultCode;
    }

    public void setDefaultCode(final Code defaultCode) {
        this.defaultCode = defaultCode;
    }

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "codescheme_preflabel", joinColumns = @JoinColumn(name = "codescheme_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "language")
    @Column(name = "preflabel")
    @OrderColumn
    public Map<String, String> getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(final Map<String, String> prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getPrefLabel(final String language) {
        if (this.prefLabel != null && !this.prefLabel.isEmpty()) {
            return this.prefLabel.get(language);
        }
        return null;
    }

    public void setPrefLabel(final String language,
                             final String value) {
        if (this.prefLabel == null) {
            this.prefLabel = new HashMap<>();
        }
        if (language != null && value != null && !value.isEmpty()) {
            this.prefLabel.put(language, value);
        } else if (language != null) {
            this.prefLabel.remove(language);
        }
        setPrefLabel(this.prefLabel);
    }

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "codescheme_definition", joinColumns = @JoinColumn(name = "codescheme_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "language")
    @Column(name = "definition")
    @OrderColumn
    public Map<String, String> getDefinition() {
        if (definition == null) {
            definition = new HashMap<>();
        }
        return definition;
    }

    public void setDefinition(final Map<String, String> definition) {
        this.definition = definition;
    }

    public String getDefinition(final String language) {
        if (this.definition != null && !this.definition.isEmpty()) {
            return this.definition.get(language);
        }
        return null;
    }

    public void setDefinition(final String language,
                              final String value) {
        if (this.definition == null) {
            this.definition = new HashMap<>();
        }
        if (language != null && value != null && !value.isEmpty()) {
            this.definition.put(language, value);
        } else if (language != null) {
            this.definition.remove(language);
        }
        setDefinition(this.definition);
    }

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "codescheme_description", joinColumns = @JoinColumn(name = "codescheme_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "language")
    @Column(name = "description")
    @OrderColumn
    public Map<String, String> getDescription() {
        if (description == null) {
            description = new HashMap<>();
        }
        return description;
    }

    public void setDescription(final Map<String, String> description) {
        this.description = description;
    }

    public String getDescription(final String language) {
        if (this.description != null && !this.description.isEmpty()) {
            return this.description.get(language);
        }
        return null;
    }

    public void setDescription(final String language,
                               final String value) {
        if (this.description == null) {
            this.description = new HashMap<>();
        }
        if (language != null && value != null && !value.isEmpty()) {
            this.description.put(language, value);
        } else if (language != null) {
            this.description.remove(language);
        }
        setDescription(this.description);
    }

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "codescheme_changenote", joinColumns = @JoinColumn(name = "codescheme_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "language")
    @Column(name = "changenote")
    @OrderColumn
    public Map<String, String> getChangeNote() {
        if (changeNote == null) {
            changeNote = new HashMap<>();
        }
        return changeNote;
    }

    public void setChangeNote(final Map<String, String> changeNote) {
        this.changeNote = changeNote;
    }

    public String getChangeNote(final String language) {
        if (this.changeNote != null && !this.changeNote.isEmpty()) {
            return this.changeNote.get(language);
        }
        return null;
    }

    public void setChangeNote(final String language,
                              final String value) {
        if (this.changeNote == null) {
            this.changeNote = new HashMap<>();
        }
        if (language != null && value != null && !value.isEmpty()) {
            this.changeNote.put(language, value);
        } else if (language != null) {
            this.changeNote.remove(language);
        }
        setChangeNote(this.changeNote);
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinTable(name = "codescheme_externalreference",
        joinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id", nullable = false, updatable = false) },
        inverseJoinColumns = {
            @JoinColumn(name = "externalreference_id", referencedColumnName = "id", nullable = false, updatable = false) })
    public Set<ExternalReference> getExternalReferences() {
        return this.externalReferences;
    }

    public void setExternalReferences(final Set<ExternalReference> externalReferences) {
        this.externalReferences = externalReferences;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "codeScheme", cascade = CascadeType.ALL)
    public Set<Code> getCodes() {
        return codes;
    }

    public void setCodes(final Set<Code> codes) {
        this.codes = codes;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "service_codescheme_code",
        joinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id", nullable = false, updatable = false) },
        inverseJoinColumns = {
            @JoinColumn(name = "code_id", referencedColumnName = "id", nullable = false, updatable = false) })
    public Set<Code> getInfoDomains() {
        return infoDomains;
    }

    public void setInfoDomains(final Set<Code> infoDomains) {
        this.infoDomains = infoDomains;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "languagecode_codescheme_code",
        joinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id", nullable = false, updatable = false) },
        inverseJoinColumns = {
            @JoinColumn(name = "code_id", referencedColumnName = "id", nullable = false, updatable = false) })
    public Set<Code> getLanguageCodes() {
        return languageCodes;
    }

    public void setLanguageCodes(final Set<Code> languageCodes) {
        this.languageCodes = languageCodes;
    }

    @Column(name = "vocabularies_uri")
    public String getConceptUriInVocabularies() {
        return conceptUriInVocabularies;
    }

    public void setConceptUriInVocabularies(final String conceptUriInVocabularies) {
        this.conceptUriInVocabularies = conceptUriInVocabularies;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentCodeScheme", cascade = CascadeType.ALL)
    public Set<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(final Set<Extension> extensions) {
        this.extensions = extensions;
    }

    @Column(name = "next_codescheme_id")
    public UUID getNextCodeschemeId() {
        return nextCodeschemeId;
    }

    public void setNextCodeschemeId(final UUID nextCodeschemeId) {
        this.nextCodeschemeId = nextCodeschemeId;
    }

    @Column(name = "prev_codescheme_id")
    public UUID getPrevCodeschemeId() {
        return prevCodeschemeId;
    }

    public void setPrevCodeschemeId(final UUID prevCodeschemeId) {
        this.prevCodeschemeId = prevCodeschemeId;
    }

    @Column(name = "last_codescheme_id")
    public UUID getLastCodeschemeId() {
        return lastCodeschemeId;
    }

    public void setLastCodeschemeId(final UUID lastCodeschemeId) {
        this.lastCodeschemeId = lastCodeschemeId;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "codescheme_variant",
        joinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id") },
        inverseJoinColumns = {
            @JoinColumn(name = "variant_codescheme_id", referencedColumnName = "id") })
    public Set<CodeScheme> getVariants() {
        return variants;
    }

    public void setVariants(final Set<CodeScheme> variants) {
        this.variants = variants;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "codescheme_variant",
        joinColumns = {
            @JoinColumn(name = "variant_codescheme_id", referencedColumnName = "id") },
        inverseJoinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id") })
    public Set<CodeScheme> getVariantMothers() {
        return variantMothers;
    }

    public void setVariantMothers(final Set<CodeScheme> variantMothers) {
        this.variantMothers = variantMothers;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "codescheme_organization",
        joinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id", nullable = false, updatable = false) },
        inverseJoinColumns = {
            @JoinColumn(name = "organization_id", referencedColumnName = "id", nullable = false, updatable = false) })
    public Set<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(final Set<Organization> organizations) {
        this.organizations = organizations;
    }

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinTable(name = "extension_codescheme",
        joinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id", nullable = false, updatable = false) },
        inverseJoinColumns = {
            @JoinColumn(name = "extension_id", referencedColumnName = "id", nullable = false, updatable = false) })
    public Set<Extension> getRelatedExtensions() {
        return relatedExtensions;
    }

    public void setRelatedExtensions(final Set<Extension> relatedExtensions) {
        this.relatedExtensions = relatedExtensions;
    }

    @Column(name = "cumulative")
    public boolean isCumulative() {
        return cumulative;
    }

    public void setCumulative(final boolean cumulative) {
        this.cumulative = cumulative;
    }

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "codescheme_feedback_channel", joinColumns = @JoinColumn(name = "codescheme_id", referencedColumnName = "id"))
    @MapKeyColumn(name = "language")
    @Column(name = "feedback_channel")
    @OrderColumn
    public Map<String, String> getFeedbackChannel() {
        return feedbackChannel;
    }

    public void setFeedbackChannel(final Map<String, String> feedbackChannel) {
        this.feedbackChannel = feedbackChannel;
    }

    public String getFeedbackChannel(final String language) {
        if (this.feedbackChannel != null && !this.feedbackChannel.isEmpty()) {
            return this.feedbackChannel.get(language);
        }
        return null;
    }

    public void setFeedbackChannel(final String language,
                                   final String value) {
        if (this.feedbackChannel == null) {
            this.feedbackChannel = new HashMap<>();
        }
        if (language != null && value != null && !value.isEmpty()) {
            this.feedbackChannel.put(language, value);
        } else if (language != null) {
            this.feedbackChannel.remove(language);
        }
        setFeedbackChannel(this.feedbackChannel);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "codescheme", cascade = CascadeType.ALL)
    public Set<CodeSchemeAnnotation> getCodeSchemeAnnotations() {
        return codeSchemeAnnotations;
    }

    public void setCodeSchemeAnnotations(final Set<CodeSchemeAnnotation> codeSchemeAnnotations) {
        this.codeSchemeAnnotations = codeSchemeAnnotations;
    }
}
