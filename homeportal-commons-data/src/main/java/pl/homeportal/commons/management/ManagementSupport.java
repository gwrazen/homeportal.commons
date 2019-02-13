package pl.homeportal.commons.management;



import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Created with IntelliJ IDEA.
 * User: gwrazen
 * Date: 11/08/2015
 * Time: 17:43
 * To change this template use File | Settings | File Templates.
 */
public class ManagementSupport
{
    private MBeanServer platformMBeanServer;
    private ObjectName objectName = null;
    private final String name;

    public ManagementSupport(String name)
    {
        this.name = name;
    }

    @PostConstruct
    public void registerInJMX()
    {
        try
        {
            objectName = new ObjectName(name + getClass().getName());
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            platformMBeanServer.registerMBean(this, objectName);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Problem during registration of HomeportalHopManagement into JMX:" + e);
        }
    }

    @PreDestroy
    public void unregisterFromJMX()
    {
        try
        {
            platformMBeanServer.unregisterMBean(this.objectName);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Problem during unregistration of HomeportalHopManagement into JMX:" + e);
        }
    }
}
