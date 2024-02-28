package pl.homeportal.commons.text;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class StringUtilsTest
{
    @Test
    public void normalize()
    {
        // given
        final String text = "Śląskieńźżó -   S.A.//";
        final String expected = "slaskienzzosa";

        // when
        String actual = StringUtils.normalize(text);

        // then
        assertThat(actual, equalTo(expected));
    }
}