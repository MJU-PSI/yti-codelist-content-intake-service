package fi.vm.yti.codelist.intake.language;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.ErrorModel;
import fi.vm.yti.codelist.intake.dao.CodeDao;
import fi.vm.yti.codelist.intake.dao.CodeSchemeDao;
import fi.vm.yti.codelist.intake.data.YtiDataAccess;
import fi.vm.yti.codelist.intake.exception.YtiCodeListException;
import fi.vm.yti.codelist.intake.model.Code;
import fi.vm.yti.codelist.intake.model.CodeScheme;
import static fi.vm.yti.codelist.intake.configuration.ApplicationConstants.YTI_LANGUAGECODE_CODESCHEME;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.ERR_MSG_USER_BAD_LANGUAGECODE;
import static fi.vm.yti.codelist.intake.parser.impl.AbstractBaseParser.PUBLIC_ADMIN_SERVICE_REGISTRY;

@Component
public class LanguageService {

    private final CodeSchemeDao codeSchemeDao;
    private final CodeDao codeDao;
    private final YtiDataAccess ytiDataAccess;
    private final Map<String, Code> languageCodes;

    public LanguageService(@Lazy final CodeSchemeDao codeSchemeDao,
                           @Lazy final CodeDao codeDao,
                           @Lazy final YtiDataAccess ytiDataAccess) {

        this.codeSchemeDao = codeSchemeDao;
        this.codeDao = codeDao;
        this.ytiDataAccess = ytiDataAccess;
        languageCodes = new HashMap<>();
    }

    @Transactional
    public void loadLanguageCodes() {
        final CodeScheme languageCodeScheme = codeSchemeDao.findByCodeRegistryCodeValueAndCodeValue(PUBLIC_ADMIN_SERVICE_REGISTRY, YTI_LANGUAGECODE_CODESCHEME);
        if (languageCodeScheme != null) {
            final Set<Code> codes = codeDao.findByCodeSchemeId(languageCodeScheme.getId());
            if (codes == null || codes.isEmpty()) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "No codes available for languagecodes, critical failure!"));
            }
            codes.forEach(code -> languageCodes.put(code.getCodeValue().toLowerCase(), code));
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "No language codescheme found, critical failure!"));
        }
    }

    public Code getLanguageCode(final String languageCodeValue) {
        final Code languageCode = languageCodes.get(languageCodeValue.toLowerCase());
        if (languageCode == null) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_BAD_LANGUAGECODE, languageCodeValue));
        }
        return languageCode;
    }

    public String getLanguageCodeCodeValue(final String languageCodeValue) {
        if (!ytiDataAccess.isInitializing()) {
            final Code languageCode = languageCodes.get(languageCodeValue.toLowerCase());
            if (languageCode == null) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_BAD_LANGUAGECODE, languageCodeValue));
            }
            return languageCode.getCodeValue();
        } return languageCodeValue;
    }

    public String validateInputLanguageForCodeScheme(final CodeScheme codeScheme,
                                                     final String languageCodeCodeValue) {
        if (!ytiDataAccess.isInitializing()) {
            return validateInputLanguageForCodeScheme(codeScheme, languageCodeCodeValue, true);
        }
        return languageCodeCodeValue;
    }

    public String validateInputLanguageForCodeScheme(final CodeScheme codeScheme,
                                                     final String languageCodeCodeValue,
                                                     final boolean saveCodeScheme) {
        if (!ytiDataAccess.isInitializing()) {
            final Code inputLanguageCode = languageCodes.get(languageCodeCodeValue.toLowerCase());
            if (inputLanguageCode == null) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_BAD_LANGUAGECODE, languageCodeCodeValue));
            }
            boolean found = false;
            Set<Code> codeSchemeLanguageCodes = codeScheme.getLanguageCodes();
            if (codeSchemeLanguageCodes == null) {
                codeSchemeLanguageCodes = new HashSet<>();
            }
            for (final Code languageCode : codeSchemeLanguageCodes) {
                if (languageCode.getCodeValue().equalsIgnoreCase(languageCodeCodeValue)) {
                    found = true;
                }
            }
            if (!found) {
                codeSchemeLanguageCodes.add(inputLanguageCode);
                codeScheme.setLanguageCodes(codeSchemeLanguageCodes);
                if (saveCodeScheme) {
                    codeSchemeDao.save(codeScheme);
                }
            }
            return inputLanguageCode.getCodeValue();
        }
        return languageCodeCodeValue;
    }
}
