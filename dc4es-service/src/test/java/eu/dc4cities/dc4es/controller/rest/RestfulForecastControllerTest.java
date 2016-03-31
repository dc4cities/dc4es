//package eu.dc4cities.dc4es.controller.rest;
//
//import static javax.measure.unit.SI.MILLI;
//import static javax.measure.unit.SI.WATT;
//import static javax.measure.unit.SI.SECOND;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import javax.annotation.Resource;
//import javax.measure.quantity.Power;
//import javax.measure.unit.NonSI;
//
//import org.apache.commons.lang3.builder.ToStringBuilder;
//import org.joda.time.DateTime;
//import org.joda.time.DateTimeZone;
//import org.jscience.physics.amount.Amount;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
//import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
//import eu.dc4cities.dc4es.converter.DateTimeFormat;
//
///**
// * RestfulForecastControllerTest Test
// *
// *
// *
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "classpath:springconfig/test-context.xml" })
//@WebAppConfiguration
//public class RestfulForecastControllerTest {
//
//	private static final Logger LOG = LoggerFactory.getLogger(RestfulForecastControllerTest.class);
//
//	@Autowired
//	private WebApplicationContext webAppContext;
//
//	/**
//	 * Custom DC4Cities ObjectMapper
//	 */
//	@Resource
//	private ObjectMapper customObjectMapper;
//
//	private MockMvc mockMvc;
//
//	@Before
//	public void before() {
//		this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
//	}
//
//	@Test
//	public void testForecast() throws Exception {
//		TimeSlotBasedEntity timeSlotBasedEntity = new TimeSlotBasedEntity();
//		timeSlotBasedEntity.setDateFrom(DateTime.parse("2014-06-01T10:00:00", DateTimeFormat.FORMAT.withZone(DateTimeZone.getDefault())));
//		timeSlotBasedEntity.setDateTo(DateTime.parse("2014-06-01T13:15:00", DateTimeFormat.FORMAT.withZone(DateTimeZone.getDefault())));
//		timeSlotBasedEntity.setTimeSlotDuration(Amount.valueOf(15, NonSI.MINUTE));
//
//		// to json
//		String requestBody = customObjectMapper.writeValueAsString(timeSlotBasedEntity);
//
//		LOG.debug(requestBody);
//
//		String responseBody = mockMvc.perform(post("/v1/erds/hp_milan/forecast").contentType(MediaType.APPLICATION_JSON).content(requestBody))
//				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn().getResponse().getContentAsString();
//
//		// from json
//		ErdsForecast erdsForecast = customObjectMapper.readValue(responseBody, ErdsForecast.class);
//
//		LOG.debug(ToStringBuilder.reflectionToString(erdsForecast));
//
//		Assert.assertEquals(timeSlotBasedEntity.getDateFrom(), erdsForecast.getDateFrom());
//		Assert.assertEquals(timeSlotBasedEntity.getDateTo(), erdsForecast.getDateTo());
//
//		int slotNumber = (int) ((timeSlotBasedEntity.getDateTo().getMillis() - timeSlotBasedEntity.getDateFrom().getMillis()) / timeSlotBasedEntity
//				.getTimeSlotDuration().longValue(MILLI(SECOND))) + 1;
//
//		Assert.assertEquals(slotNumber, erdsForecast.getTimeSlotForecasts().size());
//
//
//
//		timeSlotBasedEntity.setDateFrom(DateTime.parse("2014-06-01T10:00:00", DateTimeFormat.FORMAT.withZone(DateTimeZone.getDefault())));
//		timeSlotBasedEntity.setDateTo(DateTime.parse("2014-06-01T14:00:00", DateTimeFormat.FORMAT.withZone(DateTimeZone.getDefault())));
//		timeSlotBasedEntity.setTimeSlotDuration(Amount.valueOf(1, NonSI.HOUR));
//
//		// to json
//		requestBody = customObjectMapper.writeValueAsString(timeSlotBasedEntity);
//
//		LOG.debug(requestBody);
//
//		responseBody = mockMvc.perform(post("/v1/erds/hp_milan/forecast").contentType(MediaType.APPLICATION_JSON).content(requestBody))
//				.andExpect(status().isOk()).andExpect(content().contentType("application/json;charset=UTF-8")).andReturn().getResponse().getContentAsString();
//
//		// from json
//		ErdsForecast erdsForecast1 = customObjectMapper.readValue(responseBody, ErdsForecast.class);
//
//		LOG.debug(ToStringBuilder.reflectionToString(erdsForecast1));
//
//		Assert.assertEquals(timeSlotBasedEntity.getDateFrom(), erdsForecast1.getDateFrom());
//		Assert.assertEquals(timeSlotBasedEntity.getDateTo(), erdsForecast1.getDateTo());
//
//		slotNumber = (int) ((timeSlotBasedEntity.getDateTo().getMillis() - timeSlotBasedEntity.getDateFrom().getMillis()) / timeSlotBasedEntity
//				.getTimeSlotDuration().longValue(MILLI(SECOND))) + 1;
//
//		Assert.assertEquals(slotNumber, erdsForecast1.getTimeSlotForecasts().size());
//
//		Assert.assertEquals(erdsForecast.getTimeSlotForecasts().get(0).getPower(), erdsForecast1.getTimeSlotForecasts().get(0).getPower());
//		Assert.assertEquals(erdsForecast.getTimeSlotForecasts().get(4).getPower(), erdsForecast1.getTimeSlotForecasts().get(1).getPower());
//		Assert.assertEquals(erdsForecast.getTimeSlotForecasts().get(8).getPower(), erdsForecast1.getTimeSlotForecasts().get(2).getPower());
//		Assert.assertEquals(erdsForecast.getTimeSlotForecasts().get(12).getPower(), erdsForecast1.getTimeSlotForecasts().get(3).getPower());
//
//		Amount<Power> powerForSlot2 = Amount.valueOf((int) (erdsForecast1.getTimeSlotForecasts().get(0).getPower().times(0.75).plus(erdsForecast1.getTimeSlotForecasts().get(1).getPower().times(0.25)).doubleValue(WATT)), WATT);
//		Amount<Power> powerForSlot3 = Amount.valueOf((int) (erdsForecast1.getTimeSlotForecasts().get(0).getPower().times(0.5).plus(erdsForecast1.getTimeSlotForecasts().get(1).getPower().times(0.5)).doubleValue(WATT)), WATT);
//
//		Assert.assertEquals(powerForSlot2, erdsForecast.getTimeSlotForecasts().get(1).getPower());
//		Assert.assertEquals(powerForSlot3, erdsForecast.getTimeSlotForecasts().get(2).getPower());
//
//	}
//}
