package pl.homeportal.commons.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;

/**
 * Created by Grzegorz Wrażeń on 28-02-2021 at 18:29
 */

public class HomeportalValidationException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "[ERR-HP-VAL]: ";
    private static final String CONSTRAINT_VIOLATIONS_HEADER = "Constraint violations:";

    @Getter
    private Collection<HomeportalValidationException.Violation> violations = null;

    public HomeportalValidationException(String message)
    {
        super(message);
    }

    public HomeportalValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HomeportalValidationException(Collection<HomeportalValidationException.Violation> violations)
    {
        super();
        this.violations = violations;
    }

    @Override
    public String getMessage()
    {
        if (isNull(violations))
        {
            return ERROR_CODE.concat(String.valueOf(super.getMessage()));
        }

        final StringBuffer message = new StringBuffer();
        message.append(lineSeparator());
        message.append(ERROR_CODE);
        message.append(CONSTRAINT_VIOLATIONS_HEADER);
        message.append(lineSeparator());
        violations.forEach(v -> addMessage(message, v));

        return message.toString();
    }

    private StringBuffer addMessage(StringBuffer message, HomeportalValidationException.Violation v)
    {
        message.append(v.message());
        message.append(lineSeparator());

        return message;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Violation
    {
        private static final String VIOLATION_MESSAGE = "Violation for field: '%s' with value: '%s', message: '%s'";

        private String field;
        private Object value;
        private String message;

        public String message()
        {
            return format(VIOLATION_MESSAGE, field, value, message);
        }
    }
}
