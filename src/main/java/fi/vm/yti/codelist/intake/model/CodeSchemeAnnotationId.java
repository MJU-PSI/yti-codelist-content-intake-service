package fi.vm.yti.codelist.intake.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;

public class CodeSchemeAnnotationId implements Serializable {

    private UUID codeschemeId;

    private UUID annotationId;

    private String language;

    @Column(name = "codescheme_id")
    public UUID getCodeschemeId() {
        return codeschemeId;
    }

    public void setCodeschemeId(final UUID codeschemeId) {
        this.codeschemeId = codeschemeId;
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

    public CodeSchemeAnnotationId() { }

    public CodeSchemeAnnotationId(UUID codeschemeId, UUID annotationId) {
        this.codeschemeId = codeschemeId;
        this.annotationId = annotationId;
    }

    public CodeSchemeAnnotationId(UUID codeschemeId, UUID annotationId, String language) {
        this.codeschemeId = codeschemeId;
        this.annotationId = annotationId;
        this.language = language;
    }

    @Override
    public int hashCode() {
        return (int)(codeschemeId.hashCode() + annotationId.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CodeSchemeAnnotationId) {
            CodeSchemeAnnotationId otherId = (CodeSchemeAnnotationId) object;
            return otherId.codeschemeId.equals(this.codeschemeId) && otherId.annotationId.equals(this.annotationId) && otherId.language.equals(this.language);
        }
        return false;
    }
}