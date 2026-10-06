package pe.edu.ulima.is2.bustracka.dto;

import java.time.LocalDateTime;

/** Formato uniforme de error para toda la API (usado por GlobalExceptionHandler). */
public class ApiErrorResponseDTO {

    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private String message;

    public ApiErrorResponseDTO() {
    }

    public ApiErrorResponseDTO(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
