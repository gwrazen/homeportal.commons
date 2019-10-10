package pl.homeportal.commons.data.model.interfaces;

import java.io.Serializable;

public interface IOffice extends Serializable
{
    Integer getBuildYear();

    void setBuildYear(Integer buildYear);

    Integer getRoomQuantity();

    void setRoomQuantity(Integer roomQuantity);

    Double getMaintenanceFee();

    void setMaintenanceFee(Double maintenanceFee);

    String getFloor();

    void setFloor(String floor);

    void setFloorQuantity(Integer floorQuantity);

    Integer getFloorQuantity();

    void setTenure(String tenure);

    String getTenure();

    void setPerformance(String performance);

    String getPerformance();

    String getAdvertisementPlace();

    void setAdvertisementPlace(String advertisementPlace);

    String getOfficeType();

    void setOfficeType(String officeType);

    String getGarage();

    void setGarage(String garage);

    String getPurpose();

    void setPurpose(String garage);

}
