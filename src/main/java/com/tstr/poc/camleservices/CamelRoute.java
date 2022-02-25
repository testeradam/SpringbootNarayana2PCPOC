package com.tstr.poc.camleservices;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelRoute {
	
	private Myservice myService;

	RouteBuilder myRouteBuilder() {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				
				from("BrokerConsumerMsgComponent:queue:myQueue").bean(myService,"processService");
				
				from("direct:route1").to("BrokerProducerMsgComponent:topic:myTopic");

			}

		};
	}

}
