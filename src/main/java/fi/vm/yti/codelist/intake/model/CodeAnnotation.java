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
@Table(name = "code_annotation")
@IdClass(CodeAnnotationId.class)
public class CodeAnnotation implements Serializable {

    private UUID codeId;
    private UUID annotationId;
    private String language;
    private String value;
    private Code code;
    private Annotation annotation;

    public CodeAnnotation() { }

    @Id
    public UUID getCodeId() {
        return codeId;
    }

    public void setCodeId(final UUID codeId) {
        this.codeId = codeId;
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
    @MapsId("codeId")
    @PrimaryKeyJoinColumn(name = "code_id", referencedColumnName = "id")
    public Code getCode() {
        return code;
    }

    public void setCode(final Code code) {
        this.code = code;
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
