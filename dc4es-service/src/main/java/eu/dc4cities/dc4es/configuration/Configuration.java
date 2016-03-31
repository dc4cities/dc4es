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

package eu.dc4cities.dc4es.configuration;

import java.net.URL;

import javax.measure.quantity.Dimensionless;

import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import org.jscience.physics.amount.Amount;

import eu.dc4cities.controlsystem.model.quantity.GasEmission;

/**
 * 
 *
 * 
 */
public class Configuration {

	private URL databaseURL;
	private URL forecasterURL;
	private boolean needsForecast;
	private int serverPort;
	private int forecastGranularity;
	private String forecastUnit;
	private Amount<GasEmission> staticCarbonEmissionValue;
	private Amount<Dimensionless> staticRenewableEnergyPercentage;

	public URL getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(URL databaseURL) {
		this.databaseURL = databaseURL;
	}

	public URL getForecasterURL() {
		return forecasterURL;
	}

	public void setForecasterURL(URL forecasterURL) {
		this.forecasterURL = forecasterURL;
	}

	public boolean needsForecast() {
		return needsForecast;
	}

	public void setNeedsForecast(boolean needsForecast) {
		this.needsForecast = needsForecast;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getForecastGranularity() {
		return forecastGranularity;
	}

	public void setForecastGranularity(int forecastGranularity) {
		this.forecastGranularity = forecastGranularity;
	}

	public String getForecastUnit() {
		return forecastUnit;
	}

	public void setForecastUnit(String forecastUnit) {
		this.forecastUnit = forecastUnit;
	}

	public Amount<GasEmission> getStaticCarbonEmissionValue() {
		return staticCarbonEmissionValue;
	}

	public void setStaticCarbonEmissionValue(Amount<GasEmission> staticCarbonEmissionValue) {
		this.staticCarbonEmissionValue = staticCarbonEmissionValue;
	}

	public Amount<Dimensionless> getStaticRenewableEnergyPercentage() {
		return staticRenewableEnergyPercentage;
	}

	public void setStaticRenewableEnergyPercentage(Amount<Dimensionless> staticRenewableEnergyPercentage) {
		this.staticRenewableEnergyPercentage = staticRenewableEnergyPercentage;
	}

}
