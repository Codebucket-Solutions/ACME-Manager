/*
 *    Copyright 2024, Codebucket Solutions Private Limited
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package in.codebuckets.acmemanager.server;

import com.fasterxml.jackson.databind.JsonMappingException;
import in.codebuckets.acmemanager.common.json.Responses;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.function.Function;

import static in.codebuckets.acmemanager.common.json.Jackson.toJson;
import static java.util.Collections.singletonMap;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JsonMappingException.class)
    @ResponseBody
    public ResponseEntity<String> handleJsonMappingException(JsonMappingException ex) {
        String message;
        if (!ex.getPath().isEmpty()) {
            message = "Missing / Invalid value for field: " + ex.getPath().get(0).getFieldName();
        } else {
            message = "Invalid JSON request body";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return ResponseEntity.status(BAD_REQUEST).headers(headers).body(toJson(singletonMap("error", message)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getMessage();
        String compactMessage = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return ResponseEntity.status(BAD_REQUEST).headers(headers).body(toJson(singletonMap("error", compactMessage)));
    }

    /**
     * Generic exception handler for all exceptions thrown by the application.
     *
     * <p></p>
     * Special handling for the following exceptions:
     * <ul>
     *     <li> {@link ServiceException} </li>
     * </ul>
     */
    public static final Function<Throwable, ResponseEntity<String>> GENERIC_EXCEPTION_HANDLER = throwable -> {
        if (throwable.getCause() instanceof ServiceException) {
            String message = throwable.getMessage();
            return Responses.badRequest(message);
        } else {
            throwable.printStackTrace();
            return Responses.internalServerError(throwable.getCause().getMessage());
        }
    };
}
