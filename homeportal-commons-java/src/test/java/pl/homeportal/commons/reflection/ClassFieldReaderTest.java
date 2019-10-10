package pl.homeportal.commons.reflection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ClassFieldReaderTest
{
    private static final String FIELD_A = "fieldA";
    private static final String FIELD_B = "fieldB";
    private static final String FIELD_C = "fieldC";

    private static final String FIELD_A_VALUE = "fieldA";
    private static final int FIELD_B_VALUE = 1;
    private static final Object FIELD_C_VALUE = "fieldC";

    @Test
    public void testLogFieldsValues()
    {
        // given
        TestClass instance = TestClass.of(FIELD_A_VALUE, FIELD_B_VALUE, FIELD_C_VALUE);

        // when
        Map<String, ?> map = ClassFieldReader.readFieldValues(instance);

        // then
        assertEquals(map.get(FIELD_A), FIELD_A_VALUE);
        assertEquals(map.get(FIELD_B), FIELD_B_VALUE);
        assertEquals(map.get(FIELD_C), FIELD_C_VALUE);
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class TestClass
    {
        private String fieldA;
        private int fieldB;
        private Object fieldC;
    }
}

