package fi.vm.yti.codelist.intake.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "codescheme_annotation")
@IdClass(CodeSchemeAnnotationId.class)
public class CodeSchemeAnnotation implements Serializable {

    private UUID codeschemeId;
    private UUID annotationId;
    private String language;
    private String value;
    private CodeScheme codescheme;
    private Annotation annotation;

    public CodeSchemeAnnotation() { }

    @Id
    public UUID getCodeschemeId() {
        return codeschemeId;
    }

    public void setCodeschemeId(final UUID codeschemeId) {
        this.codeschemeId = codeschemeId;
    }

    @Id
    public UUID getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(final UUID annotationId) {
        this.annotationId = annotationId;
    }

    @Column(name = "language")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    @Column(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("codeschemeId")
    @PrimaryKeyJoinColumn(name = "codescheme_id", referencedColumnName = "id")
    public CodeScheme getCodescheme() {
        return codescheme;
    }

    public void setCodescheme(final CodeScheme codescheme) {
        this.codescheme = codescheme;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("annotationId")
    @PrimaryKeyJoinColumn(name = "annotation_id", referencedColumnName = "id")
    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(final Annotation annotation) {
        this.annotation = annotation;
    }
}
