package com.vmware.tanzulabs.person;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {

	public static void main( String[] args ) {

		SpringApplication.run( Application.class, args );

	}

}

record OrderResponse( UUID id, String customerId ) { }
record PersonResponse( UUID id, String firstName, String lastName, String customerId, Optional<List<OrderResponse>> orders ) {}

@RestController
class PersonsEndpoint {

	final PersonService personService;

	PersonsEndpoint( final PersonService personService ) {

		this.personService = personService;

	}

	@GetMapping( "/persons" )
	List<PersonResponse> persons() {

		return this.personService.getPersons().stream()
				.map( this::mapPerson )
				.toList();
	}


	@GetMapping( "/persons/{id}" )
	PersonResponse person( @PathVariable UUID id ) {

		var person = this.personService.getPersonById( id );

		return mapPerson( person );
	}

	private PersonResponse mapPerson( final Person person ) {

		var orders = mapOrders( person );

		return new PersonResponse( person.id(), person.firstName(), person.lastName(), person.customerId(), Optional.of( orders ) );
	}

	private List<OrderResponse> mapOrders( final Person person ) {

		List<OrderResponse> orders = new ArrayList<>();

		if( person.orders().isPresent() ) {

			orders =
					person.orders()
							.get().stream()
							.map( order -> new OrderResponse( order.orderId(), order.customerId() ) )
							.toList();

		}

		return orders;
	}

}

record Order( UUID orderId, String customerId ) { }
record Person( UUID id, String firstName, String lastName, String customerId, Optional<List<Order>> orders ) { }

record OrderResource( UUID orderId, String customerId ) { }

@Service
class PersonService {

	final PersonRepository repository;
	final OrderResourceService orderResourceService;

	PersonService(
			final PersonRepository repository,
			OrderResourceService orderResourceService
	) {

		this.repository = repository;
		this.orderResourceService = orderResourceService;

	}

	List<Person> getPersons() {

		return this.repository.allPersons().stream()
				.map( person -> {
					var orders = this.orderResourceService.lookupOrdersByCustomerId( person.customerId() );

					return new Person( person.id(), person.firstName(), person.lastName(), person.customerId(), orders );
				})
				.toList();
	}

	Person getPersonById( final UUID id ) {

		var person = this.repository.personById( id );
		var orders = this.orderResourceService.lookupOrdersByCustomerId( person.customerId() );

		return new Person( person.id(), person.firstName(), person.lastName(), person.customerId(), orders );
	}

}

@Component
class OrderResourceService {

	final RestTemplate restTemplate;
	final OrderResourceConfigurationProperties configurationProperties;

	OrderResourceService(
			final RestTemplate restTemplate,
			final OrderResourceConfigurationProperties configurationProperties
	) {

		this.restTemplate = restTemplate;
		this.configurationProperties = configurationProperties;

	}

	Optional<List<Order>> lookupOrdersByCustomerId( final String customerId ) {

		// Fake Circuit Breaker
		try {

			ResponseEntity<OrderResource[]> resourceResponseEntity = this.restTemplate.getForEntity( this.configurationProperties.orderResourceUrl() + "/orders/{customerId}", OrderResource[].class, customerId );

			List<Order> orders = Stream.of( Objects.requireNonNull( resourceResponseEntity.getBody() ) )
					.map(orderResource -> new Order( orderResource.orderId(), orderResource.customerId() ) )
					.toList();

			return Optional.of( orders );

		} catch( ResourceAccessException ex ) {

			return Optional.empty();
		}

	}

}

record PersonEntity( UUID id, String firstName, String lastName, String customerId ) {	}

@Repository
class PersonRepository {

	private final Map<UUID, PersonEntity> personsDb =
			Map.of(
					UUID.fromString( "74fff1ab-c620-4c1c-866a-d0e526f168a5"), new PersonEntity( UUID.fromString("74fff1ab-c620-4c1c-866a-d0e526f168a5" ), "Dan", "Frey", "6b3de4f1-8c77-4e74-8a43-f6c00b0affe6" ),
					UUID.fromString( "dc94b52e-b2d1-4988-8801-2aba785e4236"), new PersonEntity( UUID.fromString("dc94b52e-b2d1-4988-8801-2aba785e4236" ), "Ellie", "Bahadori", "58c3300e-e85d-4392-a58e-85f0cd3cd16b" )
			);

	List<Person> allPersons() {

		return this.personsDb.values().stream()
				.map( this::mapEntity )
				.toList();
	}

	Person personById( final UUID id ) {

		var entity = this.personsDb.get( id );

		return mapEntity( entity );
	}

	private Person mapEntity( final PersonEntity entity ) {

		return new Person( entity.id(), entity.firstName(), entity.lastName(), entity.customerId(), Optional.empty() );
	}

}

@ConfigurationProperties( prefix = "webservice" )
record OrderResourceConfigurationProperties( String orderResourceUrl ) { }

@Configuration
@EnableConfigurationProperties( OrderResourceConfigurationProperties.class )
class OrderResourceConfiguration {

	@Bean
	RestTemplate restTemplate() {

		return new RestTemplate();
	}

}
