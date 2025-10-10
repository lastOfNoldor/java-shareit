package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@Slf4j
@RestControllerAdvice
public class CentralExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataIntegrityViolationException(final DataIntegrityViolationException e) {
        String errorMessage = extractConstraintViolationMessage(e);

        log.warn("Нарушение целостности данных: {}", errorMessage, e);
        return new ErrorResponse(errorMessage);
    }

    private String extractConstraintViolationMessage(DataIntegrityViolationException e) {
        Throwable rootCause = e.getRootCause();

        if (rootCause instanceof SQLException) {
            String sqlMessage = rootCause.getMessage();

            if (sqlMessage != null) {
                if (sqlMessage.contains("end_date > start_date") || sqlMessage.contains("CONSTRAINT_A62") || sqlMessage.contains("check constraint")) {
                    return "Дата окончания бронирования должна быть позже даты начала";
                }
            }
        }
        return "Нарушение целостности данных: проверьте корректность введенных данных";
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleValidationException(final NotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final IllegalArgumentException e) {
        log.warn("Данные не верны: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationException(final ConflictException e) {
        log.warn("Данные не верны: {}", e.getMessage(), e);
        return new ErrorResponse(e.getMessage());
    }
}
