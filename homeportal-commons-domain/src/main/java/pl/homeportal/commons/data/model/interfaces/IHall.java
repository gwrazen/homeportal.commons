package pl.homeportal.commons.data.model.interfaces;

import java.io.Serializable;

public interface IHall extends Serializable
{
    Integer getBuildYear();

    void setBuildYear(Integer buildYear);

    Integer getRooms();

    void setRooms(Integer rooms);

    Double getMaintenanceFee();

    void setMaintenanceFee(Double maintenanceFee);

    Double getLandAreaSize();

    void setLandAreaSize(Double landAreaSize);

    String getBuildingMaterial();

    void setBuildingMaterial(String buildingMaterial);

    Integer getFloorQuantity();

    void setFloorQuantity(Integer floorQuantity);

    String getTenure();

    void setTenure(String tenure);

    String getLandTenure();

    void setLandTenure(String landTenure);

    String getLandType();

    void setLandType(String landType);

    String getLandShape();

    void setLandShape(String landShape);

    String getDriveAccess();

    void setDriveAccess(String driveAccess);

    String getPlacement();

    void setPlacement(String placement);

    String getCarAccess();

    void setCarAccess(String carAccess);

    String getMedia();

    void setMedia(String media);

    String getHeating();

    void setHeating(String heating);

    String getSewerageSystem();

    void setSewerageSystem(String sewerageSystem);

    String getPaymentsInRent();

    void setPaymentsInRent(String paymentsInRent);

    String getPaymentsViaMeters();

    void setPaymentsViaMeters(String paymentsViaMeters);

    String getAdditionals();

    void setAdditionals(String additionals);
}
