package pl.homeportal.commons.data.model.feature;


public enum FeatureType
{
    // COMMONS
    PROPERTY_TYPE("PROPERTY_TYPE",true ,String.class),
    PROPERTY_TYPE_LAND("PROPERTY_TYPE_LAND",true ,String.class),
    PROPERTY_TYPE_OFFICE("PROPERTY_TYPE_OFFICE",true ,String.class),
    PROPERTY_TYPE_HALL("PROPERTY_TYPE_HALL",true ,String.class),
    MARKET("MARKET", true, String.class),
    TENURE("TENURE", true, String.class),
    MEDIA("MEDIA", true, String.class),
    HEATING("HEATING", true, String.class),
    WINDOWS("WINDOWS", true, String.class),
    ELEVATOR("ELEVATOR", true, String.class),
    KITCHEN("KITCHEN", true, String.class),
    BATHROOM("BATHROOM", true, String.class),
    PAYMENTS_IN_RENT("PAYMENTS_IN_RENT", true, String.class),
    PAYMENTS_VIA_METERS("PAYMENTS_VIA_METERS", true, String.class),
    BALCONY("BALCONY", true, String.class),
    TERRACE("TERRACE", true, String.class),
    LAND_TYPE("LAND_TYPE", true, String.class),
    LAND_SHAPE("LAND_SHAPE", true, String.class),
    LAND_FENCE("LAND_FENCE", true, String.class),
    DRIVE_ACCESS("DRIVE_ACCESS", true, String.class),
    SURROUNDING("SURROUNDING", true, String.class),
    FLOOR("FLOOR", true, String.class),
    FURNITURE("FURNITURE", true, String.class),
    DEPOSIT_TYPE("DEPOSIT_TYPE", true, String.class),
    ADDITIONALS("ADDITIONALS", true, String.class),
    LEASE_TERM("LEASE_TERM", true, String.class),
    PLACEMENT("PLACEMENT", true, String.class),
    SEWERAGE_SYSTEM("SEWERAGE_SYSTEM", true, String.class),
    
    // APARTMENTS
    APARTMENT("APARTMENT", true, String.class),
    APARTMENT_PERFORMANCE("APARTMENT_PERFORMANCE", true, String.class),
    APARTMENT_TYPE("APARTMENT_TYPE", true, String.class),
    APARTMENT_BUILD_TECHNOLOGY("APARTMENT_BUILD_TECHNOLOGY", true, String.class),
    APARTMENT_GARAGE("APARTMENT_GARAGE", true, String.class),
    APARTMENT_CELLAR("APARTMENT_CELLAR", true, String.class),
    INSTALLATIONS("INSTALLATIONS", true, String.class),
    
    // HOUSE
    HOUSE("HOUSE", true, String.class),
    HOUSE_ROOF_TYPE("HOUSE_ROOF_TYPE", true, String.class),
    HOUSE_PERFORMANCE("HOUSE_PERFORMANCE", true, String.class),
    HOUSE_BUILD_STATE("HOUSE_BUILD_STATE", true, String.class),
    HOUSE_GARAGE("HOUSE_GARAGE", true, String.class),
    HOUSE_CELLAR("HOUSE_CELLAR", true, String.class),
    HOUSE_ELEVATION_TYPE("HOUSE_ELEVATION_TYPE", true, String.class),
    HOUSE_BUILD_MATERIAL("HOUSE_BUILD_MATERIAL", true, String.class),
    
    // LAND
    LAND_TENURE("LAND_TENURE", true, String.class),
    LAND_BUILDING("LAND_BUILDING", true, String.class),
    LAND_BUILDING_CONDITIONS("LAND_BUILDING_CONDITIONS", true, String.class),
    LAND_MANAGEMENT("LAND_MANAGEMENT", true, String.class),
    
    // OFFICE
    OFFICE("OFFICE", true, String.class),
    OFFICE_BUILDING_TYPE("OFFICE_BUILDING_TYPE", true, String.class),
    OFFICE_ADVERTISEMENT_PLACE("OFFICE_ADVERTISEMENT_PLACE", true, String.class),
    OFFICE_PLACEMENT("OFFICE_PLACEMENT", true, String.class),
    OFFICE_EXHIBITION("OFFICE_EXHIBITION", true, String.class),
    
    // HALL
    HALL("HALL", true, String.class),
    HALL_BUILDING_MATERIAL("HALL_BUILDING_MATERIAL", true, String.class),
    HALL_CAR_ACCESS("HALL_CAR_ACCESS", true, String.class),
    
    // OTHERS
    CONTACT("CONTACT", true, String.class),
    OFFER_STATUS("OFFER_STATUS", true, String.class),
    PHONE("PHONE", true, String.class),
    SEX("SEX", true, String.class),
    USER_TITLE("USER_TITLE", true, String.class),
    
    // NOT DICTIONARY PROPERTIES
    FLOOR_QUANTITY("FLOOR_QUANTITY", false, Integer.class),
    DEPOSIT("DEPOSIT", false, Double.class), 
    LAND_AREA_SIZE("LAND_AREA_SIZE", false, Integer.class),
    HOUSE_BEDROOM_QUANTITY("HOUSE_BEDROOM_QUANTITY", false, Integer.class),
    HOUSE_BATHROOM_QUANTITY("HOUSE_BATHROOM_QUANTITY", false, Integer.class);
    
    private String name;
    private boolean isDictionary;
    
    private Class clazz;

    <T> FeatureType(String name, boolean isDictionary, Class clazz)
    {
        this.name = name;
        this.isDictionary = isDictionary;
        this.clazz = clazz;
    }

    public String getName()
    {
        return name;
    }
    
    public boolean isDictionary()
    {
        return isDictionary;
    }

    public Class getClazz()
    {
        return clazz;
    }
};
