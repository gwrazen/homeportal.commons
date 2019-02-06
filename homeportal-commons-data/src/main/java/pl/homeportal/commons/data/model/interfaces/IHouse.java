package pl.homeportal.commons.data.model.interfaces;


import java.io.Serializable;

public interface IHouse extends Serializable
{        
    Integer getRoomQuantity();
    
    void setRoomQuantity(Integer noOfBedrooms);
        
    void setBuildYear(Integer buildYear);
    
    Integer getBuildYear();
    
    Double getMaintenanceFee();
    
    void setMaintenanceFee(Double maintenanceFee);
    
    String getRoofType();
    
    void setRoofType(String roofType);
    
    Double getLandAreaSize();
    
    void setLandAreaSize(Double landAreaSize);

    void setPerformance(String performance);
    
    String getPerformance();    

    String getTenure();
    
    void setTenure(String tenure);
    
    String getBuildingState();
    
    void setBuildingState(String buildingState);
    
    String getGarage();
    
    void setGarage(String garage);
    
    String getCellar();
    
    void setCellar(String cellar);
    
    String getElevationType();
    
    void setElevationType(String elevationType);
    
    String getWindows();
    
    void setWindows(String windows);
    
    String getBalcony();
    
    void setBalcony(String balcony);
    
    String getTerrace();
    
    void setTerrace(String terrace);
    
    String getLandShape();
    
    void setLandShape(String landShape);
    
    void setLandFence(String landFence);
    
    String getLandFence();
    
    Integer getFloorQuantity();
    
    void setFloorQuantity(Integer floorQuantity);
    
    String getInstallations();
    
    void setInstallations(String installations);
    
    String getBuildMaterial();
    
    void setBuildMaterial(String buildMaterial);
    
    Integer getBedroomQuantity();
    
    void setBedroomQuantity(Integer bedroomQuantity);
    
    Integer getBathroomQuantity();
    
    void setBathroomQuantity(Integer bathroomQuantity);
        
    String getKitchenType();
    
    void setKitchenType(String kitchenType);
    
    String getBathroomType();
    
    void setBathroomType(String bathroomType);
    
    String getMedia();
    
    void setMedia(String media);
    
    String getHeating();
    
    void setHeating(String media);
    
    String getSewerageSystem();
    
    void setSewerageSystem(String sewerageSystem);
    
    String getFurniture();
    
    void setFurniture(String furniture);
    
    String getAdditionals();
    
    void setAdditionals(String additionals);
    
    Double getUsageAreaSize();
    
    void setUsageAreaSize(Double usageAreaSize);
}
