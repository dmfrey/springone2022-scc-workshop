package com.vmware.tanzulabs.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class Application {

	public static void main( String[] args ) {

		SpringApplication.run( Application.class, args );

	}

}

record OrderResponse( UUID orderId, String customerId )  { }

@RestController
class OrdersEndpoint {

	final OrderRepository repository;

	OrdersEndpoint( final OrderRepository repository ) {

		this.repository = repository;

	}

	@GetMapping( "/orders/{customerId}" )
	@CrossOrigin
	List<OrderResponse> ordersByCustomerId( @PathVariable String customerId ) {

		return this.repository.ordersByCustomerId( customerId ).stream()
				.map( order -> new OrderResponse( order.orderId(), order.customerId() ) )
				.toList();
	}

}

record Order( UUID orderId, String customerId ) { }

@Repository
class OrderRepository {

	private final List<Order> ordersDb =
			List.of(
					new Order( UUID.fromString("608cdd20-7a8a-42a0-8c8e-6ddc32d3d3c8" ), "6b3de4f1-8c77-4e74-8a43-f6c00b0affe6" ),
					new Order( UUID.fromString("a1130c0e-a6bf-4ea0-a5ca-931c72692d53" ), "58c3300e-e85d-4392-a58e-85f0cd3cd16b" ),
					new Order( UUID.fromString("514453c4-652c-4ce1-9726-a98571a6a041" ), "6b3de4f1-8c77-4e74-8a43-f6c00b0affe6" ),
					new Order( UUID.fromString("80a45893-0ead-42b9-b6e7-8935de66ba3b" ), "58c3300e-e85d-4392-a58e-85f0cd3cd16b" ),
					new Order( UUID.fromString("7dbea50f-fc91-4119-962e-cfbff1c2936b" ), "6b3de4f1-8c77-4e74-8a43-f6c00b0affe6" ),
					new Order( UUID.fromString("ce0eb361-55e3-4e13-8e07-a7076d70b197" ), "58c3300e-e85d-4392-a58e-85f0cd3cd16b" ),
					new Order( UUID.fromString("248e10fc-87e2-4e27-8144-1e72adf0c6bd" ), "58c3300e-e85d-4392-a58e-85f0cd3cd16b" ),
					new Order( UUID.fromString("eb4391bb-8080-4d72-a634-d23f425d11e8" ), "6b3de4f1-8c77-4e74-8a43-f6c00b0affe6" ),
					new Order( UUID.fromString("4e162a6c-b53f-495a-9d40-c04ec83039a1" ), "58c3300e-e85d-4392-a58e-85f0cd3cd16b" )
			);

	List<Order> ordersByCustomerId( final String customerId ) {

		return this.ordersDb.stream()
				.filter( order -> order.customerId().equals( customerId ) )
				.toList();
	}

}