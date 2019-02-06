package pl.homeportal.commons.data.model.feature;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by gwrazen on 02/10/2014.
 */
public class FeatureTypeProvider
{
    private static List<FeatureType> forRentApartment = new LinkedList<FeatureType>();
    private static List<FeatureType> forRentHouse  = new LinkedList<FeatureType>();
    private static List<FeatureType> forRentOffice = new LinkedList<FeatureType>();
    private static List<FeatureType> forRentHall   = new LinkedList<FeatureType>();
    private static List<FeatureType> forRentLand   = new LinkedList<FeatureType>();
    private static List<FeatureType> forRentObject   = new LinkedList<FeatureType>();

    private static List<FeatureType> forSaleApartment = new LinkedList<FeatureType>();
    private static List<FeatureType> forSaleHouse  = new LinkedList<FeatureType>();
    private static List<FeatureType> forSaleOffice = new LinkedList<FeatureType>();
    private static List<FeatureType> forSaleHall   = new LinkedList<FeatureType>();
    private static List<FeatureType> forSaleLand   = new LinkedList<FeatureType>();
    private static List<FeatureType> forSaleObject   = new LinkedList<FeatureType>();

    static
    {
        initForRentApartment();
        initForRentHouse();
        initForRentOffice();
        initForRentHall();
        initForRentLand();
        initForRentObject();

        initForSaleApartment();
        initForSaleHouse();
        initForSaleOffice();
        initForSaleHall();
        initForSaleLand();
        initForSaleObject();
    }

    private static void initForRentApartment()
    {
        forRentApartment.add(FeatureType.MARKET);
        forRentApartment.add(FeatureType.PROPERTY_TYPE);
        forRentApartment.add(FeatureType.APARTMENT_TYPE);
        forRentApartment.add(FeatureType.APARTMENT_PERFORMANCE);

        forRentApartment.add(FeatureType.WINDOWS);
        forRentApartment.add(FeatureType.FURNITURE);
        forRentApartment.add(FeatureType.BALCONY);
        forRentApartment.add(FeatureType.TERRACE);
        forRentApartment.add(FeatureType.INSTALLATIONS);
        forRentApartment.add(FeatureType.HEATING);
        forRentApartment.add(FeatureType.ELEVATOR);
        forRentApartment.add(FeatureType.APARTMENT_GARAGE);
        forRentApartment.add(FeatureType.APARTMENT_CELLAR);
        forRentApartment.add(FeatureType.APARTMENT_BUILD_TECHNOLOGY);

        forRentApartment.add(FeatureType.FLOOR);
        forRentApartment.add(FeatureType.FLOOR_QUANTITY);
        forRentApartment.add(FeatureType.PAYMENTS_IN_RENT);
        forRentApartment.add(FeatureType.PAYMENTS_VIA_METERS);

        forRentApartment.add(FeatureType.LEASE_TERM);
        forRentApartment.add(FeatureType.DEPOSIT_TYPE);
    }

    private static void initForRentHouse()
    {
        forRentHouse.add(FeatureType.MARKET);
        forRentHouse.add(FeatureType.PROPERTY_TYPE);

        forRentHouse.add(FeatureType.WINDOWS);
        forRentHouse.add(FeatureType.FURNITURE);
        forRentHouse.add(FeatureType.BALCONY);
        forRentHouse.add(FeatureType.TERRACE);
        forRentHouse.add(FeatureType.INSTALLATIONS);
        forRentHouse.add(FeatureType.HEATING);

        forRentHouse.add(FeatureType.LAND_AREA_SIZE);
        forRentHouse.add(FeatureType.LAND_FENCE);
        forRentHouse.add(FeatureType.LAND_SHAPE);
        forRentHouse.add(FeatureType.HOUSE_BEDROOM_QUANTITY);
        forRentHouse.add(FeatureType.HOUSE_ROOF_TYPE);
        forRentHouse.add(FeatureType.HOUSE_BUILD_STATE);
        forRentHouse.add(FeatureType.SEWERAGE_SYSTEM);
        forRentHouse.add(FeatureType.FLOOR_QUANTITY);
        forRentHouse.add(FeatureType.HOUSE_CELLAR);

        forRentHouse.add(FeatureType.LEASE_TERM);
        forRentHouse.add(FeatureType.DEPOSIT_TYPE);
    }

