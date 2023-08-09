package fi.vm.yti.codelist.intake.parser.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.vm.yti.codelist.common.dto.CodeRegistryDTO;
import fi.vm.yti.codelist.common.dto.ErrorModel;
import fi.vm.yti.codelist.intake.exception.CsvParsingException;
import fi.vm.yti.codelist.intake.exception.ExcelParsingException;
import fi.vm.yti.codelist.intake.exception.JsonParsingException;
import fi.vm.yti.codelist.intake.exception.MissingHeaderCodeValueException;
import fi.vm.yti.codelist.intake.exception.MissingHeaderPrefLabelException;
import fi.vm.yti.codelist.intake.exception.MissingRowValueCodeValueException;
import fi.vm.yti.codelist.intake.exception.MissingRowValuePrefLabelException;
import fi.vm.yti.codelist.intake.parser.CodeRegistryParser;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.*;

@Service
public class CodeRegistryParserImpl extends AbstractBaseParser implements CodeRegistryParser {

    private static final Logger LOG = LoggerFactory.getLogger(CodeRegistryParserImpl.class);

    @Override
    public CodeRegistryDTO parseCodeRegistryFromJsonData(final String jsonPayload) {
        final ObjectMapper mapper = createObjectMapper();
        final CodeRegistryDTO fromCodeRegistry;
        try {
            fromCodeRegistry = mapper.readValue(jsonPayload, CodeRegistryDTO.class);
        } catch (final IOException e) {
            LOG.error("CodeRegistry parsing failed from JSON!", e);
            throw new JsonParsingException(ERR_MSG_USER_CODEREGISTRY_PARSING_FAILED);
        }
        return fromCodeRegistry;
    }

    @Override
    public Set<CodeRegistryDTO> parseCodeRegistriesFromJsonData(final String jsonPayload) {
        final ObjectMapper mapper = createObjectMapper();
        final Set<CodeRegistryDTO> fromCodeRegistries;
        final Set<String> codeValues = new HashSet<>();
        try {
            fromCodeRegistries = mapper.readValue(jsonPayload, new TypeReference<Set<CodeRegistryDTO>>() {
            });
        } catch (final IOException e) {
            LOG.error("CodeRegistries parsing failed from JSON!", e);
            throw new JsonParsingException(ERR_MSG_USER_CODEREGISTRY_PARSING_FAILED);
        }
        for (final CodeRegistryDTO fromCodeRegistry : fromCodeRegistries) {
            checkForDuplicateCodeValueInImportData(codeValues, fromCodeRegistry.getCodeValue());
            codeValues.add(fromCodeRegistry.getCodeValue().toLowerCase());
        }
        return fromCodeRegistries;
    }

    @Override
    public Set<CodeRegistryDTO> parseCodeRegistriesFromCsvInputStream(final InputStream inputStream) {
        final Set<CodeRegistryDTO> codeRegistries = new HashSet<>();
        final Set<String> codeValues = new HashSet<>();
        try (final InputStreamReader inputStreamReader = new InputStreamReader(new BOMInputStream(inputStream), StandardCharsets.UTF_8);
             final BufferedReader in = new BufferedReader(inputStreamReader);
             final CSVParser csvParser = new CSVParser(in, CSVFormat.newFormat(',').withQuote('"').withQuoteMode(QuoteMode.MINIMAL).withHeader())) {
            final Map<String, Integer> headerMap = csvParser.getHeaderMap();
            final Map<String, Integer> prefLabelHeaders = parseHeadersWithPrefix(headerMap, CONTENT_HEADER_PREFLABEL_PREFIX);
            final Map<String, Integer> descriptionHeaders = parseHeadersWithPrefix(headerMap, CONTENT_HEADER_DESCRIPTION_PREFIX);
            final List<CSVRecord> records = csvParser.getRecords();
            records.forEach(record -> {
                final String recordIdentifier = getRecordIdentifier(record);
                final CodeRegistryDTO fromCodeRegistry = new CodeRegistryDTO();
                final String codeValue = parseCodeValueFromRecord(record);
                validateCodeValue(codeValue, recordIdentifier);
                validateRequiredDataOnRecord(record, headerMap);
                checkForDuplicateCodeValueInImportData(codeValues, codeValue);
                codeValues.add(codeValue.toLowerCase());
                fromCodeRegistry.setCodeValue(codeValue);
                fromCodeRegistry.setOrganizations(resolveOrganizations(record.get(CONTENT_HEADER_ORGANIZATION)));
                fromCodeRegistry.setPrefLabel(parseLocalizedValueFromCsvRecord(prefLabelHeaders, record));
                validateRequiredDataOnRecord(record, headerMap);
                fromCodeRegistry.setDescription(parseLocalizedValueFromCsvRecord(descriptionHeaders, record));
                codeRegistries.add(fromCodeRegistry);
            });
        } catch (final IllegalArgumentException e) {
            LOG.error("Duplicate header value found in CSV!", e);
            throw new CsvParsingException(ERR_MSG_USER_DUPLICATE_HEADER_VALUE);
        } catch (final IOException e) {
            LOG.error("Error parsing CSV file!", e);
            throw new CsvParsingException(ERR_MSG_USER_ERROR_PARSING_CSV_FILE);
        }
        return codeRegistries;
    }

