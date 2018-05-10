package fi.vm.yti.codelist.intake.exception.exceptionmapping;

import javax.ws.rs.core.Response;

import fi.vm.yti.codelist.intake.api.ResponseWrapper;
import fi.vm.yti.codelist.intake.exception.YtiCodeListException;
import fi.vm.yti.codelist.intake.model.Meta;

abstract class BaseExceptionMapper {

    Response getResponse(final YtiCodeListException e) {
        final ResponseWrapper wrapper = new ResponseWrapper();
        final Meta meta = new Meta();
        meta.setMessage(e.getErrorModel().getMessage());
        meta.setCode(e.getErrorModel().getHttpStatusCode());
        meta.setEntityIdentifier(e.getErrorModel().getEntityIdentifier());
        wrapper.setMeta(meta);
        return Response.status(e.getErrorModel().getHttpStatusCode()).entity(wrapper).build();
    }
}
