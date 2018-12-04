package pl.homeportal.mail.promotion;

import pl.homeportal.i18n.Language;
import pl.homeportal.model.entities.Offer;
import pl.homeportal.model.entities.OfferIdentity;
import pl.homeportal.model.entities.Promotion;
import pl.homeportal.model.entities.User;
import pl.homeportal.model.statics.Activity;
import pl.homeportal.model.statics.IdentityConstant;
import pl.homeportal.model.statics.Product;
import pl.homeportal.model.statics.UserRole;

/**
 * Created by gwrazen on 07/02/2015.
 */
public class PromotionEndDTO
{
    private final Promotion promotion;

    public PromotionEndDTO(Promotion promotion)
    {
        this.promotion = promotion;
    }

    public String getName()
    {
        if (promotion.getOffer().getUser().getRole() == UserRole.INDIVIDUAL)
        {
            return promotion.getOffer().getUser().getName();
        }
        else
        {
            return promotion.getOffer().getUser().getDepartment().getCompany().getName();
        }
    }

    public String getOfferId()
    {
        return getOfferId(promotion.getOffer());
    }

    public String getEmail()
    {
        if (promotion.getOffer().getUser().getRole() == UserRole.INDIVIDUAL)
        {
            return promotion.getOffer().getUser().getEmail();
        }
        else
        {
            return promotion.getOffer().getUser().getDepartment().getCompany().getEmail();
        }
    }

    public Language getLanguage()
    {
        return getLanguage(promotion.getOffer().getUser());
    }

    public int getPoints()
    {
        return getPoints(promotion.getOffer().getUser());
    }

    public String getLink()
    {
        return getLink(promotion.getOffer());
    }

    public boolean isManager()
    {
        return (promotion.getOffer().getUser().getRole() == UserRole.MANAGER);
    }

    public String getPromoteLink()
    {
        return "http://homeportal.pl/account/promotions/promote/?offerId=" + promotion.getOffer().getId();
    }

    private String getOfferId(Offer offer)
    {
        OfferIdentity identity = offer.getIdentityMap().get(IdentityConstant.EXTERNAL_OFFER_IDENTITY);
        if (identity != null)
        {
            return identity.getValue();
        }

        if (identity == null)
        {
            identity = offer.getIdentityMap().get(IdentityConstant.HOMEPORTAL_IDENTITY);
            return identity.getValue();
        }

        return null;
    }

    private Language getLanguage(User user)
    {
        if (user.getRole() == UserRole.MANAGER || user.getRole() == UserRole.AGENT)
        {
            return user.getDepartment().getCompany().getLanguage();
        }
        else
        {
            return user.getLanguage();
        }
    }

    private String getLink(Offer offer)
    {
        StringBuilder link = new StringBuilder();

        String action = getAction(offer);
        if (action != null && !action.equals(""))
        {
            link.append(action);
        }

        String type = getType(offer);
        if (type != null && !type.equals(""))
        {
            link.append(type);
        }

        String ID = getExternalIdentity(offer) != null ? getExternalIdentity(offer) : String.valueOf(offer.getId());

        link.append("/?id=" + ID);

        return link.toString().replaceAll("--", "-");
    }

    private String getExternalIdentity(Offer offer)
    {

        if (offer != null && offer.getIdentityMap().get(IdentityConstant.EXTERNAL_OFFER_IDENTITY) != null)
        {
            return offer.getIdentityMap().get(IdentityConstant.EXTERNAL_OFFER_IDENTITY).getValue();
        }

        return null;
    }

    private String getAction(Offer offer)
    {
        if (offer == null)
            return null;

        if (offer.getActivity().equals(Activity.SALE))
        {
            return "/sprzedaz";
        }
        else if (offer.getActivity().equals(Activity.RENT))
        {
            return "/wynajem";
        }

        return null;
    }

    private String getType(Offer offer)
    {
        if (offer == null)
            return null;

        if (offer.getProduct().equals(Product.HOUSE))
        {
            return "/dom";
        }
        else if (offer.getProduct().equals(Product.OFFICE))
        {
            return "/lokal";
        }
        else if (offer.getProduct().equals(Product.HALL))
        {
            return "/hala";
        }
        else if (offer.getProduct().equals(Product.APARTMENT))
        {
            return "/mieszkanie";
        }
        else if (offer.getProduct().equals(Product.LAND))
        {
            return "/dzialka";
        }

        return null;
    }

    private int getPoints(User user)
    {
        if (user.getRole() == UserRole.INDIVIDUAL)
        {
            return user.getPoints();
        }
        else
        {
            return user.getDepartment().getCompany().getPoints();
        }
    }


}
