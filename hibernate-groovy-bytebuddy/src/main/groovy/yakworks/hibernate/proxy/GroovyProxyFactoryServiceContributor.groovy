/*
* Copyright 2022 original authors
* SPDX-License-Identifier: Apache-2.0
*/
package yakworks.hibernate.proxy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.hibernate.boot.registry.StandardServiceInitiator
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.bytecode.internal.bytebuddy.ByteBuddyState
import org.hibernate.bytecode.internal.bytebuddy.BytecodeProviderImpl
import org.hibernate.bytecode.internal.bytebuddy.ProxyFactoryFactoryImpl
import org.hibernate.bytecode.spi.BytecodeProvider
import org.hibernate.bytecode.spi.ProxyFactoryFactory
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.proxy.ProxyFactory
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyProxyHelper
import org.hibernate.service.spi.ServiceContributor
import org.hibernate.service.spi.ServiceRegistryImplementor

/**
 * handles the registering of the GroovyProxyFactoryFactoryInitiator.
 * This will replace the one that comes with hibernate.
 * is refenced in the service file, see META-INF/services/org.hibernate.service.spi.ServiceContributor
 */
@CompileStatic
class GroovyProxyFactoryServiceContributor implements ServiceContributor {
    private static final long serialVersionUID = 1L

    @Override
    void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        //only register if its not javassist (assumes its bytebuddy)
        if(serviceRegistryBuilder.settings['hibernate.bytecode.provider'] != 'javassist') {
            serviceRegistryBuilder.addInitiator(GroovyProxyFactoryFactoryInitiator.INSTANCE)
        }
    }

    @CompileStatic // so we can get to the privates on the provider to reuse them
    static class GroovyProxyFactoryFactoryInitiator implements StandardServiceInitiator<ProxyFactoryFactory> {

        /**
         * Singleton access
         */
        public static final StandardServiceInitiator<ProxyFactoryFactory> INSTANCE = new GroovyProxyFactoryFactoryInitiator();

        @Override
        ProxyFactoryFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
            BytecodeProviderImpl bytecodeProvider = registry.getService(BytecodeProvider.class) as BytecodeProviderImpl
            return createProxyFactoryFactory(bytecodeProvider)
        }

        @CompileDynamic // so we can get to the private byteBuddyState and byteBuddyProxyHelper on the provider to reuse them
        ProxyFactoryFactory createProxyFactoryFactory(BytecodeProviderImpl bytecodeProvider){
            ByteBuddyState byteBuddyState = bytecodeProvider.@byteBuddyState
            ByteBuddyProxyHelper byteBuddyProxyHelper = bytecodeProvider.@byteBuddyProxyHelper
            return new GroovyProxyFactoryFactory(byteBuddyState, byteBuddyProxyHelper)
        }


        @Override
        Class<ProxyFactoryFactory> getServiceInitiated() {
            return ProxyFactoryFactory.class;
        }
    }

    /**
    * This is what gets registered with serviceRegistryBuilder.addInitiator
    * Overrides the default ProxyFactoryFactoryImpl to build ByteBuddyGroovyProxyFactory instead of ByteBuddyProxyFactory
    * so we leave the buildBasicProxyFactory to the ProxyFactoryFactoryImpl.
    * TODO figure out if buildBasicProxyFactory is a problem with groovy too, not clear when thats used and did not come across it in test conditions
    * Its looks like its used in DynamicMapInstantiator and for embedded classes.
    */
    static class GroovyProxyFactoryFactory extends ProxyFactoryFactoryImpl {

        protected final ByteBuddyProxyHelper byteBuddyProxyHelper;

        public GroovyProxyFactoryFactory(ByteBuddyState byteBuddyState, ByteBuddyProxyHelper byteBuddyProxyHelper) {
            super(byteBuddyState, byteBuddyProxyHelper);
            this.byteBuddyProxyHelper = byteBuddyProxyHelper;
        }

        @Override
        public ProxyFactory buildProxyFactory(SessionFactoryImplementor sessionFactory) {
            return new ByteBuddyGroovyProxyFactory( byteBuddyProxyHelper );
        }
    }
}
