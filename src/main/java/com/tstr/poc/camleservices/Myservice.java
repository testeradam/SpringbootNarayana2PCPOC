package com.tstr.poc.camleservices;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tstr.poc.model.MyTableOne;
import com.tstr.poc.repository.MyTableOneRepository;

@Service
public class Myservice {

	@Autowired
	MyTableOneRepository myTableOneRepository;

	@Produce(uri = "direct:route1")
	ProducerTemplate template;

	@Transactional(propagation = Propagation.REQUIRED)
	public void myService(Exchange exchange) throws Exception {
		Map<String, Object> headers = exchange.getIn().getHeaders();
		String firstName = headers.get("firstName").toString();
		String lastName = headers.get("lastName").toString();
		String additionaNotes = exchange.getIn().getBody(String.class);
		MyTableOne myTableOne = new MyTableOne();
		myTableOne.setAdditionalNotes(additionaNotes);
		myTableOne.setFirstName(firstName);
		myTableOne.setLastName(lastName);

		// 1. Saving data into database
		myTableOneRepository.save(null);

		// 2. publishing event to say data has been saved
		Map<String, Object> sendable = exchange.getIn().getHeaders();
		sendable.put("STATUS", "OK");
		sendable.put("name", firstName+lastName);
		template.sendBodyAndHeaders("OK", sendable);

	}

}
