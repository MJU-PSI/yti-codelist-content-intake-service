package fi.vm.yti.codelist.intake.resource;

import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.ObjectWriterInjector;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonView;

import fi.vm.yti.codelist.common.dto.AnnotationDTO;
import fi.vm.yti.codelist.common.dto.Meta;
import fi.vm.yti.codelist.common.dto.Views;
import fi.vm.yti.codelist.intake.api.MetaResponseWrapper;
import fi.vm.yti.codelist.intake.api.ResponseWrapper;
import fi.vm.yti.codelist.intake.indexing.Indexing;
import fi.vm.yti.codelist.intake.service.AnnotationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import static fi.vm.yti.codelist.common.constants.ApiConstants.FILTER_NAME_ANNOTATION;

@Component
@Path("/v1/annotations")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Annotation")
public class AnnotationResource implements AbstractBaseResource {

    private final Indexing indexing;
    private final AnnotationService annotationService;

    @Inject
    public AnnotationResource(final Indexing indexing,
                          final AnnotationService annotationService) {
        this.indexing = indexing;
        this.annotationService = annotationService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Operation(summary = "Parses and creates or updates single Annotations from JSON input.")
    @ApiResponse(responseCode = "200", description = "Annotations added or modified successfully.")
    @Tag(name = "Annotation")
    @JsonView({ Views.ExtendedAnnotation.class, Views.Normal.class })
    public Response addOrUpdateAnnotationFromJson(@Parameter(description = "Pretty format JSON output.") @QueryParam("pretty") final String pretty,
                                              @RequestBody(description = "JSON payload for Annotation data.", required = true) final String jsonPayload) {
        return parseAndPersistAnnotationsFromSource(jsonPayload, pretty);
    }

    @POST
    @Path("{codeValue}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Operation(summary = "Parses and creates or updates single Annotation from JSON input.")
    @ApiResponse(responseCode = "200", description = "Annotation updated successfully.")
    @Tag(name = "Annotation")
    @JsonView({ Views.ExtendedAnnotation.class, Views.Normal.class })
    public Response updateAnnotation(@Parameter(description = "Annotation codeValue", required = true, in = ParameterIn.PATH) @PathParam("codeValue") final String codeValue,
                                              @RequestBody(description = "JSON payload for Annotation data.", required = true) final String jsonPayload) {
        return parseAndPersistAnnotationFromJson(jsonPayload);
    }

    @DELETE
    @Path("{codeValue}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Operation(summary = "Deletes a single existing Annotation.")
    @ApiResponse(responseCode = "200", description = "Annotation deleted.")
    @ApiResponse(responseCode = "404", description = "Annotation not found.")
    public Response deleteAnnotation(@Parameter(description = "Annotation UUID", required = true, in = ParameterIn.PATH) @PathParam("codeValue") final String codeValue) {
        final AnnotationDTO existingAnnotation = annotationService.findByCodeValue(codeValue);
        if (existingAnnotation != null) {
            annotationService.deleteAnnotation(existingAnnotation.getCodeValue());
            indexing.deleteAnnotation(existingAnnotation);
        } else {
            return Response.status(404).build();
        }
        final Meta meta = new Meta();
        final MetaResponseWrapper responseWrapper = new MetaResponseWrapper(meta);
        return Response.ok(responseWrapper).build();
    }

    @HEAD
    @Path("{codeValue}")
    @Operation(summary = "Check if a Annotation with a given codeValue exists.")
    @ApiResponse(responseCode = "200", description = "Found")
    @ApiResponse(responseCode = "404", description = "Not found")
    @Tag(name = "Annotation")
    public Response checkForExistingAnnotation(@Parameter(description = "Annotation codeValue", required = true, in = ParameterIn.PATH) @PathParam("codeValue") final String codeValue) {
        final AnnotationDTO registry = this.annotationService.findByCodeValue(codeValue);
        if (registry == null) {
            return Response.status(404).build();
        }
        return Response.status(200).build();
    }

    private Response parseAndPersistAnnotationsFromSource(final String jsonPayload,
                                                         final String pretty) {
        final Set<AnnotationDTO> annotations = annotationService.parseAndPersistAnnotationsSourceData(jsonPayload);
        indexing.updateAnnotations(annotations);
        final Meta meta = new Meta();
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_ANNOTATION, "annotation"), pretty));
        final ResponseWrapper<AnnotationDTO> responseWrapper = new ResponseWrapper<>(meta);
        meta.setMessage("Annotation added or modified.");
        meta.setCode(200);
        responseWrapper.setResults(annotations);
        return Response.ok(responseWrapper).build();
    }

    private Response parseAndPersistAnnotationFromJson(final String jsonPayload) {
        final Set<AnnotationDTO> annotations = annotationService.parseAndPersistAnnotationFromJson(jsonPayload);
        indexing.updateAnnotations(annotations);
        final Meta meta = new Meta();
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_ANNOTATION, "annotation"), null));
        final ResponseWrapper<AnnotationDTO> responseWrapper = new ResponseWrapper<>(meta);
        meta.setMessage("Annotation added or modified.");
        meta.setCode(200);
        responseWrapper.setResults(annotations);
        return Response.ok(responseWrapper).build();
    }
}
