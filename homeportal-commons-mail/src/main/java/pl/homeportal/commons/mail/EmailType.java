package pl.homeportal.commons.mail;

/**
 * Created with IntelliJ IDEA.
 * User: gwrazen
 * Date: 11/01/14
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */
public enum EmailType
{
    IMPORT_REPORT("mail/importReport.vm"),
    USER_REGISTRATION("mail/registerUser.vm"),
    PASSWORD_RESET("mail/resetPasswordReqLink.vm"),
    NEW_PASSWORD("mail/resetPasswordNewPassword.vm"),
    QUESTION("mail/question.vm"),
    PROMOTION_START("mail/promotionStart.vm"),
    PROMOTION_END("mail/promotionEnd.vm"),
    SUPPLY_ACCOUNT("mail/supplyAccount.vm"),
    PORTALS_OFFERS("mail/portalsOffers.vm"),
    PORTALS_ACTIVATION("mail/portalsActivation.vm"),
    PORTALS_DEACTIVATION("mail/portalsDeactivation.vm"),
    EXPORTS_DEACTIVATION("mail/exportsDeactivation.vm"),
    EXPORT_REPORT("mail/exportReport.vm");

    private String templateName;

    EmailType(String templateName)
    {
        this.templateName = templateName;
    }

    public String getTemplateName()
    {
        return templateName;
    }
}
