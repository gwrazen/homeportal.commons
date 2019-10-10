package pl.homeportal.commons.data.model.interfaces;

import java.io.Serializable;

public interface IRent extends Serializable
{
    Double getDeposit();

    void setDeposit(Double deposit);

    String getDepositeType();

    void setDepositeType(String depositeType);

    String getLeaseTerm();

    void setLeaseTerm(String leaseTerm);
}
