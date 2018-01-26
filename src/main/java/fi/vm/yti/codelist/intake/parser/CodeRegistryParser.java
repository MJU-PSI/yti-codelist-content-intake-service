package fi.vm.yti.codelist.intake.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.vm.yti.codelist.common.model.CodeRegistry;
import fi.vm.yti.codelist.common.model.Organization;
import fi.vm.yti.codelist.intake.api.ApiUtils;
import fi.vm.yti.codelist.intake.jpa.OrganizationRepository;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

/**
 * Class that handles parsing of CodeRegistries from source data.
 */
@Service
public class CodeRegistryParser extends AbstractBaseParser {

    private final ApiUtils apiUtils;
    private final ParserUtils parserUtils;
    private final OrganizationRepository organizationRepository;

    @Inject
    public CodeRegistryParser(final ApiUtils apiUtils,
                              final ParserUtils parserUtils,
                              final OrganizationRepository organizationRepository) {
        this.apiUtils = apiUtils;
        this.parserUtils = parserUtils;
        this.organizationRepository = organizationRepository;
    }

    /**
     * Parses the .csv CodeRegistry-file and returns the coderegistries as a set.
     *
     * @param inputStream The CodeRegistry-file.
     * @return Set of CodeRegistry objects.
     */
    public Set<CodeRegistry> parseCodeRegistriesFromCsvInputStream(final InputStream inputStream) throws IOException {
        final Set<CodeRegistry> codeRegistries = new HashSet<>();
        try (final InputStreamReader inputStreamReader = new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8);
             final BufferedReader in = new BufferedReader(inputStreamReader);
             final CSVParser csvParser = new CSVParser(in, CSVFormat.newFormat(',').withQuote('"').withQuoteMode(QuoteMode.MINIMAL).withHeader())) {
            final Map<String, Integer> headerMap = csvParser.getHeaderMap();
            final Map<String, String> prefLabelHeaders = new LinkedHashMap<>();
            final Map<String, String> definitionHeaders = new LinkedHashMap<>();
            for (final String value : headerMap.keySet()) {
                if (value.startsWith(CONTENT_HEADER_PREFLABEL_PREFIX)) {
                    prefLabelHeaders.put(resolveLanguageFromHeader(CONTENT_HEADER_PREFLABEL_PREFIX, value), value);
                } else if (value.startsWith(CONTENT_HEADER_DEFINITION_PREFIX)) {
                    definitionHeaders.put(resolveLanguageFromHeader(CONTENT_HEADER_DEFINITION_PREFIX, value), value);
                }
            }
            final List<CSVRecord> records = csvParser.getRecords();
            records.forEach(record -> {
                final String codeValue = record.get(CONTENT_HEADER_CODEVALUE);
                final String organizationsString = record.get(CONTENT_HEADER_ORGANIZATION);
                final Set<Organization> organizations = resolveOrganizations(organizationsString);
                final Map<String, String> prefLabel = new LinkedHashMap<>();
                prefLabelHeaders.forEach((language, header) ->
                    prefLabel.put(language, record.get(header)));
                final Map<String, String> definition = new LinkedHashMap<>();
                definitionHeaders.forEach((language, header) ->
                    definition.put(language, record.get(header)));
                final CodeRegistry codeRegistry = createOrUpdateCodeRegistry(codeValue, organizations, prefLabel, definition);
                if (codeRegistry != null) {
                    codeRegistries.add(codeRegistry);
                }
            });
        }
        return codeRegistries;
    }

    /**
     * Parses the .xls CodeRegistry Excel-file and returns the CodeRegistries as a set.
     *
     * @param inputStream The CodeRegistry containing Excel -file.
     * @return Set of CodeRegistry objects.
     */
    @SuppressFBWarnings("UC_USELESS_OBJECT")
    public Set<CodeRegistry> parseCodeRegistriesFromExcelInputStream(final InputStream inputStream) throws Exception {
        final Set<CodeRegistry> codeRegistries = new HashSet<>();
        try (final Workbook workbook = WorkbookFactory.create(inputStream)) {
            final DataFormatter formatter = new DataFormatter();
            Sheet sheet = workbook.getSheet(EXCEL_SHEET_CODEREGISTRIES);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }
            final Iterator<Row> rowIterator = sheet.rowIterator();
            final Map<String, Integer> genericHeaders = new LinkedHashMap<>();
            final Map<String, Integer> prefLabelHeaders = new LinkedHashMap<>();
            final Map<String, Integer> definitionHeaders = new LinkedHashMap<>();
            boolean firstRow = true;
            while (rowIterator.hasNext()) {
                final Row row = rowIterator.next();
                if (firstRow) {
                    final Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        final Cell cell = cellIterator.next();
                        final String value = cell.getStringCellValue();
                        final Integer index = cell.getColumnIndex();
                        if (value.startsWith(CONTENT_HEADER_PREFLABEL_PREFIX)) {
                            prefLabelHeaders.put(resolveLanguageFromHeader(CONTENT_HEADER_PREFLABEL_PREFIX, value), index);
                        } else if (value.startsWith(CONTENT_HEADER_DEFINITION_PREFIX)) {
                            definitionHeaders.put(resolveLanguageFromHeader(CONTENT_HEADER_DEFINITION_PREFIX, value), index);
                        } else {
                            genericHeaders.put(value, index);
                        }
                    }
                    firstRow = false;
                } else {
                    final String codeValue = formatter.formatCellValue(row.getCell(genericHeaders.get(CONTENT_HEADER_CODEVALUE)));
                    if (codeValue == null || codeValue.trim().isEmpty()) {
                        continue;
                    }
                    final String organizationsString = formatter.formatCellValue(row.getCell(genericHeaders.get(CONTENT_HEADER_ORGANIZATION)));
                    final Set<Organization> organizations = resolveOrganizations(organizationsString);
                    final Map<String, String> prefLabel = new LinkedHashMap<>();
                    prefLabelHeaders.forEach((language, header) ->
                        prefLabel.put(language, formatter.formatCellValue(row.getCell(header))));
                    final Map<String, String> definition = new LinkedHashMap<>();
                    definitionHeaders.forEach((language, header) ->
                        definition.put(language, formatter.formatCellValue(row.getCell(header))));
                    final CodeRegistry codeRegistry = createOrUpdateCodeRegistry(codeValue, organizations, prefLabel, definition);
                    codeRegistries.add(codeRegistry);
                }
            }
        }
        return codeRegistries;
    }

    private CodeRegistry createOrUpdateCodeRegistry(final String codeValue,
                                                    final Set<Organization> organizations,
                                                    final Map<String, String> prefLabel,
                                                    final Map<String, String> definition) {
        final Map<String, CodeRegistry> existingCodeRegistriesMap = parserUtils.getCodeRegistriesMap();
        final Date timeStamp = new Date(System.currentTimeMillis());
        CodeRegistry codeRegistry = existingCodeRegistriesMap.get(codeValue);
        if (codeRegistry != null) {
            final String uri = apiUtils.createResourceUri(API_PATH_CODEREGISTRIES, codeValue);
            boolean hasChanges = false;
            if (!Objects.equals(codeRegistry.getUri(), uri)) {
                codeRegistry.setUri(uri);
                hasChanges = true;
            }
            for (final Map.Entry<String, String> entry : prefLabel.entrySet()) {
                final String language = entry.getKey();
                final String value = entry.getValue();
                if (!Objects.equals(codeRegistry.getPrefLabel(language), value)) {
                    codeRegistry.setPrefLabel(language, value);
                    hasChanges = true;
                }
            }
            for (final Map.Entry<String, String> entry : definition.entrySet()) {
                final String language = entry.getKey();
                final String value = entry.getValue();
                if (!Objects.equals(codeRegistry.getDefinition(language), value)) {
                    codeRegistry.setDefinition(language, value);
                    hasChanges = true;
                }
            }
            if (hasChanges) {
                codeRegistry.setModified(timeStamp);
            }
        } else {
            codeRegistry = new CodeRegistry();
            codeRegistry.setId(UUID.randomUUID());
            codeRegistry.setCodeValue(codeValue);
            codeRegistry.setModified(timeStamp);
            codeRegistry.setOrganizations(organizations);
            for (Map.Entry<String, String> entry : prefLabel.entrySet()) {
                codeRegistry.setPrefLabel(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : definition.entrySet()) {
                codeRegistry.setDefinition(entry.getKey(), entry.getValue());
            }
            codeRegistry.setUri(apiUtils.createCodeRegistryUri(codeRegistry));
        }
        return codeRegistry;
    }

    final Set<Organization> resolveOrganizations(final String organizationsString) {
        final Set<Organization> organizations = new HashSet<>();
        if (organizationsString != null && !organizationsString.isEmpty()) {
            for (final String organizationId : organizationsString.split(";")) {
                final Organization organization = organizationRepository.findById(UUID.fromString(organizationId));
                if (organization != null) {
                    organizations.add(organization);
                }
            }
        }
        return organizations;
    }
}
