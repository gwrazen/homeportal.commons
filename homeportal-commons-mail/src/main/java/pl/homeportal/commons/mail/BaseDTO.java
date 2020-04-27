package pl.homeportal.commons.mail;

import lombok.Getter;
import pl.homeportal.commons.i18n.Language;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

/**
 * Created by Grzegorz Wrażeń on 24-02-2020
 */

@Getter
public abstract class BaseDTO
{
    public static final int INITIAL_CAPACITY = 1;

    private final Language language;
    private Set<String> tos;
    private Set<String> ccs;
    private Set<String> bccs;
    private Set<Attachment> attachments;

    protected BaseDTO(Language language, Collection<String> tos, Collection<String> ccs, Collection<String> bccs, Collection<Attachment> attachments)
    {
        this.language = language;
        addAllTos(tos);
        addAllCcs(ccs);
        addAllBccs(bccs);
        addAllAttachments(attachments);
    }

    public void addAllTos(Collection<String> emails)
    {
        if (isEmpty(emails))
        {
            return;
        }

        if (tos == null)
        {
            tos = new HashSet<>(emails.size());
        }

        tos.addAll(emails);
    }

    public void addAllCcs(Collection<String> emails)
    {
        if (isEmpty(emails))
        {
            return;
        }

        if (ccs == null)
        {
            ccs = new HashSet<>(emails.size());
        }

        ccs.addAll(emails);
    }

    public void addAllBccs(Collection<String> emails)
    {
        if (isEmpty(emails))
        {
            return;
        }

        if (bccs == null)
        {
            bccs = new HashSet<>(emails.size());
        }

        bccs.addAll(emails);
    }

    public void addAllAttachments(Collection<Attachment> attachments)
    {
        if (isEmpty(attachments))
        {
            return;
        }

        if (this.attachments == null)
        {
            this.attachments = new HashSet<>(attachments.size());
        }

        this.attachments.addAll(attachments);
    }

    public Locale locale()
    {
        return language.locale();
    }

    @Getter
    public static class Attachment
    {
        private String path;
        private String name;
        private boolean resources;

        private Attachment(String path, String name, boolean resources)
        {
            this.path = path;
            this.name = name;
            this.resources = resources;
        }

        public static Attachment of(String path, String name, boolean resources)
        {
            return new Attachment(path, name, resources);
        }
    }
}
