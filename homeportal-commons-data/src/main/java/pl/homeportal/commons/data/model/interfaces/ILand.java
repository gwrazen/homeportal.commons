package pl.homeportal.commons.data.model.interfaces;

import java.io.Serializable;

public interface ILand extends Serializable
{
    String getLandTenure();

    void setLandTenure(String landTenure);

    String getLandBuilding();

    void setLandBuilding(String landBuilding);

    String getLandBuildingConditions();

    void setLandBuildingConditions(String landBuildingConditions);

    String getLandManagement();

    void setLandManagement(String landManagement);

    String getLandShape();

    void setLandShape(String landShape);

    String getLandFence();

    void setLandFence(String landFence);

    String getDriveAccess();

    void setDriveAccess(String driveAccess);

    String getSurrounding();

    void setSurrounding(String surrounding);

    String getPlacement();

    void setPlacement(String placement);

    String getMedia();

    void setMedia(String media);

    String getSawerageSystem();

    void setSawerageSystem(String sawerageSystem);

    String getAdditionals();

    void setAdditionals(String additionals);

    void setTypeLand(String propertyType);

    String getTypeLand();
}
