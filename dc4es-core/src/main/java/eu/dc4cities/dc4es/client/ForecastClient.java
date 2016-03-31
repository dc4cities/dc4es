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

package eu.dc4cities.dc4es.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.dc4es.converter.DateTimeModule;

/**
 * RESTful client for connecting to a RESTful Forecast Service (based on
 * Spring's RestTemplate which makes use of Jackson2 mapper for JSON)
 * 
 *
 * 
 */
public class ForecastClient {

	private static final Logger LOG = LoggerFactory
			.getLogger(ForecastClient.class);

	private static final String FORECAST_PATH = "v1/erds/{erdsName}/forecast";

	private final String forecastUrl;
	private final RestTemplate rt;

	/**
	 * @param serviceUrl forecaster host url (only host + webapp context)
	 */
	public ForecastClient(String serviceUrl) {
		Validate.notBlank(serviceUrl, "Forecaster Service URL cannot be blank");

		this.forecastUrl = StringUtils.endsWith(serviceUrl, "/") ? (serviceUrl + FORECAST_PATH)
				: (serviceUrl + "/" + FORECAST_PATH);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Configured " + getClass().getName()
					+ " with forecast URL " + forecastUrl);
		}

		// create client
		this.rt = createRestTemplate();
	}

	/**
	 * Get forecast for given request.
	 * 
	 * @param erdsName the name of the ERDS
	 * @param timeSlotBasedEntity the time interval of the forecast
	 * @return the energy forecast
	 * @throws IOException if the forecast service cannot be contacted
	 */
	public ErdsForecast getForecast(String erdsName,
			TimeSlotBasedEntity timeSlotBasedEntity) throws IOException {
		try {
			Map uriVariables = new HashMap();
			uriVariables.put("erdsName", erdsName);

			ErdsForecast response = rt.postForObject(forecastUrl,
					timeSlotBasedEntity, ErdsForecast.class, uriVariables);

			LOG.debug("ForecastResponse:\n"
					+ ToStringBuilder.reflectionToString(response));

			return response;
		} catch (Throwable e) {
			throw new IOException("Could not get ForecastResponse", e);
		}
	}

	/**
	 * @return RestTemplate instance
	 */
	protected RestTemplate createRestTemplate() {
		RestTemplate rt = new RestTemplate();
		// TODO set custom serializers/deserializers (e.g. from some global
		// location such as ctrl-model)
		ObjectMapper objectMapper = eu.dc4cities.controlsystem.model.json.JsonUtils
				.getDc4CitiesObjectMapper();

		// XXX should be moved to some global location
		objectMapper.registerModule(new DateTimeModule());

		MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
		jacksonConverter.setObjectMapper(objectMapper);
		
		List<HttpMessageConverter<?>> converters = rt.getMessageConverters();
		Iterator<HttpMessageConverter<?>> it = converters.iterator();
		while(it.hasNext()) {
			if(it.next() instanceof MappingJackson2HttpMessageConverter) {
				it.remove();
			}
		}
		
		// add custom instance
		rt.getMessageConverters().add(jacksonConverter);

		return rt;
	}

}
