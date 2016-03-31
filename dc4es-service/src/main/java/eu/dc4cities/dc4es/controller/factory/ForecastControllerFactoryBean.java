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

package eu.dc4cities.dc4es.controller.factory;

import static javax.measure.unit.NonSI.PERCENT;

import java.net.URL;

import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.dc4cities.dc4es.configuration.Configuration;
import eu.dc4cities.dc4es.controller.ForecastController;
import eu.dc4cities.dc4es.controller.ForecastControllerImpl;
import eu.dc4cities.dc4es.controller.replay.ReplaySimControllerImpl;
import eu.dc4cities.dc4es.unit.AdvancedUnit;

/**
 * Simple Spring FactoryBean to either create the default ForecastController
 * instance or the Replay ForecastController.
 * 
 * This factory heavily relies on configuration params read from the Spring
 * configured *.properties.
 * 
 *
 * 
 */
@Component
public class ForecastControllerFactoryBean implements
		FactoryBean<ForecastController> {

	private static final Logger LOG = LoggerFactory
			.getLogger(ForecastControllerFactoryBean.class);

	/**
	 * default or replay
	 */
	@Value("${dc4es.forecastcontroller.type}")
	private String forecastControllerType;

	// default ForecastController
	@Value("${databaseURL}")
	private URL databaseURL;
	@Value("${forecasterURL}")
	private URL forecasterURL;
	@Value("${needsForecast}")
	private boolean needsForecast;
	@Value("${serverPort}")
	private int serverPort;
	@Value("${forecastGranularity}")
	private int forecastGranularity;
	@Value("${forecastUnit}")
	private String forecastUnit;
	@Value("${staticCarbonEmissionValue}")
	private long staticCarbonEmissionValue;
	@Value("${staticRenewableEnergyPercentage}")
	private long staticRenewableEnergyPercentage;
	
	
	

	// replay ForecastController
	@Value("${dc4es.forecastcontroller.replay.csvlocation}")
	private String csvLocation;
	@Value("${dc4es.forecastcontroller.replay.datetimepattern}")
	private String dateTimePattern;
	@Value("${dc4es.forecastcontroller.replay.delimiter}")
	private String delimiter;
	@Value("${dc4es.forecastcontroller.replay.fuzzynessfactor}")
	private double fuzzynessFactor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ForecastController getObject() throws Exception {
		Validate.notBlank(forecastControllerType,
				"Configuration key 'dc4es.forecastcontroller.type' cannot be blank");

		if (LOG.isDebugEnabled()) {
			LOG.debug("Found ForecastController Type = "
					+ forecastControllerType);
		}

		// instance
		ForecastController forecastController = null;

		if (StringUtils.equals(forecastControllerType, "default")) {
			// instantiate default forecastcontroller
			forecastController = createDefaultForecastController();
		} else if (StringUtils.equals(forecastControllerType, "replay")) {
			// instantiate replay forecastcontroller
			forecastController = createReplayForecastController();
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Instantiated ForecastController implementation = "
					+ forecastController.getClass().getName());
		}

		return forecastController;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getObjectType() {
		return ForecastController.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @return default ForecastController
	 * 
	 * @see eu.dc4cities.dc4es.controller.ForecastControllerImpl
	 */
	private ForecastController createDefaultForecastController() {
		try {
			// conf params
			Configuration configuration = new Configuration();
			configuration.setDatabaseURL(databaseURL);
			configuration.setForecasterURL(forecasterURL);
			configuration.setNeedsForecast(needsForecast);
			configuration.setServerPort(serverPort);
			configuration.setForecastGranularity(forecastGranularity);
			configuration.setForecastUnit(forecastUnit);
			configuration.setStaticCarbonEmissionValue(Amount.valueOf(staticCarbonEmissionValue, GasEmission.UNIT));
			configuration.setStaticRenewableEnergyPercentage(Amount.valueOf(staticRenewableEnergyPercentage, PERCENT));

			// create instance
			return new ForecastControllerImpl(configuration);
		} catch (Throwable e) {
			LOG.warn("Couldn't configure default ForecastController", e);

			throw new RuntimeException(
					"Couldn't configure default ForecastController", e);
		}
	}

	/**
	 * @return replay ForecastController
	 * 
	 * @see eu.dc4cities.dc4es.controller.replay.ReplaySimControllerImpl
	 */
	private ForecastController createReplayForecastController() {
		try {
			// configure 'replay' forecastcontroller

			// validate conf params
			Validate.notBlank(
					csvLocation,
					"Configuration key 'dc4es.forecastcontroller.replay.csvlocation' cannot be blank");

			Validate.notBlank(delimiter,
					"Configuration key 'dc4es.forecastcontroller.replay.delimiter' cannot be blank");

			// instantiate replay forecastcontroller
			return new ReplaySimControllerImpl(csvLocation, dateTimePattern,
					delimiter, fuzzynessFactor);
		} catch (Throwable e) {
			LOG.warn("Couldn't configure replay ForecastController", e);

			throw new RuntimeException(
					"Couldn't configure replay ForecastController", e);
		}
	}
}
