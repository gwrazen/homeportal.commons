package pl.homeportal.commons.mail;

import org.junit.Test;
import pl.homeportal.commons.i18n.Language;

import java.util.Collections;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertFalse;

/**
 * Kontrakt {@link NotifierAdapter#notifyChecked(BaseDTO)}: metoda ma ZWRACAC wynik zamiast go
 * polykac, bo na jej wyniku stoi stemplowanie daty wyslania maila po stronie aplikacji.
 *
 * Pokryte sa dwie sciezki konczace sie {@code false} — one psuja sie po cichu. Sciezka udanej
 * wysylki wymaga dzialajacego SMTP i szablonu Velocity, wiec zostaje na weryfikacji recznej.
 *
 * Created by Grzegorz Wrazen on 11-08-2026
 */

public class NotifierAdapterTest
{
    private static final String TEMPLATE_NAME = "template/test.vm";
    private static final String SUBJECT_KEY = "email.subject.test";
    private static final String SENDER = "test@homeportal.pl";
    private static final String DOMAIN = "homeportal.pl";
    private static final String RECEIVER = "receiver@homeportal.pl";

    @Test
    public void shouldNotSendWhenNotifierIsDisabled()
    {
        final TestNotifier notifier = new TestNotifier(false);

        assertFalse(notifier.notifyChecked(new TestDTO()));
    }

    /**
     * Brak {@code messageSource} wywraca budowanie maila — dla testu wazne jest to, ze wyjatek
     * konczy sie wynikiem {@code false}, a nie leci do wolajacego.
     */
    @Test
    public void shouldReturnFalseWhenSendingBlowsUp()
    {
        final TestNotifier notifier = new TestNotifier(true);

        assertFalse(notifier.notifyChecked(new TestDTO()));
    }

    private static class TestNotifier extends NotifierAdapter<TestDTO>
    {
        private final boolean enabled;

        private TestNotifier(boolean enabled)
        {
            this.enabled = enabled;
        }

        @Override
        protected EmailTemplate template()
        {
            return () -> TEMPLATE_NAME;
        }

        @Override
        public boolean isEnabled()
        {
            return enabled;
        }

        @Override
        public String senderEmail()
        {
            return SENDER;
        }
    }

    private static class TestDTO extends BaseDTO
    {
        private TestDTO()
        {
            super(DOMAIN, Language.POLISH, singleton(RECEIVER), Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        }

        @Override
        public String subjectKey()
        {
            return SUBJECT_KEY;
        }
    }
}
