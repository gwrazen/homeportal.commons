package pl.homeportal.commons.reflection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClassFieldReaderTest
{
    private static final String FIELD_A = "fieldA";
    private static final String FIELD_B = "fieldB";
    private static final String FIELD_C = "fieldC";
    private static final String STATIC_FIELD = "staticField";
    private static final String BASE_FIELD = "baseField";
    private static final String CHILD_FIELD = "childField";

    private static final String FIELD_A_VALUE = "fieldA";
    private static final int FIELD_B_VALUE = 1;
    private static final Object FIELD_C_VALUE = "fieldC";
    private static final String BASE_FIELD_VALUE = "base";
    private static final String CHILD_FIELD_VALUE = "child";

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

    /**
     * Regresja: Collectors.toMap opiera sie na Map.merge, ktory odrzuca wartosci null,
     * wiec dowolny obiekt z niewypelnionym polem konczyl sie NPE. Konfiguracje aplikacji
     * (portal/hop/importer) loguja przez ta metode pola @Value, ktore bywaja puste.
     */
    @Test
    public void testLogFieldsValuesWithNullField()
    {
        // given
        TestClass instance = TestClass.of(null, FIELD_B_VALUE, null);

        // when
        Map<String, ?> map = ClassFieldReader.readFieldValues(instance);

        // then
        assertTrue(map.containsKey(FIELD_A));
        assertNull(map.get(FIELD_A));
        assertNull(map.get(FIELD_C));
    }

    @Test
    public void testLogInheritedFieldsValues()
    {
        // given
        ChildTestClass instance = new ChildTestClass(BASE_FIELD_VALUE, CHILD_FIELD_VALUE);

        // when
        Map<String, ?> map = ClassFieldReader.readFieldValues(instance);

        // then
        assertEquals(map.get(BASE_FIELD), BASE_FIELD_VALUE);
        assertEquals(map.get(CHILD_FIELD), CHILD_FIELD_VALUE);
    }

    @Test
    public void testStaticFieldsAreSkipped()
    {
        // given
        TestClass instance = TestClass.of(FIELD_A_VALUE, FIELD_B_VALUE, FIELD_C_VALUE);

        // when
        Map<String, ?> map = ClassFieldReader.readFieldValues(instance);

        // then
        assertFalse(map.containsKey(STATIC_FIELD));
    }

    public static class BaseTestClass
    {
        private String baseField;

        BaseTestClass(String baseField)
        {
            this.baseField = baseField;
        }
    }

    public static class ChildTestClass extends BaseTestClass
    {
        private String childField;

        ChildTestClass(String baseField, String childField)
        {
            super(baseField);
            this.childField = childField;
        }
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class TestClass
    {
        static final String staticField = "static";

        private String fieldA;
        private int fieldB;
        private Object fieldC;
    }

    public static class ExtendedTestClass extends TestClass
    {
        private String fieldD;

        ExtendedTestClass(String fieldA, int fieldB, Object fieldC, String fieldD)
        {
            super(fieldA, fieldB, fieldC);
            this.fieldD = fieldD;
        }
    }
}

