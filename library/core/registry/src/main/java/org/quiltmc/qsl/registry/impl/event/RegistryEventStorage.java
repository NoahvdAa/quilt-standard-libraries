/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.registry.impl.event;

import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

/**
 * A duck interface for getting registry events stored in a {@link Registry}.
 */
@ApiStatus.Internal
public interface RegistryEventStorage<V> {
	/**
	 * {@return the entry added event}
	 */
	Event<RegistryEvents.EntryAdded<V>> quilt$getEntryAddedEvent();

	/**
	 * Casts a {@link Registry} to the duck interface.
	 */
	@SuppressWarnings("unchecked")
	static <W> RegistryEventStorage<W> as(Registry<W> registry) {
		return (RegistryEventStorage<W>) registry;
	}
}
