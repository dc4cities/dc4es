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

package eu.dc4cities.dc4es.model;

import java.util.Arrays;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import eu.dc4cities.controlsystem.model.json.AmountDeserializer;
import eu.dc4cities.controlsystem.model.json.AmountSerializer;

/**
 * 
 *
 *
 */

@Deprecated
public class TimeSlots {
	private int[] values;
	@JsonSerialize(using = AmountSerializer.class)
	@JsonDeserialize(using = AmountDeserializer.class)
	private Amount<Duration> duration;

	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		this.values = values;
	}

	public Amount<Duration> getDuration() {
		return duration;
	}
	
	public void setDuration(Amount<Duration> duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "TimeSlots [values=" + Arrays.toString(values) + ", duration="
				+ duration + "]";
	}

}