    @Override
    @SuppressFBWarnings("UC_USELESS_OBJECT")
    public Set<CodeRegistryDTO> parseCodeRegistriesFromExcelInputStream(final InputStream inputStream) {
        final Set<CodeRegistryDTO> codeRegistries = new HashSet<>();
        final Set<String> codeValues = new HashSet<>();
        try (final Workbook workbook = WorkbookFactory.create(inputStream)) {
            final DataFormatter formatter = new DataFormatter();
            Sheet sheet = workbook.getSheet(EXCEL_SHEET_CODEREGISTRIES);
            if (sheet == null) {
                sheet = workbook.getSheetAt(0);
            }
            boolean firstRow = true;
            final Iterator<Row> rowIterator = sheet.rowIterator();
            Map<String, Integer> headerMap;
            Map<String, Integer> prefLabelHeaders = null;
            Map<String, Integer> descriptionHeaders = null;
            checkIfExcelEmpty(rowIterator);
            checkExcelMaxRows(sheet);
            while (rowIterator.hasNext()) {
                final Row row = rowIterator.next();
                final String rowIdentifier = getRowIdentifier(row);
                headerMap = resolveHeaderMap(row);
                if (firstRow) {
                    firstRow = false;
                    prefLabelHeaders = parseHeadersWithPrefix(headerMap, CONTENT_HEADER_PREFLABEL_PREFIX);
                    descriptionHeaders = parseHeadersWithPrefix(headerMap, CONTENT_HEADER_DESCRIPTION_PREFIX);
                    validateRequiredHeaders(headerMap);
                } else if (!checkIfRowIsEmpty(row)) {
                    final CodeRegistryDTO fromCodeRegistry = new CodeRegistryDTO();
                    final String codeValue = formatter.formatCellValue(row.getCell(headerMap.get(CONTENT_HEADER_CODEVALUE))).trim();
                    validateCodeValue(codeValue, rowIdentifier);
                    checkForDuplicateCodeValueInImportData(codeValues, codeValue);
                    codeValues.add(codeValue.toLowerCase());
                    fromCodeRegistry.setCodeValue(codeValue);
                    fromCodeRegistry.setOrganizations(resolveOrganizations(formatter.formatCellValue(row.getCell(headerMap.get(CONTENT_HEADER_ORGANIZATION)))));
                    fromCodeRegistry.setPrefLabel(parseLocalizedValueFromExcelRow(prefLabelHeaders, row, formatter));
                    validateRequiredDataOnRow(row, headerMap, formatter);
                    fromCodeRegistry.setDescription(parseLocalizedValueFromExcelRow(descriptionHeaders, row, formatter));
                    codeRegistries.add(fromCodeRegistry);
                }
            }
        } catch (final EmptyFileException | IOException e) {
            LOG.error("Error parsing Excel file!", e);
            throw new ExcelParsingException(ERR_MSG_USER_ERROR_PARSING_EXCEL_FILE);
        }
        return codeRegistries;
    }

    private void validateRequiredHeaders(final Map<String, Integer> headerMap) {
        if (!headerMap.containsKey(CONTENT_HEADER_ORGANIZATION)) {
            throw new MissingHeaderCodeValueException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(),
                ERR_MSG_USER_MISSING_HEADER_ORGANIZATION));
        }
        if (!headerMap.containsKey(CONTENT_HEADER_CODEVALUE)) {
            throw new MissingHeaderCodeValueException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(),
                ERR_MSG_USER_MISSING_HEADER_CODEVALUE));
        }
        if (!headerMapContainsAtLeastOneHeaderWhichStartsWithPrefLabel(headerMap)) {
            throw new MissingHeaderPrefLabelException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(),
                ERR_MSG_USER_MISSING_HEADER_PREFLABEL));
        }
    }

    private void validateRequiredDataOnRecord(final CSVRecord record,
                                              final Map<String, Integer> headerMap) {
        if (record.get(CONTENT_HEADER_CODEVALUE) == null || record.get(CONTENT_HEADER_CODEVALUE).isEmpty()) {
            throw new MissingRowValueCodeValueException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(),
                ERR_MSG_USER_ROW_MISSING_CODEVALUE, getRecordIdentifier(record)));
        }
        boolean foundAtLeastOnePrefLabelFromTheRecord = false;
        List<String> columnNamesWhichStartWithPrefLabelPrefix = new ArrayList<>();
        headerMap.keySet().forEach(key -> {
            if (key.startsWith(CONTENT_HEADER_PREFLABEL_PREFIX)) {
                columnNamesWhichStartWithPrefLabelPrefix.add(key);
            }
        });
        for (String prefLabelColumnName : columnNamesWhichStartWithPrefLabelPrefix) {
            if (record.get(prefLabelColumnName) != null &&
                !record.get(prefLabelColumnName).isEmpty()) {
                foundAtLeastOnePrefLabelFromTheRecord = true;
            }
        }
        if (!foundAtLeastOnePrefLabelFromTheRecord) {
            throw new MissingRowValuePrefLabelException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(),
                ERR_MSG_USER_ROW_MISSING_PREFLABEL_VALUE, getRecordIdentifier(record)));
        }
    }
}
