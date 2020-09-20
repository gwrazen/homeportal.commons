package pl.homeportal.commons.collections;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Created by Grzegorz Wrazen on 20-09-2020
 */

public class CollectionUtils
{
    public static Set<String> asSet(String... parameters)
    {
        Set<String> parameterSet = new HashSet<>(parameters.length);
        parameterSet.addAll(asList(parameters));
        return parameterSet;
    }
}
