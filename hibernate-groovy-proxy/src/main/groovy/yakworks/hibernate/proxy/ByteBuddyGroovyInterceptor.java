package yakworks.hibernate.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor;
import org.hibernate.type.CompositeType;

/**
 * Extends ByteBuddyInterceptor to intecept calls when using Groovy.
 * In the base ByteBuddyInterceptor calls to getMetaClass, which happen often in dynamically compiled groovy, will initialize.
 * Ths intercepts and passes them on as best as possible without initializing.
 *
 * TODO in ByteBuddyProxyHelper the deserializeProxy still uses the ByteBuddyInterceptor. Unsure if that will have an impact or how often
 * that will get used, anything desirialized will of course suffer the same issues when accessed.
 */
class ByteBuddyGroovyInterceptor extends ByteBuddyInterceptor{
    protected boolean overridesToString;
    // if true then doesnt matter what overridesToString, it always uses this intercetors toString.
    protected boolean replaceToString;

    ByteBuddyGroovyInterceptor(
            String entityName,
            Class persistentClass,
            Class[] interfaces,
            Serializable id,
            Method getIdentifierMethod,
            Method setIdentifierMethod,
            CompositeType componentIdType,
            SharedSessionContractImplementor session,
            boolean overridesEquals,
            boolean overridesToString,
            boolean replaceToString
    ) {

        super(entityName, persistentClass, interfaces, id, getIdentifierMethod, setIdentifierMethod, componentIdType, session, overridesEquals);
        this.overridesToString = overridesToString;
        this.replaceToString = replaceToString;
    }

    @Override
    public Object intercept(Object proxy, Method thisMethod, Object[] args) throws Throwable {
        String methodName = thisMethod.getName();
        int params = args.length;

        //only do this hacky stuff if its not initialized
        if(isUninitialized()) {
            if ( params == 0 ) {
                if ("getMetaClass".equals(methodName)) {
                    return InvokerHelper.getMetaClass(this.persistentClass);
                } else if ("toString".equals(methodName) && (replaceToString || !overridesToString)) {
                    return toString();
                }
                //TODO allow config for forwards.
            }
            //if its a dynamic check then will come in as getProperty
            else if (params == 1 && "getProperty".equals(methodName) ) {
                String prop = (String) args[0];
                if ("metaClass".equals(prop)) {
                    return InvokerHelper.getMetaClass(this.persistentClass);
                } else if ("id".equals(prop)) {
                    return getIdentifier();
                }
            }
            return super.intercept(proxy, thisMethod, args);
        }
        else {
             return super.intercept(proxy, thisMethod, args);
        }
        // return val;
    }

    @Override
    public String toString() {
        return this.persistentClass.getSimpleName() + " : " + getIdentifier() + " (proxy)";
    }

}
