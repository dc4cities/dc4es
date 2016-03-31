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

import org.springframework.beans.factory.FactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dc4cities.dc4es.converter.DateTimeModule;

/**
 * Temp. BeanFactory for configuring custom DC4Cities ObjectMapper
 * 
 *
 *
 */
public class ObjectMapperFactoryBean implements FactoryBean<ObjectMapper> {

	/**
	 * Custom configured ObjectMapper
	 */
	@Override
	public ObjectMapper getObject() throws Exception {
		ObjectMapper objectMapper = eu.dc4cities.controlsystem.model.json.JsonUtils.getDc4CitiesObjectMapper();
		
		// XXX should be moved to some global location
		objectMapper.registerModule(new DateTimeModule());
		
		return objectMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getObjectType() {
		return ObjectMapper.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}

}
