package fi.vm.yti.codelist.intake.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;

public class CodeAnnotationId implements Serializable {

    private UUID codeId;

    private UUID annotationId;

    private String language;

    @Column(name = "code_id")
    public UUID getCodeId() {
        return codeId;
    }

    public void setCodeId(final UUID codeId) {
        this.codeId = codeId;
    }

    @Column(name = "annotation_id")
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

    public CodeAnnotationId() { }

    public CodeAnnotationId(UUID codeId, UUID annotationId) {
        this.codeId = codeId;
        this.annotationId = annotationId;
    }

    public CodeAnnotationId(UUID codeId, UUID annotationId, String language) {
        this.codeId = codeId;
        this.annotationId = annotationId;
        this.language = language;
    }

    @Override
    public int hashCode() {
        return (int)(codeId.hashCode() + annotationId.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CodeAnnotationId) {
            CodeAnnotationId otherId = (CodeAnnotationId) object;
            return otherId.codeId.equals(this.codeId) && otherId.annotationId.equals(this.annotationId) && otherId.language.equals(this.language);
        }
        return false;
    }
}