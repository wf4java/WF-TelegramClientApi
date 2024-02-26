package wf.utils.telegram_client_api.models.exception;

public class TelegramClientRequestException extends RuntimeException {
    public TelegramClientRequestException() {
    }

    public TelegramClientRequestException(String message) {
        super(message);
    }

    public TelegramClientRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public TelegramClientRequestException(Throwable cause) {
        super(cause);
    }

    public TelegramClientRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
