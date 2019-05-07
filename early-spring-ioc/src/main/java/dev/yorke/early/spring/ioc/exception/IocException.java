package dev.yorke.early.spring.ioc.exception;

/**
 * @author Yorke
 */
public class IocException extends RuntimeException {

    public IocException(String message) {
        super(message);
    }

    public IocException(String message, Throwable cause) {
        super(message, cause);
    }

    public IocException(Throwable cause) {
        super(cause);
    }
}
