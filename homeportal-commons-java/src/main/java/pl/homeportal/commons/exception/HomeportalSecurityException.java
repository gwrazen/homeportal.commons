package pl.homeportal.commons.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Created by Grzegorz Wrażeń on 22-08-2023 at 12:01
 */

@Getter
@RequiredArgsConstructor(staticName = "of")
public class HomeportalSecurityException extends RuntimeException
{
    private final String user;
    private final String action;
    private final String resource;

    public String message()
    {
        return new StringBuilder()
                .append("User: '")
                .append(user)
                .append("' is trying to execute: '")
                .append(action)
                .append("' on resource: '")
                .append(resource)
                .append("'")
                .toString();
    }
}