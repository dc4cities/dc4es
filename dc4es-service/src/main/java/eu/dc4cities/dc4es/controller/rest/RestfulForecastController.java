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

package eu.dc4cities.dc4es.controller.rest;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.dc4es.controller.ForecastController;
import eu.dc4cities.dc4es.model.ForecastRequest;

/**
 * 
 *
 * 
 */
@Controller
public class RestfulForecastController {

	private static final Logger LOG = LoggerFactory
			.getLogger(RestfulForecastController.class);

	@Resource
	private ForecastController forecastController;

	@RequestMapping(value = "/v1/erds/{erdsName}/forecast", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
	@ResponseBody
	public ErdsForecast getForecast(@PathVariable String erdsName,
			@RequestBody TimeSlotBasedEntity timeSlotBasedEntity) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Request: " + timeSlotBasedEntity);
		}

		ForecastRequest forecastRequest = new ForecastRequest(timeSlotBasedEntity);
		forecastRequest.setErdsName(erdsName);
		
		ErdsForecast erdsForecast = forecastController
				.getForecast(forecastRequest);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Response: " + erdsForecast);
		}

		return erdsForecast;

	}
}
