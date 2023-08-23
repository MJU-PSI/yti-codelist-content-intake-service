package fi.vm.yti.codelist.intake.configuration;

public interface ApplicationConstants {

    public final static String YTI_DATACLASSIFICATION_INFODOMAIN_CODESCHEME = "serviceclassification";
    public final static String YTI_LANGUAGECODE_CODESCHEME = "languagecodes";
    public final static String DCAT_CODESCHEME = "dcat";
    public final static String[] INITIALIZATION_CODE_SCHEMES = { YTI_DATACLASSIFICATION_INFODOMAIN_CODESCHEME, YTI_LANGUAGECODE_CODESCHEME, DCAT_CODESCHEME };

    public static String[] initializationCodeSchemes() {
        return INITIALIZATION_CODE_SCHEMES.clone();
    }
}
