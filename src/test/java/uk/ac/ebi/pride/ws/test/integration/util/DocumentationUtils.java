package uk.ac.ebi.pride.ws.test.integration.util;

import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.util.MultiValueMap;


public class DocumentationUtils {

    public static OperationPreprocessor maskTokenPreProcessor() {
        return new PasswordMaskingPreprocessor();
    }

    private static class PasswordMaskingPreprocessor implements OperationPreprocessor {

        @Override
        public OperationRequest preprocess(OperationRequest request) {

            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> headersMap = new HttpHeaders();
            request.getHeaders().forEach(headersMap::put);
            headersMap.set("Authorization", "Bearer ***AUTH_TOKEN***");
            headers.addAll(headersMap);
            return new OperationRequestFactory().create(request.getUri(),
                    request.getMethod(), request.getContent(), headers,
                    request.getParts());
        }

        @Override
        public OperationResponse preprocess(OperationResponse response) {
            return response;
        }

    }

}
