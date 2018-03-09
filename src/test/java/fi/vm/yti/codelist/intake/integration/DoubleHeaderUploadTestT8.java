package fi.vm.yti.codelist.intake.integration;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;

import fi.vm.yti.codelist.intake.ContentIntakeServiceApplication;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ContentIntakeServiceApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@TestPropertySource(locations = "classpath:test-port.properties")
public class DoubleHeaderUploadTestT8 extends AbstractIntegrationTestBase {

    private static final String TEST_CODE_DUPLICATE_HEADER_FILENAME_1 = "v1_duplicate_header_test.xlsx";
    private static final String TEST_CODE_DUPLICATE_HEADER_FILENAME_2 = "v1_duplicate_header_test.csv";
    private static final String DUPLICATE_HEADER_TEST_CODESCHEME_1 = "duplicateheadercodescheme1";
    private static final String DUPLICATE_HEADER_TEST_CODESCHEME_2 = "duplicateheadercodescheme2";
    private TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    private int randomServerPort;

    @Test
    @Transactional
    public void postCodesWithDuplicateHeaderValuesToCodeSchemeTestExcel() {
        final String apiUrl = createApiUrl(randomServerPort, API_PATH_CODEREGISTRIES) + TEST_CODEREGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/" + DUPLICATE_HEADER_TEST_CODESCHEME_1 + API_PATH_CODES + "/" + "?format=" + FORMAT_EXCEL;
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final String registryFilePath = "/" + CODES_FOLDER_NAME + "/" + TEST_CODE_DUPLICATE_HEADER_FILENAME_1;
        parameters.add("file", new ClassPathResource(registryFilePath));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class, "");
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }

    @Test
    @Transactional
    public void postCodesWithDuplicateHeaderValuesToCodeSchemeTestCsv() {
        final String apiUrl = createApiUrl(randomServerPort, API_PATH_CODEREGISTRIES) + TEST_CODEREGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/" + DUPLICATE_HEADER_TEST_CODESCHEME_2 + API_PATH_CODES + "/" + "?format=" + FORMAT_CSV;
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final String registryFilePath = "/" + CODES_FOLDER_NAME + "/" + TEST_CODE_DUPLICATE_HEADER_FILENAME_2;
        parameters.add("file", new ClassPathResource(registryFilePath));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class, "");
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }
}
