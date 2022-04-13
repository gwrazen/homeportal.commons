package pl.homeportal.commons.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Objects.isNull;
import static pl.homeportal.commons.text.Constants.EMPTY_STRING;

/**
 * Created by Grzegorz Wrazen on 04-03-2017
 */

public class HomeportalServiceException extends RuntimeException
{
    private static final String CONSTRAINT_VIOLATIONS_HEADER = "Constraint violations:";

    @Getter
    private Collection<Violation> violations = null;

    public HomeportalServiceException(String message)
    {
        super(message);
    }

    public HomeportalServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HomeportalServiceException(Collection<Violation> violations)
    {
        super();
        this.violations = violations;
    }

    @Override
    public String getMessage()
    {
        if (isNull(violations))
        {
            return super.getMessage();
        }

        final StringBuffer message = new StringBuffer();
        message.append(lineSeparator());
        message.append(CONSTRAINT_VIOLATIONS_HEADER);
        message.append(lineSeparator());
        violations.forEach(v -> addMessage(message, v));
//        removeLastLine(message);

        return message.toString();
    }

    private StringBuffer addMessage(StringBuffer message, Violation v)
    {
        message.append(v.message());
        message.append(lineSeparator());

        return message;
    }

    private void removeLastLine(StringBuffer message)
    {
        int start = message.length() - 1;
        int end = message.length();
        message.replace(start, end, EMPTY_STRING);
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