    private static void initForRentOffice()
    {
        forRentOffice.add(FeatureType.MARKET);
        forRentOffice.add(FeatureType.PROPERTY_TYPE_OFFICE);
        forRentOffice.add(FeatureType.APARTMENT_TYPE);

        forRentOffice.add(FeatureType.WINDOWS);
        forRentOffice.add(FeatureType.FURNITURE);
        forRentOffice.add(FeatureType.BALCONY);
        forRentOffice.add(FeatureType.TERRACE);
        forRentOffice.add(FeatureType.INSTALLATIONS);
        forRentOffice.add(FeatureType.HEATING);

        forRentOffice.add(FeatureType.APARTMENT_PERFORMANCE);
        forRentOffice.add(FeatureType.FLOOR);
        forRentOffice.add(FeatureType.FLOOR_QUANTITY);
        forRentOffice.add(FeatureType.PAYMENTS_IN_RENT);
        forRentOffice.add(FeatureType.PAYMENTS_VIA_METERS);

        forRentOffice.add(FeatureType.LEASE_TERM);
        forRentOffice.add(FeatureType.DEPOSIT_TYPE);
    }

    private static void initForRentHall()
    {
        forRentHall.add(FeatureType.MARKET);
        forRentHall.add(FeatureType.PROPERTY_TYPE_HALL);

        forRentHall.add(FeatureType.PAYMENTS_VIA_METERS);
        forRentHall.add(FeatureType.HALL_CAR_ACCESS);
        forRentHall.add(FeatureType.PLACEMENT);
        forRentHall.add(FeatureType.HALL_BUILDING_MATERIAL);
        forRentHall.add(FeatureType.DRIVE_ACCESS);
        forRentHall.add(FeatureType.PAYMENTS_IN_RENT);
        forRentHall.add(FeatureType.LAND_SHAPE);
        forRentHall.add(FeatureType.HEATING);

        forRentHall.add(FeatureType.LEASE_TERM);
        forRentHall.add(FeatureType.DEPOSIT_TYPE);
    }

    private static void initForRentLand()
    {
        forRentLand.add(FeatureType.PROPERTY_TYPE_LAND);
        forRentLand.add(FeatureType.LAND_SHAPE);
        forRentLand.add(FeatureType.LAND_BUILDING_CONDITIONS);
        forRentLand.add(FeatureType.LAND_FENCE);
        forRentLand.add(FeatureType.SURROUNDING);
        forRentLand.add(FeatureType.LAND_MANAGEMENT);
        forRentLand.add(FeatureType.SEWERAGE_SYSTEM);
        forRentLand.add(FeatureType.PLACEMENT);
        forRentLand.add(FeatureType.DRIVE_ACCESS);

        forRentLand.add(FeatureType.LEASE_TERM);
        forRentLand.add(FeatureType.DEPOSIT_TYPE);
    }

    private static void initForRentObject()
    {
        forRentOffice.add(FeatureType.MARKET);
        forRentOffice.add(FeatureType.PROPERTY_TYPE);
        forRentOffice.add(FeatureType.APARTMENT_TYPE);

        forRentOffice.add(FeatureType.WINDOWS);
        forRentOffice.add(FeatureType.FURNITURE);
        forRentOffice.add(FeatureType.BALCONY);
        forRentOffice.add(FeatureType.TERRACE);
        forRentOffice.add(FeatureType.INSTALLATIONS);
        forRentOffice.add(FeatureType.HEATING);

        forRentOffice.add(FeatureType.APARTMENT_PERFORMANCE);
        forRentOffice.add(FeatureType.FLOOR);
        forRentOffice.add(FeatureType.FLOOR_QUANTITY);
        forRentOffice.add(FeatureType.PAYMENTS_IN_RENT);
        forRentOffice.add(FeatureType.PAYMENTS_VIA_METERS);

        forRentOffice.add(FeatureType.LEASE_TERM);
        forRentOffice.add(FeatureType.DEPOSIT_TYPE);
        forRentOffice.add(FeatureType.DRIVE_ACCESS);
    }

