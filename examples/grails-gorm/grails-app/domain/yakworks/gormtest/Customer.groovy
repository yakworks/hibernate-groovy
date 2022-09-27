package yakworks.gormtest

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class Customer implements Serializable {

    String name

    Customer() {}

    Customer(Long id, String name) {
        this.id = id
        this.name = name
    }

    static mapping = {
        id generator: 'assigned'
    }
}
