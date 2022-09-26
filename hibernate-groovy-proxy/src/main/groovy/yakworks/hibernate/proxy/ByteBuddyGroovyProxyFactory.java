package yakworks.hibernate.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyConfiguration;
import org.hibernate.proxy.ProxyConfiguration.Interceptor;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyProxyHelper;
import org.hibernate.type.CompositeType;

import static org.hibernate.internal.CoreLogging.messageLogger;

/**
 * Replaces the stock ByteBuddyProxyFactory instead of overriding since everything is private and not easy extend.
 * A copy paste of original with slight modifications for class construction. Make it easier to extend too for others to modify
 */
public class ByteBuddyGroovyProxyFactory implements ProxyFactory, Serializable  {

    private static final CoreMessageLogger LOG = messageLogger( ByteBuddyGroovyProxyFactory.class );

    protected final ByteBuddyProxyHelper byteBuddyProxyHelper;

    protected Class persistentClass;
    protected String entityName;
    protected Class[] interfaces;
    protected Method getIdentifierMethod;
    protected Method setIdentifierMethod;
    protected CompositeType componentIdType;
    protected boolean overridesEquals;
    protected boolean overridesToString;
    protected boolean replaceToString;

    protected Class proxyClass;

    public ByteBuddyGroovyProxyFactory(ByteBuddyProxyHelper byteBuddyProxyHelper, boolean replaceToString) {
        this.byteBuddyProxyHelper = byteBuddyProxyHelper;
        this.replaceToString = replaceToString;
    }

    @Override
    public void postInstantiate(String entityName, Class persistentClass, Set<Class> interfaces, Method getIdentifierMethod,
                                Method setIdentifierMethod, CompositeType componentIdType) throws HibernateException {

        this.entityName = entityName;
        this.persistentClass = persistentClass;
        this.interfaces = toArray( interfaces );
        this.getIdentifierMethod = getIdentifierMethod;
        this.setIdentifierMethod = setIdentifierMethod;
        this.componentIdType = componentIdType;
        this.overridesEquals = ReflectHelper.overridesEquals( persistentClass );
        this.overridesToString = ReflectToStringHelper.overridesToString(persistentClass );

        this.proxyClass = byteBuddyProxyHelper.buildProxy( persistentClass, this.interfaces );
    }

    public static Class[] toArray(Set<Class> interfaces) {
        if ( interfaces == null ) {
            return ArrayHelper.EMPTY_CLASS_ARRAY;
        }
        return interfaces.toArray( new Class[interfaces.size()] );
    }

    @Override
    public HibernateProxy getProxy(Serializable id, SharedSessionContractImplementor session) throws HibernateException {

        final Interceptor interceptor = buildInterceptor(id, session);

        try {
            final HibernateProxy proxy = (HibernateProxy) proxyClass.getConstructor().newInstance();
            ( (ProxyConfiguration) proxy ).$$_hibernate_set_interceptor( interceptor );
            return proxy;
        }
        catch (NoSuchMethodException e) {
            String logMessage = LOG.bytecodeEnhancementFailedBecauseOfDefaultConstructor( entityName );
            LOG.error( logMessage, e );
            throw new HibernateException( logMessage, e );
        }
        catch (Throwable t) {
            String logMessage = LOG.bytecodeEnhancementFailed( entityName );
            LOG.error( logMessage, t );
            throw new HibernateException( logMessage, t );
        }
    }

    /**
     * creates the new ByteBuddy interceptor.
     * If implementing your own this should be the only method that needs to be overriden.
     * @return the new Interceptor
     */
    Interceptor buildInterceptor(Serializable id, SharedSessionContractImplementor session){
        final ByteBuddyGroovyInterceptor interceptor = new ByteBuddyGroovyInterceptor(
            entityName, persistentClass, interfaces, id, getIdentifierMethod, setIdentifierMethod, componentIdType, session,
            overridesEquals, overridesToString, replaceToString
        );
        return interceptor;
    }
}
