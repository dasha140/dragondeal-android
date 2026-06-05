package com.elvsf.dragondeal.dto;

import java.util.List;

public class BackendError {

    private Integer status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> errors;

    /**
     * Convenience constructor for wrapping a user-friendly message.
     */
    public BackendError(String message) {
        this.message = message;
        this.errors = null;
    }

    public BackendError() {
    }

    public Integer getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public static class FieldError {
        private String field;
        private String message;

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
