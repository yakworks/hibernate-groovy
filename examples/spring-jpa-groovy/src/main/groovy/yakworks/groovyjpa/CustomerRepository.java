package yakworks.groovyjpa;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

	List<Customer> findByName(String name);

	Customer findById(long id);
}
