/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yakworks.groovyjpa


import org.hibernate.Hibernate
import org.hibernate.Session
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

import spock.lang.Specification
import yakworks.groovyjpa.Customer
import yakworks.groovyjpa.CustomerRepository

@DataJpaTest
class GroovyProxySpec extends Specification {

    @Autowired TestEntityManager testEntityManager
    // @Autowired EntityManagerFactory entityManagerFactory
    @Autowired CustomerRepository customerRepo

    void "groovy entity test"() {
        when:
        Customer customer = new Customer(1, "Tesla");
        testEntityManager.persist(customer);

        Session session = testEntityManager.entityManager.unwrap(Session.class);
        // session.flush()
        session.clear()
        Customer proxy = session.load(Customer, 1L)

        then:
        session
        //without ByteBuddyGroovyInterceptor this would normally cause the proxy to init
        proxy
        proxy.metaClass
        proxy.getMetaClass()
        !Hibernate.isInitialized(proxy)
        //id calls
        proxy.id == 1
        proxy.getId() == 1
        proxy["id"] == 1
        !Hibernate.isInitialized(proxy)
        //this would also normally cause the proxy to hydrate
        proxy.toString() == "Customer : 1 (proxy)"
        !Hibernate.isInitialized(proxy)
    }
}
