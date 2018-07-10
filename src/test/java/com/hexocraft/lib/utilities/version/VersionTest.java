package com.hexocraft.lib.utilities.version;

/*

 Copyright 2018 hexosse

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
import mock.FakePlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VersionTest {


    private JavaPlugin plugin = FakePlugin.get();

    @Test
    void constructors() {
        assertEquals(new Version(1, 2, 2).toString(), "1.2.2");
        assertEquals(new Version(plugin.getDescription().getVersion()).toString(), "1.0.0");
    }

    @Test
    void parser() {
        assertTrue(Version.isSemver("0.1.2"));
        assertTrue(Version.isSemver("1.2.3"));
        assertTrue(Version.isSemver("10.20.3"));
        assertTrue(Version.isSemver("1.2.3-alpha.23-pre"));
        assertTrue(Version.isSemver("12.12.3-123.hexagon+dontmakemecompileplea.se"));
        assertTrue(Version.isSemver("1.2.3-alpha-dev.51-something+mybuild-1-4-1975-clang"));
        assertTrue(Version.isSemver("4.3.22+mybuild"));
        assertTrue(Version.isSemver("4.1.405+hexa.13331-objectfiles"));

        assertTrue(Version.isSemver(plugin));
        assertEquals(Version.parse(plugin).toString(), "1.0.0");

        assertFalse(Version.isSemver("1.0"));

        assertFalse(Version.isSemver("01.2.3"));
        assertFalse(Version.isSemver("1.02.3"));
        assertFalse(Version.isSemver("2.3.04"));
        assertFalse(Version.isSemver("a.1.1"));
        assertFalse(Version.isSemver("1.a.1"));
        assertFalse(Version.isSemver("1.1.a"));
        assertFalse(Version.isSemver("1.2.3-rele..ase+build"));
        assertFalse(Version.isSemver("1.2.3-release-something+..build"));
        assertFalse(Version.isSemver("1.2.3-rele--ase+build"));
        assertFalse(Version.isSemver("1.2.3-release-something+--build"));
        assertFalse(Version.isSemver("1.2.3-release++build"));
        assertFalse(Version.isSemver("1.2.3+-release-something-build"));


        assertFalse(Version.isSemver("v1.0.0"));
        assertEquals(new Version("v1.0.0").toString(), "1.0.0");

        assertFalse(Version.isSemver("v1.0"));
        assertEquals(new Version("v1.0").toString(), "1.0.0");

        assertFalse(Version.isSemver("no number"));
        assertEquals(Version.parse("no number"), null);
        assertThrows(IllegalArgumentException.class, () -> new Version("no number"));
    }

    @Test
    void precedence() {
        assertEquals(Version.parse("1.2.3"), Version.parse("1.2.3"));
        assertEquals(Version.parse("1.0.0-alpha"), Version.parse("1.0.0-alpha"));
        assertEquals(Version.parse("1.0.0-alpha-alpha.1"), Version.parse("1.0.0-alpha-alpha.1"));
        assertEquals(Version.parse("1.0.0-alpha-alpha.1"), Version.parse("1.0.0-alpha.1-alpha"));

        Version v1 = Version.parse("v1.1.1");
        Version v1b = Version.parse("v1.1.2");
        Version v2 = Version.parse("2.0.0");

        assertTrue(v1.equals(v1));
        assertTrue(v1.isLessThan(v1b));
        assertFalse(v1.isGreaterThan(v1b));
        assertFalse(v1.equals(null));
        assertFalse(v1.equals(v2));

        assertTrue(Objects.requireNonNull(Version.parse("1.0.0")).isLessThan(Version.parse("2.0.0")));
        assertTrue(Objects.requireNonNull(Version.parse("2.0.0")).isLessThan(Version.parse("2.1.0")));
        assertTrue(Objects.requireNonNull(Version.parse("2.1.0")).isLessThan(Version.parse("2.1.1")));

        assertTrue(Objects.requireNonNull(Version.parse("2.1.1")).isGreaterThan(Version.parse("2.1.0")));
        assertTrue(Objects.requireNonNull(Version.parse("2.1.0")).isGreaterThan(Version.parse("2.0.0")));
        assertTrue(Objects.requireNonNull(Version.parse("2.0.0")).isGreaterThan(Version.parse("1.0.0")));

        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha")).isLessThan(Version.parse("1.0.0-alpha.1")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha.1")).isLessThan(Version.parse("1.0.0-alpha.beta")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha.beta")).isLessThan(Version.parse("1.0.0-beta")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-beta")).isLessThan(Version.parse("1.0.0-beta.2")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-beta.2")).isLessThan(Version.parse("1.0.0-beta.11")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-beta.11")).isLessThan(Version.parse("1.0.0-rc.1")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-rc.1")).isLessThan(Version.parse("1.0.0")));

        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha-alpha.1")).isLessThan(Version.parse("1.0.0-alpha-alpha.1-test")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha-alpha.1")).isGreaterThan(Version.parse("1.0.0-alpha-alpha.1-0")));

        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha-alpha.1-test")).isGreaterThan(Version.parse("1.0.0-alpha-alpha.1")));
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha-alpha.1-0")).isLessThan(Version.parse("1.0.0-alpha-alpha.1")));

        assertTrue(Objects.requireNonNull(Version.parse("1.0.0-alpha-alpha.1")).equals(Version.parse("1.0.0-alpha.1-alpha")));
    }

    @Test
    void updates() {
        assertTrue(Objects.requireNonNull(Version.parse("1.0.0")).isUpdateFor(Version.parse("0.1.0")));
        assertTrue(Objects.requireNonNull(Version.parse("1.1.0")).isUpdateFor(Version.parse("1.0.0")));
        assertTrue(Objects.requireNonNull(Version.parse("2.1.0")).isUpdateFor(Version.parse("1.1.0")));

        assertFalse(Objects.requireNonNull(Version.parse("1.0.0")).isUpdateFor(Version.parse("2.0.0")));

        assertTrue(Objects.requireNonNull(Version.parse("1.1.0")).isCompatibleUpdateFor(Version.parse("1.0.0")));

        assertFalse(Objects.requireNonNull(Version.parse("1.0.0")).isCompatibleUpdateFor(Version.parse("0.1.0")));
        assertFalse(Objects.requireNonNull(Version.parse("2.1.0")).isCompatibleUpdateFor(Version.parse("1.1.0")));
    }
}
