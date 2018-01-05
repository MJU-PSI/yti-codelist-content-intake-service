package fi.vm.yti.codelist.intake.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fi.vm.yti.codelist.common.model.PropertyType;
import fi.vm.yti.codelist.intake.api.ApiUtils;
import fi.vm.yti.codelist.intake.jpa.PropertyTypeRepository;
import fi.vm.yti.codelist.intake.util.FileUtils;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

/**
 * Class that handles parsing of PropertyTypes from source data.
 */
@Service
public class PropertyTypeParser extends AbstractBaseParser {

    private static final Logger LOG = LoggerFactory.getLogger(CodeSchemeParser.class);
    private final ApiUtils apiUtils;
    private final PropertyTypeRepository propertyTypeRepository;

    @Inject
    public PropertyTypeParser(final ApiUtils apiUtils,
                              final PropertyTypeRepository codeSchemeRepository) {
        this.apiUtils = apiUtils;
        this.propertyTypeRepository = codeSchemeRepository;
    }

    /**
     * Parses the .csv PropertyType-file and returns the PropertyType as an ArrayList.
     *
     * @param inputStream The PropertyType -file.
     * @return List of PropertyType objects.
     */
    public List<PropertyType> parsePropertyTypesFromCsvInputStream(final InputStream inputStream) {
        final List<PropertyType> propertyTypes = new ArrayList<>();
        try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             final BufferedReader in = new BufferedReader(inputStreamReader);
             final CSVParser csvParser = new CSVParser(in, CSVFormat.newFormat(',').withQuote('"').withQuoteMode(QuoteMode.MINIMAL).withHeader())) {
            FileUtils.skipBom(in);
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
            for (final CSVRecord record : records) {
                final UUID id = parseUUIDFromString(record.get(CONTENT_HEADER_ID));
                final String localName = record.get(CONTENT_HEADER_LOCALNAME);
                final String propertyUri = record.get(CONTENT_HEADER_PROPERTYURI);
                final String context = record.get(CONTENT_HEADER_CONTEXT);
                final String type = record.get(CONTENT_HEADER_TYPE);
                final Map<String, String> prefLabel = new LinkedHashMap<>();
                prefLabelHeaders.forEach((language, header) -> {
                    prefLabel.put(language, record.get(header));
                });
                final Map<String, String> definition = new LinkedHashMap<>();
                definitionHeaders.forEach((language, header) -> {
                    definition.put(language, record.get(header));
                });
                final PropertyType propertyType = createOrUpdatePropertyType(id, propertyUri, context, localName, type, prefLabel, definition);
                propertyTypes.add(propertyType);
            }
        } catch (IOException e) {
            LOG.error("Parsing PropertyTypes failed: " + e.getMessage());
        }
        return propertyTypes;
    }

    /*
     * Parses the .xls PropertyType Excel-file and returns the PropertyTypes as an arrayList.
     *
     * @param codeRegistry CodeRegistry.
     * @param inputStream The Code containing Excel -file.
     * @return List of Code objects.
     */
    public List<PropertyType> parsePropertyTypesFromExcelInputStream(final InputStream inputStream) throws Exception {
        final List<PropertyType> propertyTypes = new ArrayList<>();
        try (final Workbook workbook = new XSSFWorkbook(inputStream)) {
            final Sheet codesSheet = workbook.getSheet(EXCEL_SHEET_PROPERTYTYPES);
            final Iterator<Row> rowIterator = codesSheet.rowIterator();
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
                    final UUID id = parseUUIDFromString(row.getCell(genericHeaders.get(CONTENT_HEADER_ID)).getStringCellValue());
                    final String localName = row.getCell(genericHeaders.get(CONTENT_HEADER_LOCALNAME)).getStringCellValue();
                    final String propertyUri = row.getCell(genericHeaders.get(CONTENT_HEADER_PROPERTYURI)).getStringCellValue();
                    final String context = row.getCell(genericHeaders.get(CONTENT_HEADER_CONTEXT)).getStringCellValue();
                    final String type = row.getCell(genericHeaders.get(CONTENT_HEADER_TYPE)).getStringCellValue();
                    final Map<String, String> prefLabel = new LinkedHashMap<>();
                    for (final String language : prefLabelHeaders.keySet()) {
                        prefLabel.put(language, row.getCell(prefLabelHeaders.get(language)).getStringCellValue());
                    }
                    final Map<String, String> definition = new LinkedHashMap<>();
                    for (final String language : definitionHeaders.keySet()) {
                        definition.put(language, row.getCell(definitionHeaders.get(language)).getStringCellValue());
                    }
                    final PropertyType propertyType = createOrUpdatePropertyType(id, propertyUri, context, localName, type, prefLabel, definition);
                    if (propertyType != null) {
                        propertyTypes.add(propertyType);
                    }
                }
            }
        }
        return propertyTypes;
    }

    private PropertyType createOrUpdatePropertyType(final UUID id,
                                                    final String propertyUri,
                                                    final String context,
                                                    final String localName,
                                                    final String type,
                                                    final Map<String, String> prefLabel,
                                                    final Map<String, String> definition) {
        PropertyType propertyType = null;
        String uri = null;
        if (id != null) {
            propertyType = propertyTypeRepository.findById(id);
            uri = apiUtils.createResourceUri(API_PATH_PROPERTYTYPES, id.toString());
        }
        if (propertyType != null) {
            if (!Objects.equals(propertyType.getPropertyUri(), propertyUri)) {
                propertyType.setPropertyUri(propertyUri);
            }
            if (!Objects.equals(propertyType.getUri(), uri)) {
                propertyType.setUri(uri);
            }
            if (!Objects.equals(propertyType.getContext(), context)) {
                propertyType.setUri(context);
            }
            if (!Objects.equals(propertyType.getLocalName(), localName)) {
                propertyType.setLocalName(localName);
            }
            if (!Objects.equals(propertyType.getType(), type)) {
                propertyType.setType(type);
            }
            for (final String language : prefLabel.keySet()) {
                final String value = prefLabel.get(language);
                if (!Objects.equals(propertyType.getPrefLabel(language), value)) {
                    propertyType.setPrefLabel(language, value);
                }
            }
            for (final String language : definition.keySet()) {
                final String value = definition.get(language);
                if (!Objects.equals(propertyType.getDefinition(language), value)) {
                    propertyType.setDefinition(language, value);
                }
            }
        } else {
            propertyType = new PropertyType();
            if (id != null) {
                propertyType.setId(id);
            } else {
                final UUID uuid = UUID.randomUUID();
                uri = apiUtils.createResourceUri(API_PATH_PROPERTYTYPES, uuid.toString());
                propertyType.setId(uuid);
            }
            propertyType.setContext(context);
            propertyType.setLocalName(localName);
            propertyType.setType(type);
            propertyType.setUri(uri);
            propertyType.setPropertyUri(propertyUri);
            for (final String language : prefLabel.keySet()) {
                propertyType.setPrefLabel(language, prefLabel.get(language));
            }
            for (final String language : definition.keySet()) {
                propertyType.setDefinition(language, definition.get(language));
            }
        }
        return propertyType;
    }
}
