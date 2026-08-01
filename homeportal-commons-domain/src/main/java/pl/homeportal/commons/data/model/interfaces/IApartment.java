package pl.homeportal.commons.data.model.interfaces;

import java.io.Serializable;

public interface IApartment extends Serializable
{
    Integer getRoomQuantity();

    void setRoomQuantity(Integer roomQuantity);

    void setBuildYear(Integer buildYear);

    Integer getBuildYear();

    Double getMaintenanceFee();

    void setMaintenanceFee(Double maintenanceFee);

    void setFloor(String floor);

    String getFloor();

    void setFloorQuantity(Integer floorQuantity);

    Integer getFloorQuantity();

    void setPerformance(String performance);

    String getPerformance();

    void setTenure(String tenure);

    String getTenure();

    void setApartmentType(String apartmentType);

    String getApartmentType();

    void setBuildTechnology(String buildTechnology);

    String getBuildTechnology();

    void setGarage(String garage);

    String getGarage();

    void setCellar(String cellar);

    String getCellar();

    void setWindows(String windows);

    String getWindows();

    void setInstallations(String installations);

    String getInstallations();

    void setBalcony(String balcony);

    String getBalcony();

    void setTerrace(String terrace);

    String getTerrace();

    void setMedia(String media);

    String getMedia();

    void setHeating(String heating);

    String getHeating();

    void setElevator(String elevator);

    String getElevator();

    void setKitchenType(String kitchenType);

    String getKitchenType();

    void setBathroomType(String bathroomType);

    String getBathroomType();

    void setFurniture(String Furniture);

    String getFurniture();

    String getPaymentsInRent();

    void setPaymentsInRent(String paymentsInRent);

    String getPaymentsViaMeters();

    void setPaymentsViaMeters(String paymentsViaMeters);

    String getAdditionals();

    void setAdditionals(String additionals);
}
