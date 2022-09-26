package yakworks.groovyjpa

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@CompileStatic
public class AccessingDataJpaApplication {

    private static final Logger log = LoggerFactory.getLogger(AccessingDataJpaApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AccessingDataJpaApplication.class);
    }

    @Bean
    public CommandLineRunner demo(CustomerRepository repository) {
        return (args) -> {
            // save a few customers
            // repository.save(new Customer("Jack", "Bauer"));
            // repository.save(new Customer("Chloe", "O'Brian"));
            // repository.save(new Customer("Kim", "Bauer"));
            // repository.save(new Customer("David", "Palmer"));
            // repository.save(new Customer("Michelle", "Dessler"));

            repository.save(new Customer(1, "Bauer"));
            repository.save(new Customer(2, "O'Brian"));
            repository.save(new Customer(3, "Bauer"));
            repository.save(new Customer(4, "Palmer"));
            repository.save(new Customer(5, "Dessler"));
            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            for (Customer customer : repository.findAll()) {
                log.info(customer.toString());
            }
            log.info("");

            // fetch an individual customer by ID
            Customer customer = repository.findById(1L) as Customer;
            log.info("Customer found with findById(1L):");
            log.info("--------------------------------");
            log.info(customer.toString());
            log.info("");

            // fetch customers by last name
            log.info("Customer found with findByLastName('Bauer'):");
            log.info("--------------------------------------------");
            repository.findByName("Bauer").forEach(bauer -> log.info(bauer.toString()) );
            log.info("");
        } as CommandLineRunner
    }

}
