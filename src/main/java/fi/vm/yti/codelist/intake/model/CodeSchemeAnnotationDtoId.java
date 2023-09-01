package fi.vm.yti.codelist.intake.model;

import java.io.Serializable;
import java.util.UUID;

public class CodeSchemeAnnotationDtoId implements Serializable {

    private UUID codeschemeId;

    private UUID annotationId;

    public UUID getCodeschemeId() {
        return codeschemeId;
    }

    public void setCodeschemeId(final UUID codeschemeId) {
        this.codeschemeId = codeschemeId;
    }

    public UUID getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(final UUID annotationId) {
        this.annotationId = annotationId;
    }

    public CodeSchemeAnnotationDtoId(UUID codeschemeId, UUID annotationId) {
        this.codeschemeId = codeschemeId;
        this.annotationId = annotationId;
    }

    @Override
    public int hashCode() {
        return (int)(codeschemeId.hashCode() + annotationId.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CodeSchemeAnnotationDtoId) {
            CodeSchemeAnnotationDtoId otherId = (CodeSchemeAnnotationDtoId) object;
            return otherId.codeschemeId.equals(this.codeschemeId) && otherId.annotationId.equals(this.annotationId);
        }
        return false;
    }
}