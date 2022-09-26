package yakworks.groovyjpa;

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

import groovy.transform.CompileStatic

@Entity
@CompileStatic
class Contact {

    @Id
    Long id
    String firstName
    String lastName
    String phone

    Contact() {}

    Contact(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
