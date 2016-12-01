package org.wowtools.dao;
/**
 * jdbc运行时异常
 * @author liuyu
 *
 */
public class DaoRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -3358017326252670602L;

	public DaoRuntimeException() {
        super();
    }

    public DaoRuntimeException(String message) {
        super(message);
    }

    public DaoRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoRuntimeException(Throwable cause) {
        super(cause);
    }

    protected DaoRuntimeException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
