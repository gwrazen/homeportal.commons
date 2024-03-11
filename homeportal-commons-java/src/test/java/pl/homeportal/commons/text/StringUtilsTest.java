package pl.homeportal.commons.text;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.homeportal.commons.text.Constants.DASH;

public class StringUtilsTest
{
    @Test
    public void normalize()
    {
        // given
        final String text = "Śląskieńźżół -   S.A.//ężź";
        final String expected = "slaskienzzolsaezz";

        // when
        String actual = StringUtils.normalize(text);

        // then
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void normalize_dash()
    {
        // given
        final String text = "Śląskieńźżół -   S.A.//ężź";
        final String expected = "slaskienzzol-s-a-ezz";

        // when
        String actual = StringUtils.normalize(text, DASH);

        // then
        assertThat(actual, equalTo(expected));
    }
}