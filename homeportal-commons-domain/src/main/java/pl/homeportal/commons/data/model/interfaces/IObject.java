package pl.homeportal.commons.data.model.interfaces;

import java.io.Serializable;

/**
 * Created by gwrazen on 16/04/2015.
 */
public interface IObject extends Serializable
{
    Double getMaintenanceFee();

    void setMaintenanceFee(Double maintenanceFee);

    Integer getRooms();

    void setRooms(Integer rooms);

    Integer getBuildYear();

    void setBuildYear(Integer buildYear);

    String getFloor();

    void setFloor(String floor);

    Integer getFloorQuantity();

    void setFloorQuantity(Integer floorQuantity);

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

    String getDriveAccess();

    void setDriveAccess(String driveAccess);
}
