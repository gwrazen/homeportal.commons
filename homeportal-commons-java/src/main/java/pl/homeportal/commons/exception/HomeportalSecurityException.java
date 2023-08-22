package pl.homeportal.commons.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Grzegorz Wrażeń on 22-08-2023 at 12:01
 */

@Getter
@RequiredArgsConstructor
public class HomeportalSecurityException extends RuntimeException
{
    private final String user;
    private final String role;
    private final String action;
    private final String resource;

    @Override
    public String getMessage()
    {
        return new StringBuilder()
                .append("User: '")
                .append(user)
                .append("' with role: '")
                .append(role)
                .append("' is trying to execute: '")
                .append(action)
                .append("' on resource: '")
                .append(resource)
                .append("'")
                .toString();
    }
}