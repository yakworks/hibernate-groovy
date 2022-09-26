package yakworks.groovyjpa;

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

import org.springframework.lang.Nullable

@Entity
@CompileStatic
class Customer {

    @Id
    // @GeneratedValue(strategy=GenerationType.)
    Long id
    String name
    //
    // @JoinColumn(name = "contactId")
    // @ManyToOne
    // private @Nullable Contact contact;

    Customer() {}

    Customer(Long id, String name) {
        this.id = id
        this.name = name
    }

    //this will only get used if its not a proxy by default
    // if hibernate.groovy.proxy.replace_to_string=false then it will init the proxy and use this as normal
    @Override
    String toString() {
        return String.format( "Customer[id=%d, name='%s']", id, name);
    }
}
