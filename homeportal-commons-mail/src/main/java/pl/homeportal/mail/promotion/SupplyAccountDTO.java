package pl.homeportal.mail.promotion;

import pl.homeportal.i18n.Language;
import pl.homeportal.model.entities.Company;
import pl.homeportal.model.entities.User;

/**
 * Created by gwrazen on 10/02/2015.
 */
public class SupplyAccountDTO
{
    private final Company company;
    private final User user;
    private final int amount;

    public SupplyAccountDTO(Company company, int amount)
    {
        this.company = company;
        this.amount = amount;
        this.user = null;
    }

    public SupplyAccountDTO(User user, int amount)
    {
        this.user = user;
        this.amount = amount;
        this.company = null;
    }

    public String getName()
    {
        if (company != null)
            return company.getName();

        return user.getName();
    }

    public Language getLanguage()
    {
        if (company != null)
            return company.getLanguage();

        return user.getLanguage();
    }

    public String getEmail()
    {
        if (company != null)
            return company.getEmail();

        return user.getEmail();
    }

    public int getAmount()
    {
        return amount;
    }

    public int getPoints()
    {
        if (company != null)
            return company.getPoints();

        return user.getPoints();
    }
}