    private static void initForSaleApartment()
    {
        forSaleApartment.add(FeatureType.MARKET);
        forSaleApartment.add(FeatureType.PROPERTY_TYPE);
        forSaleApartment.add(FeatureType.APARTMENT_TYPE);
        forSaleApartment.add(FeatureType.APARTMENT_PERFORMANCE);

        forSaleApartment.add(FeatureType.WINDOWS);
        forSaleApartment.add(FeatureType.FURNITURE);
        forSaleApartment.add(FeatureType.BALCONY);
        forSaleApartment.add(FeatureType.TERRACE);
        forSaleApartment.add(FeatureType.INSTALLATIONS);
        forSaleApartment.add(FeatureType.HEATING);
        forSaleApartment.add(FeatureType.ELEVATOR);
        forSaleApartment.add(FeatureType.APARTMENT_GARAGE);
        forSaleApartment.add(FeatureType.APARTMENT_CELLAR);
        forSaleApartment.add(FeatureType.APARTMENT_BUILD_TECHNOLOGY);

        forSaleApartment.add(FeatureType.FLOOR);
        forSaleApartment.add(FeatureType.FLOOR_QUANTITY);
        forSaleApartment.add(FeatureType.PAYMENTS_IN_RENT);
        forSaleApartment.add(FeatureType.PAYMENTS_VIA_METERS);
    }

    private static void initForSaleHouse()
    {
        forSaleHouse.add(FeatureType.MARKET);
        forSaleHouse.add(FeatureType.PROPERTY_TYPE);

        forSaleHouse.add(FeatureType.WINDOWS);
        forSaleHouse.add(FeatureType.FURNITURE);
        forSaleHouse.add(FeatureType.BALCONY);
        forSaleHouse.add(FeatureType.TERRACE);
        forSaleHouse.add(FeatureType.INSTALLATIONS);
        forSaleHouse.add(FeatureType.HEATING);

        forSaleHouse.add(FeatureType.LAND_AREA_SIZE);
        forSaleHouse.add(FeatureType.LAND_FENCE);
        forSaleHouse.add(FeatureType.LAND_SHAPE);
        forSaleHouse.add(FeatureType.HOUSE_BEDROOM_QUANTITY);
        forSaleHouse.add(FeatureType.HOUSE_ROOF_TYPE);
        forSaleHouse.add(FeatureType.HOUSE_BUILD_STATE);
        forSaleHouse.add(FeatureType.SEWERAGE_SYSTEM);
        forSaleHouse.add(FeatureType.FLOOR_QUANTITY);
        forSaleHouse.add(FeatureType.FLOOR_QUANTITY);
        forSaleHouse.add(FeatureType.HOUSE_CELLAR);
    }

    private static void initForSaleOffice()
    {
        forSaleOffice.add(FeatureType.MARKET);
        forSaleOffice.add(FeatureType.PROPERTY_TYPE_OFFICE);
        forSaleOffice.add(FeatureType.APARTMENT_TYPE);

        forSaleOffice.add(FeatureType.WINDOWS);
        forSaleOffice.add(FeatureType.FURNITURE);
        forSaleOffice.add(FeatureType.BALCONY);
        forSaleOffice.add(FeatureType.TERRACE);
        forSaleOffice.add(FeatureType.INSTALLATIONS);
        forSaleOffice.add(FeatureType.HEATING);

        forSaleOffice.add(FeatureType.APARTMENT_PERFORMANCE);
        forSaleOffice.add(FeatureType.FLOOR);
        forSaleOffice.add(FeatureType.FLOOR_QUANTITY);
        forSaleOffice.add(FeatureType.PAYMENTS_IN_RENT);
        forSaleOffice.add(FeatureType.PAYMENTS_VIA_METERS);
    }

