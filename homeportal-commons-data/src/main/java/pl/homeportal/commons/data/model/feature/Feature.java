package pl.homeportal.commons.data.model.feature;

public class Feature
{
    FeatureType featureType;
    Object value;

    public Feature(FeatureType featureType, Object value)
    {
        this.featureType = featureType;
        this.value = value;
    }

    public String asString()
    {
        return FeatureConstants.FEATURE_SEPARATOR + featureType.getName() + FeatureConstants.NAME_SEPARATOR + value + FeatureConstants.FEATURE_SEPARATOR;
    }
}
