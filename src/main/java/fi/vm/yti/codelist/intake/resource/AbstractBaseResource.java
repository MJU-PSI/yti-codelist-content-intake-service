package fi.vm.yti.codelist.intake.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;

import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

public abstract class AbstractBaseResource {

    public SimpleFilterProvider createSimpleFilterProvider(final String baseFilter,
                                                           final String expand) {
        final List<String> baseFilters = new ArrayList<>();
        baseFilters.add(baseFilter);
        return createSimpleFilterProvider(baseFilters, expand);
    }

    public SimpleFilterProvider createSimpleFilterProvider(final List<String> baseFilters,
                                                           final String expand) {
        final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(FILTER_NAME_CODEREGISTRY, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.addFilter(FILTER_NAME_CODESCHEME, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.addFilter(FILTER_NAME_CODE, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.addFilter(FILTER_NAME_EXTERNALREFERENCE, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.addFilter(FILTER_NAME_PROPERTYTYPE, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.addFilter(FILTER_NAME_DATACLASSIFICATION, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.setFailOnUnknownId(false);
        for (final String baseFilter : baseFilters) {
            filterProvider.removeFilter(baseFilter);
        }
        if (expand != null && !expand.isEmpty()) {
            final List<String> filterOptions = Arrays.asList(expand.split(","));
            for (final String filter : filterOptions) {
                filterProvider.removeFilter(filter);
            }
        }
        return filterProvider;
    }

    public void logApiRequest(final Logger logger,
                              final String method,
                              final String apiVersionPath,
                              final String apiPath) {
        logger.info(method + " " + apiVersionPath + apiPath + " requested!");
    }

    static class FilterModifier extends ObjectWriterModifier {

        private final FilterProvider provider;

        protected FilterModifier(final FilterProvider provider) {
            this.provider = provider;
        }

        @Override
        public ObjectWriter modify(final EndpointConfigBase<?> endpoint,
                                   final MultivaluedMap<String, Object> responseHeaders,
                                   final Object valueToWrite,
                                   final ObjectWriter w,
                                   final JsonGenerator g) throws IOException {
            return w.with(provider);
        }
    }

    public ObjectMapper createObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        return mapper;
    }
}