    private static void initForSaleHall()
    {
        forSaleHall.add(FeatureType.MARKET);
        forSaleHall.add(FeatureType.PROPERTY_TYPE_HALL);

        forSaleHall.add(FeatureType.PAYMENTS_VIA_METERS);
        forSaleHall.add(FeatureType.HALL_CAR_ACCESS);
        forSaleHall.add(FeatureType.PLACEMENT);
        forSaleHall.add(FeatureType.HALL_BUILDING_MATERIAL);
        forSaleHall.add(FeatureType.DRIVE_ACCESS);
        forSaleHall.add(FeatureType.PAYMENTS_IN_RENT);
        forSaleHall.add(FeatureType.LAND_SHAPE);
        forSaleHall.add(FeatureType.HEATING);
    }

    private static void initForSaleLand()
    {
        forSaleLand.add(FeatureType.PROPERTY_TYPE_LAND);
        forSaleLand.add(FeatureType.LAND_SHAPE);
        forSaleLand.add(FeatureType.LAND_BUILDING_CONDITIONS);
        forSaleLand.add(FeatureType.LAND_FENCE);
        forSaleLand.add(FeatureType.SURROUNDING);
        forSaleLand.add(FeatureType.LAND_MANAGEMENT);
        forSaleLand.add(FeatureType.SEWERAGE_SYSTEM);
        forSaleLand.add(FeatureType.PLACEMENT);
        forSaleLand.add(FeatureType.DRIVE_ACCESS);
    }

    private static void initForSaleObject()
    {
        forSaleOffice.add(FeatureType.MARKET);
        forSaleOffice.add(FeatureType.PROPERTY_TYPE);
        forSaleOffice.add(FeatureType.APARTMENT_TYPE);

        forSaleOffice.add(FeatureType.WINDOWS);
        forSaleOffice.add(FeatureType.FURNITURE);
        forSaleOffice.add(FeatureType.BALCONY);
        forSaleOffice.add(FeatureType.TERRACE);
        forSaleOffice.add(FeatureType.INSTALLATIONS);
        forSaleOffice.add(FeatureType.HEATING);

        forSaleOffice.add(FeatureType.APARTMENT_PERFORMANCE);
        forSaleOffice.add(FeatureType.FLOOR);
        forSaleOffice.add(FeatureType.FLOOR_QUANTITY);
        forSaleOffice.add(FeatureType.PAYMENTS_IN_RENT);
        forSaleOffice.add(FeatureType.PAYMENTS_VIA_METERS);
        forSaleOffice.add(FeatureType.DRIVE_ACCESS);
    }

    public static List<FeatureType> forRentApartment()
    {
        return forRentApartment;
    }

    public static List<FeatureType> forRentHouse()
    {
        return forRentHouse;
    }

    public static List<FeatureType> forRentOffice()
    {
        return forRentOffice;
    }

    public static List<FeatureType> forRentHall()
    {
        return forRentHall;
    }

    public static List<FeatureType> forRentLand()
    {
        return forRentHall;
    }

    public static List<FeatureType> forRentObject()
    {
        return forRentObject;
    }

    public static List<FeatureType> forSaleApartment()
    {
        return forSaleApartment;
    }

    public static List<FeatureType> forSaleHouse()
    {
        return forSaleHouse;
    }

    public static List<FeatureType> forSaleOffice()
    {
        return forSaleOffice;
    }

    public static List<FeatureType> forSaleHall()
    {
        return forSaleHall;
    }

    public static List<FeatureType> forSaleLand()
    {
        return forSaleLand;
    }

    public static List<FeatureType> forSaleObject()
    {
        return forSaleObject;
    }
}
