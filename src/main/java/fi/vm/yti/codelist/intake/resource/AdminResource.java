package fi.vm.yti.codelist.intake.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.vm.yti.codelist.common.model.Code;
import fi.vm.yti.codelist.common.model.CodeRegistry;
import fi.vm.yti.codelist.common.model.CodeScheme;
import fi.vm.yti.codelist.common.model.ExternalReference;
import fi.vm.yti.codelist.common.model.PropertyType;
import fi.vm.yti.codelist.intake.api.ApiUtils;
import fi.vm.yti.codelist.intake.indexing.Indexing;
import fi.vm.yti.codelist.intake.jpa.CodeRegistryRepository;
import fi.vm.yti.codelist.intake.jpa.CodeRepository;
import fi.vm.yti.codelist.intake.jpa.CodeSchemeRepository;
import fi.vm.yti.codelist.intake.jpa.ExternalReferenceRepository;
import fi.vm.yti.codelist.intake.jpa.PropertyTypeRepository;
import fi.vm.yti.codelist.intake.parser.ExternalReferenceParser;
import fi.vm.yti.codelist.intake.parser.PropertyTypeParser;
import fi.vm.yti.codelist.intake.security.AuthorizationManager;
import fi.vm.yti.codelist.intake.util.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_ADMIN;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_CODEREGISTRIES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_CODES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_CODESCHEMES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_EXTERNALREFERENCES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_PROPERTYTYPES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_REINDEX;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_RELOAD;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_REWRITEURIS;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_VERSION_V1;
import static fi.vm.yti.codelist.common.constants.ApiConstants.DATA_EXTERNALREFERENCES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.DATA_PROPERTYTYPES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.METHOD_GET;
import static fi.vm.yti.codelist.intake.data.YtiDataAccess.DEFAULT_EXTERNALREFERENCE_FILENAME;
import static fi.vm.yti.codelist.intake.data.YtiDataAccess.DEFAULT_PROPERTYTYPE_FILENAME;

