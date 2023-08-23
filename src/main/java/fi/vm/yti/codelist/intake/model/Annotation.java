package fi.vm.yti.codelist.intake.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

@Entity
@Table(name = "annotation")
public class Annotation extends AbstractIdentifyableTimestampedCode implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codeValue;
    private Map<String, String> prefLabel;
    private Map<String, String> description;
    private Set<CodeSchemeAnnotation> codeSchemeAnnotations;
    //private Set<CodeScheme> codeSchemes;
    // private Date contentModified;

    public Annotation() {
        prefLabel = new HashMap<>();
        description = new HashMap<>();
    }

    public Annotation(final String codeValue) {
        this.codeValue = codeValue;
        prefLabel = new HashMap<>();
        description = new HashMap<>();
    }
    @Column(name = "codevalue")
    public String getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(final String codeValue) {
        this.codeValue = codeValue;
    }
/* 
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
 */

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "annotation_preflabel", joinColumns = @JoinColumn(name = "annotation_id", referencedColumnName = "id"))
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
    @CollectionTable(name = "annotation_description", joinColumns = @JoinColumn(name = "annotation_id", referencedColumnName = "id"))
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "annotation", cascade = CascadeType.ALL)
    public Set<CodeSchemeAnnotation> getCodeSchemeAnnotations() {
        return codeSchemeAnnotations;
    }

    public void setCodeSchemeAnnotations(final Set<CodeSchemeAnnotation> codeSchemeAnnotations) {
        this.codeSchemeAnnotations = codeSchemeAnnotations;
    }
/* 
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "codescheme_annotation",
        joinColumns = {
            @JoinColumn(name = "annotation_id", referencedColumnName = "id", nullable = false, updatable = false) },
        inverseJoinColumns = {
            @JoinColumn(name = "codescheme_id", referencedColumnName = "id", nullable = false, updatable = false) })
    public Set<CodeScheme> getCodeSchemes() {
        return codeSchemes;
    }

    public void setCodeSchemes(final Set<CodeScheme> codeSchemes) {
        this.codeSchemes = codeSchemes;
    }
 */
}
