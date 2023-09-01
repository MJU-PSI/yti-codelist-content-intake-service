package fi.vm.yti.codelist.intake.model;

import java.io.Serializable;
import java.util.UUID;

public class CodeAnnotationDtoId implements Serializable {

    private UUID codeId;

    private UUID annotationId;

    public UUID getCodeId() {
        return codeId;
    }

    public void setCodeId(final UUID codeId) {
        this.codeId = codeId;
    }

    public UUID getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(final UUID annotationId) {
        this.annotationId = annotationId;
    }

    public CodeAnnotationDtoId(UUID codeId, UUID annotationId) {
        this.codeId = codeId;
        this.annotationId = annotationId;
    }

    @Override
    public int hashCode() {
        return (int)(codeId.hashCode() + annotationId.hashCode());
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CodeAnnotationDtoId) {
            CodeAnnotationDtoId otherId = (CodeAnnotationDtoId) object;
            return otherId.codeId.equals(this.codeId) && otherId.annotationId.equals(this.annotationId);
        }
        return false;
    }
}