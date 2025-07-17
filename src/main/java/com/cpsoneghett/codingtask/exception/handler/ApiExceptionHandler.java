package com.cpsoneghett.codingtask.exception.handler;


import com.cpsoneghett.codingtask.exception.BusinessException;
import com.cpsoneghett.codingtask.exception.CustomError;
import com.cpsoneghett.codingtask.exception.DeviceInUseException;
import com.cpsoneghett.codingtask.exception.DeviceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.github.fge.jsonpatch.JsonPatchException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public ApiExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    private static Problem getProblemBuilder(HttpStatusCode status, ProblemType problemType, List<CustomError> errors) {
        return Problem.builder().timestamp(LocalDateTime.now()).status(status.value()).type(problemType.getUri()).title(problemType.getTitle()).errors(errors).build();
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        String userMessage = String.format("The resource %s you tried to find is not existent.", ex.getRequestURL());
        String detail = ex.toString();

        Problem error = getProblemBuilder(status, ProblemType.RESOURCE_NOT_FOUND, List.of(new CustomError(userMessage, detail)));

        return handleExceptionInternal(ex, error, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        List<CustomError> errors = getCustomErrors(ex.getBindingResult());

        Problem error = getProblemBuilder(status, ProblemType.BUSINESS_ERROR, errors);

        return handleExceptionInternal(ex, error, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.BUSINESS_ERROR;

        List<CustomError> errors = new ArrayList<>();

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String detailMessage = violation.toString();
            String userMessage = violation.getMessageTemplate();

            errors.add(new CustomError(userMessage, detailMessage));
        }

        Problem problem = getProblemBuilder(status, problemType, errors);

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        String userMessage = "Invalid request body format or content.";

        Throwable mostSpecificCause = ex.getMostSpecificCause();
        if (mostSpecificCause instanceof UnrecognizedPropertyException ue) {
            userMessage = String.format("Unrecognized field '%s' found in request body. " + "Please ensure the JSON matches the expected structure.", ue.getPropertyName());

            if ("createdAt".equals(ue.getPropertyName())) {
                userMessage = "The field 'createdAt' is automatically managed and cannot be provided in the request body.";
            }
        } else if (mostSpecificCause instanceof JsonPatchException) {
            userMessage = "Invalid request body format or content.";
        } else if (mostSpecificCause.getMessage() != null) {
            userMessage = "Failed to parse request body: " + mostSpecificCause.getMessage();
        }

        Problem problem = getProblemBuilder(HttpStatus.BAD_REQUEST, ProblemType.INCOMPREHENSIVE_MESSAGE, List.of(new CustomError(userMessage, ex.toString())));

        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({JsonPatchException.class, JsonProcessingException.class})
    public ResponseEntity<Object> handleJsonPatchException(Exception ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.INCOMPREHENSIVE_MESSAGE;

        String detailMessage = ex.toString();
        String userMessage = ex.getLocalizedMessage();

        Problem problem = getProblemBuilder(status, problemType, List.of(new CustomError(userMessage, detailMessage)));

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex, WebRequest request) {

        HttpStatus status;

        if (ex instanceof DeviceNotFoundException) status = HttpStatus.NOT_FOUND;
        else if (ex instanceof DeviceInUseException) status = HttpStatus.CONFLICT;
        else status = HttpStatus.BAD_REQUEST;

        ProblemType problemType = ProblemType.BUSINESS_ERROR;

        String detailMessage = ex.toString();
        String userMessage = ex.getLocalizedMessage();

        Problem problem = getProblemBuilder(status, problemType, List.of(new CustomError(userMessage, detailMessage)));

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    private List<CustomError> getCustomErrors(BindingResult bindingResult) {
        List<CustomError> errors = new ArrayList<>();

        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String userMessage = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
            String detailMessage = fieldError.toString();
            errors.add(new CustomError(userMessage, detailMessage));
        }
        return errors;
    }


}