@Component
@Path("/admin")
@Api(value = "admin", description = "Operations for data administration and maintenance.")
@Produces("text/plain")
public class AdminResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(AdminResource.class);
    private final AuthorizationManager authorizationManager;
    private final CodeRegistryRepository codeRegistryRepository;
    private final CodeSchemeRepository codeSchemeRepository;
    private final CodeRepository codeRepository;
    private final ExternalReferenceRepository externalReferenceRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final PropertyTypeParser propertyTypeParser;
    private final ExternalReferenceParser externalReferenceParser;
    private final ApiUtils apiUtils;
    private final Indexing indexing;

    @Inject
    public AdminResource(final AuthorizationManager authorizationManager,
                         final CodeRegistryRepository codeRegistryRepository,
                         final CodeSchemeRepository codeSchemeRepository,
                         final CodeRepository codeRepository,
                         final ExternalReferenceRepository externalReferenceRepository,
                         final PropertyTypeRepository propertyTypeRepository,
                         final PropertyTypeParser propertyTypeParser,
                         final ExternalReferenceParser externalReferenceParser,
                         final ApiUtils apiUtils,
                         final Indexing indexing) {
        this.authorizationManager = authorizationManager;
        this.codeRegistryRepository = codeRegistryRepository;
        this.codeSchemeRepository = codeSchemeRepository;
        this.codeRepository = codeRepository;
        this.externalReferenceRepository = externalReferenceRepository;
        this.propertyTypeRepository = propertyTypeRepository;
        this.propertyTypeParser = propertyTypeParser;
        this.externalReferenceParser = externalReferenceParser;
        this.apiUtils = apiUtils;
        this.indexing = indexing;
    }

    @GET
    @Path("/coderegistries/rewriteuris/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Rewrites all coderegistry resource uris.")
    @ApiResponse(code = 200, message = "Upon successful request.")
    public Response rewriteCodeRegistryUris() {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_ADMIN + API_PATH_CODEREGISTRIES + API_PATH_REWRITEURIS);
        if (authorizationManager.isSuperUser()) {
            final Set<CodeRegistry> codeRegistries = codeRegistryRepository.findAll();
            for (final CodeRegistry codeRegistry : codeRegistries) {
                codeRegistry.setUri(apiUtils.createCodeRegistryUri(codeRegistry));
            }
            codeRegistryRepository.save(codeRegistries);
            LOG.info("CodeRegistry uris rewritten.");
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("/codeschemes/rewriteuris/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Rewrites all codescheme resource uris.")
    @ApiResponse(code = 200, message = "Upon successful request.")
    public Response rewriteCodeSchemeUris() {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_ADMIN + API_PATH_CODESCHEMES + API_PATH_REWRITEURIS);
        if (authorizationManager.isSuperUser()) {
            final Set<CodeScheme> codeSchemes = codeSchemeRepository.findAll();
            for (final CodeScheme codeScheme : codeSchemes) {
                codeScheme.setUri(apiUtils.createCodeSchemeUri(codeScheme));
            }
            codeSchemeRepository.save(codeSchemes);
            LOG.info("CodeScheme uris rewritten.");
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("/codes/rewriteuris/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Rewrites all code resource uris.")
    @ApiResponse(code = 200, message = "Upon successful request.")
    public Response rewriteCodeUris() {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_ADMIN + API_PATH_CODES + API_PATH_REWRITEURIS);
        if (authorizationManager.isSuperUser()) {
            final Set<Code> codes = codeRepository.findAll();
            for (final Code code : codes) {
                code.setUri(apiUtils.createCodeUri(code));
            }
            codeRepository.save(codes);
            LOG.info("Code uris rewritten.");
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("/externalreferences/reload")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Reloads global ExternalReferences from source data.")
    @ApiResponse(code = 200, message = "Upon successful request.")
    public Response reloadGlobalExternalReferences() {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_ADMIN + API_PATH_EXTERNALREFERENCES + API_PATH_RELOAD);
        if (authorizationManager.isSuperUser()) {
            try (final InputStream inputStream = FileUtils.loadFileFromClassPath("/" + DATA_EXTERNALREFERENCES + "/" + DEFAULT_EXTERNALREFERENCE_FILENAME)) {
                final List<ExternalReference> externalReferences = externalReferenceParser.parseExternalReferencesFromCsvInputStream(inputStream);
                externalReferenceRepository.save(externalReferences);
            } catch (IOException e) {
                LOG.error("Issue with parsing ExternalReference file. ", e);
            }
            LOG.info("ExternalReferences reloaded.");
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("/propertytypes/reload")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Reloads PropertyTypes from source data.")
    @ApiResponse(code = 200, message = "Upon successful request.")
    public Response reloadPropertyTypes() {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_ADMIN + API_PATH_PROPERTYTYPES + API_PATH_RELOAD);
        if (authorizationManager.isSuperUser()) {
            try (final InputStream inputStream = FileUtils.loadFileFromClassPath("/" + DATA_PROPERTYTYPES + "/" + DEFAULT_PROPERTYTYPE_FILENAME)) {
                final List<PropertyType> propertyTypes = propertyTypeParser.parsePropertyTypesFromCsvInputStream(inputStream);
                propertyTypeRepository.save(propertyTypes);
            } catch (IOException e) {
                LOG.error("Issue with parsing PropertyType file. ", e);
            }
            LOG.info("PropertyTypes reloaded.");
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("/reindex")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @ApiOperation(value = "Reindex ElasticSearch data.")
    @ApiResponse(code = 200, message = "Upon successful request.")
    public Response reIndex() {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_ADMIN + API_PATH_REINDEX);
        if (authorizationManager.isSuperUser()) {
            indexing.reIndexEverything();
            LOG.info("Reindexing finished.");
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}