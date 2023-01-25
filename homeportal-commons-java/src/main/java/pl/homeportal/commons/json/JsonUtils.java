package pl.homeportal.commons.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class JsonUtils
{
    public static String readJsonFromFile(Class aClass, String fileName) throws IOException
    {
        URL url = Resources.getResource(aClass, fileName);
        return Files.asCharSource(new File(url.getFile()), Charsets.UTF_8).read();
    }

    public static <T> T readFromJsonResource(Class aClass, String resourceName, Class<T> valueType)
    {
        return readFromJsonResource(Resources.getResource(aClass, resourceName), valueType);
    }

    public static <T> T readFromJsonResource(URL url, Class<T> valueType)
    {
        return readFromJsonResource(url, valueType, new ObjectMapper());
    }

    public static <T> T readFromJsonResource(URL url, Class<T> valueType, ObjectMapper objectMapper)
    {
        try
        {
            return objectMapper.readValue(url, valueType);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
