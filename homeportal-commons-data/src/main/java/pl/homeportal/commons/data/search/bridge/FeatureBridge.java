package pl.homeportal.commons.data.search.bridge;

import org.hibernate.search.bridge.builtin.StringBridge;
import pl.homeportal.commons.data.model.feature.FeatureConstants;

public class FeatureBridge extends StringBridge
{
    private static final String SPECIAL_CHARACTERS = "[^\\p{L}\\p{Nd}\\s]+";
    private static final String MORE_THAN_ONE_SPACE = "\\s{2,}";

    @Override
    public String objectToString(Object object)
    {
        if ( null == object )
        {
            return null;
        }

        String original = (String)object;
        if(original.trim().length() == 0)
        {
            return null;
        }

        if(original.replaceAll(SPECIAL_CHARACTERS, "").trim().length() == 0)
        {
            return null;
        }

        StringBuilder features = new StringBuilder();
        for (String feature : original.split(getSeparator()))
        {
            int index =  feature.indexOf(FeatureConstants.NAME_SEPARATOR);
            if(index != -1)
            {
                feature = feature.substring(index + 1);
                feature = feature.toLowerCase();
                feature = feature.replaceAll(SPECIAL_CHARACTERS, "");
                feature = feature.replaceAll(MORE_THAN_ONE_SPACE, " ");
                features.append(feature);
                features.append(" ");
            }
        }
        return features.toString().trim();
    }

    private String getSeparator()
    {
        return "\\" + FeatureConstants.FEATURE_SEPARATOR;
    }
}
