package uk.ac.ebi.pride.ws.test.integration.util;

import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.Parameters;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.util.MultiValueMap;


public class DocumentationUtils {

    public static OperationPreprocessor maskTokenPreProcessor() {
        return new PasswordMaskingPreprocessor();
    }

    private static class PasswordMaskingPreprocessor implements OperationPreprocessor {

        @Override
        public OperationRequest preprocess(OperationRequest request) {

            Parameters parameters = new Parameters();
            parameters.putAll(request.getParameters());
            parameters.set("password", "XXXX");

            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> headersMap = new HttpHeaders();
            request.getHeaders().forEach(headersMap::put);
            headersMap.set("Authorization", "Bearer ***AUTH_TOKEN***");
            headers.addAll(headersMap);
            return new OperationRequestFactory().create(request.getUri(),
                    request.getMethod(), request.getContent(), headers,
                    request.getParameters(), request.getParts());
        }

        @Override
        public OperationResponse preprocess(OperationResponse response) {
            return response;
        }

    }

}
