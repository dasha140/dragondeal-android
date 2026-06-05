package com.elvsf.dragondeal.util;

import android.text.TextUtils;

import com.elvsf.dragondeal.dto.BackendError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import retrofit2.Response;

public class BackendErrorParser {

    private static final Gson GSON = new Gson();

    /**
     * Parse a Retrofit error Response into a user-friendly Russian message.
     */
    public static String parse(Response<?> response) {
        if (response == null) {
            return "Произошла ошибка. Попробуйте позже.";
        }

        int code = response.code();

        /* --- 503/500: prefer backend message, fallback to hardcoded --- */
        if (code == 503 || code == 500) {
            BackendError error = tryParseErrorBody(response);
            if (error != null && !isBlank(error.getMessage())) {
                return error.getMessage();
            }
            return code == 503
                    ? "Сервис отправки писем временно недоступен. Попробуйте позже."
                    : "На сервере произошла ошибка. Попробуйте позже.";
        }
        if (code == 401 || code == 403) {
            return "Войдите в аккаунт и попробуйте снова.";
        }

        /* --- try to extract structured error from body --- */
        BackendError error = tryParseErrorBody(response);

        if (error != null) {
            // 404 with message
            if (code == 404 && !isBlank(error.getMessage())) {
                return error.getMessage();
            }

            // validation errors (400) — prefer errors[] list
            List<BackendError.FieldError> fieldErrors = error.getErrors();
            if (fieldErrors != null && !fieldErrors.isEmpty()) {
                return formatFieldErrors(fieldErrors);
            }

            // single message field
            if (!isBlank(error.getMessage())) {
                return error.getMessage();
            }
        }

        /* --- fallback --- */
        if (code >= 400 && code < 500) {
            return "Неверный запрос. Проверьте данные и попробуйте снова.";
        }

        return "Произошла ошибка. Попробуйте позже.";
    }

    /**
     * Parse a Throwable (from onFailure) into a user-friendly Russian message.
     */
    public static String parse(Throwable t) {
        if (t == null) {
            return "Произошла ошибка. Попробуйте позже.";
        }
        if (t instanceof SocketTimeoutException) {
            return "Нет соединения с сервером.";
        }
        if (t instanceof IOException) {
            return "Нет соединения с сервером.";
        }
        return "Произошла ошибка. Попробуйте позже.";
    }

    /* --------------- private helpers --------------- */

    private static BackendError tryParseErrorBody(Response<?> response) {
        try {
            if (response.errorBody() == null) {
                return null;
            }
            String raw = response.errorBody().string();
            if (isBlank(raw)) {
                return null;
            }
            BackendError error = GSON.fromJson(raw, BackendError.class);

            // Detect auth errors that come as HTTP 400 (IllegalArgumentException)
            if (error != null && !isBlank(error.getMessage())) {
                String msg = error.getMessage().toLowerCase();
                if (msg.contains("invalid or expired token")
                        || msg.contains("authorization token is required")
                        || msg.contains("unauthorized")) {
                    return new BackendError("Сессия истекла. Войдите в аккаунт снова.");
                }
            }

            return error;
        } catch (JsonSyntaxException | IOException ignored) {
            return null;
        }
    }

    private static String formatFieldErrors(List<BackendError.FieldError> fieldErrors) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldErrors.size(); i++) {
            BackendError.FieldError fe = fieldErrors.get(i);
            if (i > 0) {
                sb.append("\n");
            }
            if (!isBlank(fe.getMessage())) {
                sb.append(fe.getMessage());
            } else if (!isBlank(fe.getField())) {
                sb.append(fe.getField());
            }
        }
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || TextUtils.getTrimmedLength(s) == 0;
    }
}
