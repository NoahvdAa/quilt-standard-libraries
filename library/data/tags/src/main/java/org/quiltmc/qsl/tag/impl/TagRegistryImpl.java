/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.tag.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Holder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.tag.api.QuiltTagKey;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tag.api.TagType;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;
import org.quiltmc.qsl.tag.mixin.DynamicRegistryManagerAccessor;

@ApiStatus.Internal
public final class TagRegistryImpl implements ServerLifecycleEvents.Stopped {
	private static final Map<TagKey<?>, Tag<Holder<?>>> TAGS = new Object2ObjectOpenHashMap<>();

	/**
	 * Returns whether the given registry key is the key of a dynamic registry.
	 *
	 * @param registryKey the key of the registry
	 * @return {@code true} if the registry is dynamic, otherwise {@code false}
	 */
	public static boolean isRegistryDynamic(RegistryKey<? extends Registry<?>> registryKey) {
		return DynamicRegistryManagerAccessor.getInfos().containsKey(registryKey);
	}

	public static void populateTags(Map<TagKey<?>, List<Holder<?>>> tags) {
		tags.forEach((key, values) -> TAGS.put(key, new Tag<>(values)));
	}

	public static void resetTags() {
		TAGS.clear();
	}

	@SuppressWarnings({"unchecked", "RedundantCast"})
	public static <T> Tag<Holder<T>> getTag(TagKey<T> key) {
		var type = ((QuiltTagKey<T>) (Object) key).type();

		if (type.hasSync()) {
			Tag<Holder<T>> tag = (Tag<Holder<T>>) (Object) TAGS.get(key);

			if (tag == null) {
				if (type == TagType.CLIENT_FALLBACK) {
					tag = ClientTagRegistryManager.get(key.registry()).getFallbackTag(key);
				} else {
					tag = Tag.getEmpty();
				}
			}

			return tag;
		} else if (type == TagType.CLIENT_ONLY) {
			return ClientTagRegistryManager.get(key.registry()).getClientTag(key);
		}

		return Tag.getEmpty();
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<TagRegistry.TagEntry<T>> streamTags(RegistryKey<? extends Registry<T>> registry) {
		return TAGS.entrySet().stream()
				.filter(entry -> entry.getKey().registry() == registry)
				.map(entry -> new TagRegistry.TagEntry<>((TagKey<T>) entry.getKey(), (Tag<Holder<T>>) (Object) entry.getValue()));
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<TagRegistry.TagEntry<T>> streamTagsWithFallback(RegistryKey<? extends Registry<T>> registry) {
		return Stream.concat(TAGS.entrySet().stream()
						.filter(entry -> entry.getKey().registry() == registry)
						.map(entry -> new TagRegistry.TagEntry<>((TagKey<T>) entry.getKey(), (Tag<Holder<T>>) (Object) entry.getValue())),
				ClientTagRegistryManager.get(registry).streamFallbackTags(entry -> !TAGS.containsKey(entry.getKey())));
	}

	@Override
	public void exitServer(MinecraftServer server) {
		resetTags();
	}
}
