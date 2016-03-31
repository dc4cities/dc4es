//package eu.dc4cities.dc4es;
//
//import org.junit.Test;
//import org.springframework.util.Assert;
//
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//
///**
// *
// *
// *
// */
//public class EnergisTest {
//
//	@Test
//	public void basicEnergisTest() {
//		Client client = Client.create();
//
//		WebResource webResource = client
//				.resource("http://hackmeplz/energiscloud-gateway/restful/api/v1/data/query?apiKey=test");
//		String input = "{\"startAbsolute\": \"2014-06-01T00:00:00\",\"endAbsolute\": \"2014-07-01T00:00:00\",\"companyCode\" : \"hp\",\"assetCode\" : \"hp_milan\",\"metricName\" : \"renewable_power.FORECASTED\",\"granularity\" : {\"value\" : 1,\"unit\" : \"HOURS\"}}";
//
//		ClientResponse response = webResource.accept("application/json")
//				.type("application/json").post(ClientResponse.class, input);
//		if (response.getStatus() != 200) {
//			throw new RuntimeException("Failed : HTTP error code : "
//					+ response.getStatus());
//		}
//		String output = response.getEntity(String.class);
//		Assert.notNull(output);
//	}
//}