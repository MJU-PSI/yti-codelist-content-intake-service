package fi.vm.yti.codelist.intake.integration;

import javax.inject.Inject;
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

import fi.vm.yti.codelist.common.model.CodeRegistry;
import fi.vm.yti.codelist.intake.ContentIntakeServiceApplication;
import fi.vm.yti.codelist.intake.jpa.CodeRegistryRepository;
import fi.vm.yti.codelist.intake.jpa.CodeSchemeRepository;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ContentIntakeServiceApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test"})
@TestPropertySource(locations = "classpath:test-port.properties")
public class CodeSchemeResourceT3 extends AbstractIntegrationTestBase {

    private static final int CODE_SCHEME_COUNT = 13;
    private static final String NOT_FOUND_REGISTRY_CODEVALUE = "notfoundregistry";
    public static final String TEST_CODESCHEME_FILENAME = "v1_testcodeschemes.csv";
    private TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    private int randomServerPort;

    @Inject
    private CodeRegistryRepository codeRegistryRepository;

    @Inject
    private CodeSchemeRepository codeSchemeRepository;

    @Test
    @Transactional
    public void postCodeSchemesToCodeRegistryTest() {
        final String apiUrl = createApiUrl(randomServerPort, API_PATH_CODEREGISTRIES) + TEST_CODEREGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/" + "?format=" + FORMAT_CSV;
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final String registryFilePath = "/" + CODESCHEMES_FOLDER_NAME + "/" + TEST_CODESCHEME_FILENAME;
        parameters.add("file", new ClassPathResource(registryFilePath));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class, "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        final CodeRegistry codeRegistry = codeRegistryRepository.findByCodeValue(TEST_CODEREGISTRY_CODEVALUE);
        assertNotNull(codeRegistry);
        assertEquals(CODE_SCHEME_COUNT, codeSchemeRepository.findByCodeRegistry(codeRegistry).size());
    }

    @Test
    @Transactional
    public void postCodeSchemesToNotExistingCodeRegistryTest() {
        final String apiUrl = createApiUrl(randomServerPort, API_PATH_CODEREGISTRIES) + NOT_FOUND_REGISTRY_CODEVALUE + API_PATH_CODESCHEMES + "/" + "?format=" + FORMAT_CSV;
        final LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        final String registryFilePath = "/" + CODESCHEMES_FOLDER_NAME + "/" + TEST_CODESCHEME_FILENAME;
        parameters.add("file", new ClassPathResource(registryFilePath));
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        final HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(parameters, headers);
        final ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class, "");
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode());
    }
}
