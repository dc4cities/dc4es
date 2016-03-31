/*
 * Copyright 2016 The DC4Cities author.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.dc4cities.dc4es.controller.rest.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.annotation.Resource;
import javax.measure.unit.NonSI;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;

/**
 * Integration test using ReplayForecastController and Spanish Grid data from
 * 2013
 *
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:replay-spanish-grid-test/test-replay-context.xml"})
@WebAppConfiguration
public class ReplaySpanishGridTest {

    private static final Logger LOG = LoggerFactory
            .getLogger(ReplaySpanishGridTest.class);

    @Autowired
    private WebApplicationContext webAppContext;

    /**
     * Custom DC4Cities ObjectMapper
     */
    @Resource
    private ObjectMapper customObjectMapper;

    private MockMvc mockMvc;

    @Before
    public void before() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .build();
    }

    @Test
    public void testForecast() throws Exception {
        TimeSlotBasedEntity timeSlotBasedEntity = new TimeSlotBasedEntity();

        // test whole day (96 slots), 15. August 2014 00:00:00 - 23:45:00
        DateTime basis = eu.dc4cities.dc4es.converter.DateTimeFormat.FORMAT
                .parseDateTime("2014-08-15T00:00:00");

        DateTime from = basis.toDateTime();
        DateTime to = basis.plusHours(23).plusMinutes(45);

        timeSlotBasedEntity.setDateFrom(from);
        timeSlotBasedEntity.setDateTo(to);
        timeSlotBasedEntity.setTimeSlotDuration(Amount
                .valueOf(15, NonSI.MINUTE));

        String erdsName = "spanish_grid";

        // to json
        String requestBody = customObjectMapper
                .writeValueAsString(timeSlotBasedEntity);

        LOG.debug("Request = " + requestBody);

        String responseBody = mockMvc
                .perform(
                        post("/v1/erds/" + erdsName + "/forecast").contentType(
                                MediaType.APPLICATION_JSON)
                                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(
                        content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();

        LOG.debug("ResponseBody = " + responseBody);

        // from json
        ErdsForecast erdsForecast = customObjectMapper.readValue(responseBody,
                ErdsForecast.class);

        LOG.debug(ToStringBuilder.reflectionToString(erdsForecast));

        // assert

        // String[] out = new String[8];
        // int i = 0;
        // for (TimeSlotErdsForecast timeSlotErdsForecast : erdsForecast
        // .getTimeSlotForecasts()) {
        // System.out.println("*** "
        // + timeSlotBasedEntity
        // .getDateFrom()
        // .plusMinutes(
        // (int) timeSlotBasedEntity
        // .getTimeSlotDuration()
        // .times(timeSlotErdsForecast
        // .getTimeSlot() - 1)
        // .longValue(MINUTE))
        // .toString(DateTimeFormat.forPattern("hh.mm.ss"))
        // + " ***");
        //
        // out[i] = timeSlotBasedEntity
        // .getDateFrom()
        // .plusMinutes(
        // (int) timeSlotBasedEntity
        // .getTimeSlotDuration()
        // .times(timeSlotErdsForecast.getTimeSlot() - 1)
        // .longValue(MINUTE))
        // .toString(DateTimeFormat.forPattern("hh.mm.ss"));
        // i++;
        // System.out.println(timeSlotErdsForecast.getPower());
        // out[i] = String.valueOf(timeSlotErdsForecast.getPower()
        // .getExactValue());
        // i++;
        // System.out.println(timeSlotErdsForecast.getCarbonEmissions());
        // out[i] = String.valueOf(timeSlotErdsForecast.getCarbonEmissions()
        // .getExactValue());
        // i++;
        // System.out.println(timeSlotErdsForecast.getRenewablePercentage());
        // out[i] = String.valueOf(timeSlotErdsForecast
        // .getRenewablePercentage().getExactValue());
        // i++;
        // System.out.println("");
        // }
        // String[] exp = { "10.15.00", "500", "2", "10", "10.30.00", "515",
        // "5",
        // "13" };
        // Assert.assertArrayEquals(out, exp);
    }
}
