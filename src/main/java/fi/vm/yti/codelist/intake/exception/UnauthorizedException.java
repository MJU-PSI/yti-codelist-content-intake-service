package fi.vm.yti.codelist.intake.exception;

import fi.vm.yti.codelist.common.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import fi.vm.yti.codelist.common.model.ErrorModel;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class UnauthorizedException extends YtiCodeListException {
    public UnauthorizedException(ErrorModel errorModel) {
        super(errorModel);
    }
}
