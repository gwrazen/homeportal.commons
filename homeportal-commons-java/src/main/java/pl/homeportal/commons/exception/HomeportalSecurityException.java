package pl.homeportal.commons.exception;

import lombok.Getter;

/**
 * Created by Grzegorz Wrażeń on 22-08-2023 at 12:01
 */

@Getter
public class HomeportalSecurityException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private static final String ERROR_CODE = "[ERR-HP-SEC]: ";

    private final String user;
    private final String role;
    private final String action;
    private final String resource;

    public HomeportalSecurityException(String user, String role, String action, String resource)
    {
        this.user = user;
        this.role = role;
        this.action = action;
        this.resource = resource;
    }

    /**
     * Wariant dla bledow bezpieczenstwa niezwiazanych z proba dostepu (np. brak
     * algorytmu kryptograficznego) — wczesniej takie przypadki nie mialy jak
     * przeniesc przyczyny.
     */
    public HomeportalSecurityException(String message, Throwable cause)
    {
        super(message, cause);
        this.user = null;
        this.role = null;
        this.action = null;
        this.resource = null;
    }

    @Override
    public String getMessage()
    {
        if (user == null && role == null && action == null && resource == null)
        {
            return ERROR_CODE.concat(String.valueOf(super.getMessage()));
        }

        return new StringBuilder()
                .append(ERROR_CODE)
                .append("User '")
                .append(user)
                .append("' with role '")
                .append(role)
                .append("' is trying to execute '")
                .append(action)
                .append("' on resource '")
                .append(resource)
                .append("'")
                .toString();
    }
}
