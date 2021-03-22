package org.cryptomator.cryptofs;

import org.cryptomator.cryptofs.CryptoFileSystemProperties.*;
import org.cryptomator.cryptolib.api.MasterkeyLoader;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import static org.cryptomator.cryptofs.CryptoFileSystemProperties.*;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

public class CryptoFileSystemPropertiesTest {

	private final MasterkeyLoader keyLoader = Mockito.mock(MasterkeyLoader.class);

	@Test
	public void testSetNoPassphrase() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			cryptoFileSystemProperties().build();
		});
	}

	@Test
	public void testSetMasterkeyFilenameAndReadonlyFlag() {
		String masterkeyFilename = "aMasterkeyFilename";
		CryptoFileSystemProperties inTest = cryptoFileSystemProperties() //
				.withKeyLoaders(keyLoader) //
				.withMasterkeyFilename(masterkeyFilename) //
				.withFlags(FileSystemFlags.READONLY)
				.build();

		MatcherAssert.assertThat(inTest.masterkeyFilename(), is(masterkeyFilename));
		MatcherAssert.assertThat(inTest.readonly(), is(true));
		MatcherAssert.assertThat(inTest.entrySet(),
				containsInAnyOrder( //
						anEntry(PROPERTY_KEYLOADERS, Set.of(keyLoader)), //
						anEntry(PROPERTY_VAULTCONFIG_FILENAME, DEFAULT_VAULTCONFIG_FILENAME), //
						anEntry(PROPERTY_MASTERKEY_FILENAME, masterkeyFilename), //
						anEntry(PROPERTY_MAX_PATH_LENGTH, DEFAULT_MAX_PATH_LENGTH), //
						anEntry(PROPERTY_MAX_NAME_LENGTH, DEFAULT_MAX_NAME_LENGTH), //
						anEntry(PROPERTY_CIPHER_COMBO, DEFAULT_CIPHER_COMBO), //
						anEntry(PROPERTY_FILESYSTEM_FLAGS, EnumSet.of(FileSystemFlags.READONLY))));
	}

	@Test
	public void testFromMap() {
		Map<String, Object> map = new HashMap<>();
		String masterkeyFilename = "aMasterkeyFilename";
		map.put(PROPERTY_KEYLOADERS, Set.of(keyLoader));
		map.put(PROPERTY_MASTERKEY_FILENAME, masterkeyFilename);
		map.put(PROPERTY_MAX_PATH_LENGTH, 1000);
		map.put(PROPERTY_MAX_NAME_LENGTH, 255);
		map.put(PROPERTY_FILESYSTEM_FLAGS, EnumSet.of(FileSystemFlags.READONLY));
		CryptoFileSystemProperties inTest = cryptoFileSystemPropertiesFrom(map).build();

		MatcherAssert.assertThat(inTest.masterkeyFilename(), is(masterkeyFilename));
		MatcherAssert.assertThat(inTest.readonly(), is(true));
		MatcherAssert.assertThat(inTest.maxPathLength(), is(1000));
		MatcherAssert.assertThat(inTest.maxNameLength(), is(255));
		MatcherAssert.assertThat(inTest.entrySet(),
				containsInAnyOrder( //
						anEntry(PROPERTY_KEYLOADERS, Set.of(keyLoader)), //
						anEntry(PROPERTY_VAULTCONFIG_FILENAME, DEFAULT_VAULTCONFIG_FILENAME), //
						anEntry(PROPERTY_MASTERKEY_FILENAME, masterkeyFilename), //
						anEntry(PROPERTY_MAX_PATH_LENGTH, 1000), //
						anEntry(PROPERTY_MAX_NAME_LENGTH, 255), //
						anEntry(PROPERTY_CIPHER_COMBO, DEFAULT_CIPHER_COMBO), //
						anEntry(PROPERTY_FILESYSTEM_FLAGS, EnumSet.of(FileSystemFlags.READONLY))));
	}

	@Test
	public void testWrapMapWithTrueReadonly() {
		Map<String, Object> map = new HashMap<>();
		String masterkeyFilename = "aMasterkeyFilename";
		map.put(PROPERTY_KEYLOADERS, Set.of(keyLoader));
		map.put(PROPERTY_MASTERKEY_FILENAME, masterkeyFilename);
		map.put(PROPERTY_FILESYSTEM_FLAGS, EnumSet.of(FileSystemFlags.READONLY));
		CryptoFileSystemProperties inTest = CryptoFileSystemProperties.wrap(map);

		MatcherAssert.assertThat(inTest.masterkeyFilename(), is(masterkeyFilename));
		MatcherAssert.assertThat(inTest.readonly(), is(true));
		MatcherAssert.assertThat(inTest.entrySet(),
				containsInAnyOrder( //
						anEntry(PROPERTY_KEYLOADERS, Set.of(keyLoader)), //
						anEntry(PROPERTY_VAULTCONFIG_FILENAME, DEFAULT_VAULTCONFIG_FILENAME), //
						anEntry(PROPERTY_MASTERKEY_FILENAME, masterkeyFilename), //
						anEntry(PROPERTY_MAX_PATH_LENGTH, DEFAULT_MAX_PATH_LENGTH), //
						anEntry(PROPERTY_MAX_NAME_LENGTH, DEFAULT_MAX_NAME_LENGTH), //
						anEntry(PROPERTY_CIPHER_COMBO, DEFAULT_CIPHER_COMBO), //
						anEntry(PROPERTY_FILESYSTEM_FLAGS, EnumSet.of(FileSystemFlags.READONLY))));
	}

	@Test
	public void testWrapMapWithFalseReadonly() {
		Map<String, Object> map = new HashMap<>();
		String masterkeyFilename = "aMasterkeyFilename";
		map.put(PROPERTY_KEYLOADERS, Set.of(keyLoader));
		map.put(PROPERTY_MASTERKEY_FILENAME, masterkeyFilename);
		map.put(PROPERTY_FILESYSTEM_FLAGS, EnumSet.noneOf(FileSystemFlags.class));
		CryptoFileSystemProperties inTest = CryptoFileSystemProperties.wrap(map);

		MatcherAssert.assertThat(inTest.masterkeyFilename(), is(masterkeyFilename));
		MatcherAssert.assertThat(inTest.readonly(), is(false));
		MatcherAssert.assertThat(inTest.entrySet(),
				containsInAnyOrder( //
						anEntry(PROPERTY_KEYLOADERS, Set.of(keyLoader)), //
						anEntry(PROPERTY_VAULTCONFIG_FILENAME, DEFAULT_VAULTCONFIG_FILENAME), //
						anEntry(PROPERTY_MASTERKEY_FILENAME, masterkeyFilename), //
						anEntry(PROPERTY_MAX_PATH_LENGTH, DEFAULT_MAX_PATH_LENGTH), //
						anEntry(PROPERTY_MAX_NAME_LENGTH, DEFAULT_MAX_NAME_LENGTH), //
						anEntry(PROPERTY_CIPHER_COMBO, DEFAULT_CIPHER_COMBO), //
						anEntry(PROPERTY_FILESYSTEM_FLAGS, EnumSet.noneOf(FileSystemFlags.class))));
	}

	@Test
	public void testWrapMapWithInvalidFilesystemFlags() {
		Map<String, Object> map = new HashMap<>();
		map.put(PROPERTY_MASTERKEY_FILENAME, "any");
		map.put(PROPERTY_FILESYSTEM_FLAGS, "invalidType");

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			CryptoFileSystemProperties.wrap(map);
		});
	}

	@Test
	public void testWrapMapWithInvalidMasterkeyFilename() {
		Map<String, Object> map = new HashMap<>();
		map.put(PROPERTY_MASTERKEY_FILENAME, "");
		map.put(PROPERTY_FILESYSTEM_FLAGS, EnumSet.noneOf(FileSystemFlags.class));

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			CryptoFileSystemProperties.wrap(map);
		});
	}

	@Test
	public void testWrapMapWithInvalidPassphrase() {
		Map<String, Object> map = new HashMap<>();
		map.put(PROPERTY_MASTERKEY_FILENAME, "any");
		map.put(PROPERTY_FILESYSTEM_FLAGS, EnumSet.noneOf(FileSystemFlags.class));

		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			CryptoFileSystemProperties.wrap(map);
		});
	}

	@Test
	public void testWrapMapWithoutReadonly() {
		Map<String, Object> map = new HashMap<>();
		map.put(PROPERTY_KEYLOADERS, Set.of(keyLoader));
		CryptoFileSystemProperties inTest = CryptoFileSystemProperties.wrap(map);

		MatcherAssert.assertThat(inTest.masterkeyFilename(), is(DEFAULT_MASTERKEY_FILENAME));
		MatcherAssert.assertThat(inTest.readonly(), is(false));
		MatcherAssert.assertThat(inTest.entrySet(),
				containsInAnyOrder( //
						anEntry(PROPERTY_KEYLOADERS, Set.of(keyLoader)), //
						anEntry(PROPERTY_VAULTCONFIG_FILENAME, DEFAULT_VAULTCONFIG_FILENAME), //
						anEntry(PROPERTY_MASTERKEY_FILENAME, DEFAULT_MASTERKEY_FILENAME), //
						anEntry(PROPERTY_MAX_PATH_LENGTH, DEFAULT_MAX_PATH_LENGTH), //
						anEntry(PROPERTY_MAX_NAME_LENGTH, DEFAULT_MAX_NAME_LENGTH), //
						anEntry(PROPERTY_CIPHER_COMBO, DEFAULT_CIPHER_COMBO), //
						anEntry(PROPERTY_FILESYSTEM_FLAGS, EnumSet.noneOf(FileSystemFlags.class))
				)
		);
	}

	@Test
	public void testWrapMapWithoutPassphrase() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
			CryptoFileSystemProperties.wrap(new HashMap<>());
		});
	}

	@Test
	public void testWrapCryptoFileSystemProperties() {
		CryptoFileSystemProperties inTest = cryptoFileSystemProperties().withKeyLoaders(keyLoader).build();

		MatcherAssert.assertThat(CryptoFileSystemProperties.wrap(inTest), is(sameInstance(inTest)));
	}

	@Test
	public void testMapIsImmutable() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			cryptoFileSystemProperties() //
					.withKeyLoaders(keyLoader) //
					.build() //
					.put("test", "test");
		});
	}

	@Test
	public void testEntrySetIsImmutable() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			cryptoFileSystemProperties() //
					.withKeyLoaders(keyLoader) //
					.build() //
					.entrySet() //
					.add(null);
		});
	}

	@Test
	public void testEntryIsImmutable() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			cryptoFileSystemProperties() //
					.withKeyLoaders(keyLoader) //
					.build() //
					.entrySet() //
					.iterator().next() //
					.setValue(null);
		});
	}

	private <K, V> Matcher<Map.Entry<K, V>> anEntry(K key, V value) {
		return new TypeSafeDiagnosingMatcher<>(Map.Entry.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("an entry ").appendValue(key).appendText(" = ").appendValue(value);
			}

			@Override
			protected boolean matchesSafely(Entry<K, V> item, Description mismatchDescription) {
				if (keyMatches(item.getKey()) && valueMatches(item.getValue())) {
					return true;
				}
				mismatchDescription.appendText("an entry ").appendValue(item.getKey()).appendText(" = ").appendValue(item.getValue());
				return false;
			}

			private boolean keyMatches(K itemKey) {
				return Objects.equals(key, itemKey);
			}

			private boolean valueMatches(V itemValue) {
				if (value instanceof Collection v && itemValue instanceof Collection c) {
					return valuesMatch(v, c);
				} else {
					return Objects.equals(value, itemValue);
				}
			}

			@SuppressWarnings("rawtypes")
			private boolean valuesMatch(Collection<?> value, Collection<?> itemValue) {
				return value.containsAll(itemValue) && itemValue.containsAll(value);
			}
		};
	}

}
