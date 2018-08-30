package com.github.hexocraft.lib.utilities.version;

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

import com.github.hexocraft.lib.MineMock;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SemVerTest {

    private JavaPlugin plugin;


    @BeforeAll
    public void init() {
        MineMock.start();
        plugin = MineMock.createFakePlugin("Fake plugin", "1.0.0");
    }

    @AfterAll
    public static void cleanUp() {
        MineMock.stop();
    }

    @Test
    void constructors() {
        assertEquals(new SemVer(1, 2, 2).toString(), "1.2.2");
        assertEquals(new SemVer(1, 2, 2, "alpha.1").toString(), "1.2.2-alpha.1");
        assertEquals(new SemVer(1, 2, 2, "alpha.1-alpha").toString(), "1.2.2-alpha.1-alpha");
        assertEquals(new SemVer(1, 2, 2, "alpha.1", "546").toString(), "1.2.2-alpha.1+546");
        assertEquals(new SemVer(plugin.getDescription().getVersion()).toString(), "1.0.0");

        assertThrows(IllegalArgumentException.class, () -> new SemVer(1, 2, 2, "rele..ase", "build"));
        assertThrows(IllegalArgumentException.class, () -> new SemVer(1, 2, 2, "release-something", "..build"));
        assertThrows(IllegalArgumentException.class, () -> new SemVer(1, 2, 2, "rele--ase", "build"));
        assertThrows(IllegalArgumentException.class, () -> new SemVer(1, 2, 2, "release-something", "--build"));
        assertThrows(IllegalArgumentException.class, () -> new SemVer(1, 2, 2, "release", "+build"));
        assertThrows(IllegalArgumentException.class, () -> new SemVer(1, 2, 2, "1.2.3-rele--ase", "build"));
    }

    @Test
    void parser() {
        assertTrue(SemVer.isSemver("0.1.2"));
        assertTrue(SemVer.isSemver("1.2.3"));
        assertTrue(SemVer.isSemver("10.20.3"));
        assertTrue(SemVer.isSemver("1.2.3-alpha.23-pre"));
        assertTrue(SemVer.isSemver("12.12.3-123.hexagon+dontmakemecompileplea.se"));
        assertTrue(SemVer.isSemver("1.2.3-alpha-dev.51-something+mybuild-1-4-1975-clang"));
        assertTrue(SemVer.isSemver("4.3.22+mybuild"));
        assertTrue(SemVer.isSemver("4.1.405+hexa.13331-objectfiles"));

        assertTrue(SemVer.isSemver(plugin));

        assertFalse(SemVer.isSemver("1.0"));

        assertFalse(SemVer.isSemver("01.2.3"));
        assertFalse(SemVer.isSemver("1.02.3"));
        assertFalse(SemVer.isSemver("2.3.04"));
        assertFalse(SemVer.isSemver("a.1.1"));
        assertFalse(SemVer.isSemver("1.a.1"));
        assertFalse(SemVer.isSemver("1.1.a"));
        assertFalse(SemVer.isSemver("1.2.3-rele..ase+build"));
        assertFalse(SemVer.isSemver("1.2.3-release-something+..build"));
        assertFalse(SemVer.isSemver("1.2.3-rele--ase+build"));
        assertFalse(SemVer.isSemver("1.2.3-release-something+--build"));
        assertFalse(SemVer.isSemver("1.2.3-release++build"));
        assertFalse(SemVer.isSemver("1.2.3+-release-something-build"));


    }

    @Test
    void stability() {
        assertFalse(SemVer.parse("0.1.2").isStable());

        assertTrue(SemVer.parse("1.2.3").isStable());
        assertTrue(SemVer.parse("10.20.3").isStable());
        assertTrue(SemVer.parse("4.3.22+mybuild").isStable());
        assertTrue(SemVer.parse("4.1.405+hexa.13331-objectfiles").isStable());

        assertFalse(SemVer.parse("1.2.3-alpha.23-pre").isStable());
        assertFalse(SemVer.parse("12.12.3-123.hexagon+dontmakemecompileplea.se").isStable());
        assertFalse(SemVer.parse("1.2.3-alpha-dev.51-something+mybuild-1-4-1975-clang").isStable());
    }

    /**
     * Precedence refers to how versions are compared to each other when ordered. Precedence MUST be calculated by
     * separating the version into major, minor, patch and pre-release identifiers in that order (Build metadata does
     * not figure into precedence). Precedence is determined by the first difference when comparing each of these
     * identifiers from left to right as follows: Major, minor, and patch versions are always compared numerically.
     * <p>
     * Example: 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1.
     * <p>
     * When major, minor, and patch are equal, a pre-release version has lower precedence than a normal version.
     * <p>
     * Example: 1.0.0-alpha < 1.0.0.
     * <p>
     * Precedence for two pre-release versions with the same major, minor, and patch version MUST be determined by
     * comparing each dot separated identifier from left to right until a difference is found as follows: identifiers
     * consisting of only digits are compared numerically and identifiers with letters or hyphens are compared lexically
     * in ASCII sort order. Numeric identifiers always have lower precedence than non-numeric identifiers. A larger set
     * of pre-release fields has a higher precedence than a smaller set, if all of the preceding identifiers are equal.
     * <p>
     * Example: 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1
     * < 1.0.0.
     * <p>
     * Example: 1.0.0-alpha-alpha.1 < 1.0.0-alpha-alpha.1-0
     * <p>
     * Example: 1.0.0-alpha-alpha.1 = 1.0.0-alpha.1-alpha
     */
    @Test
    void precedence() {
        assertEquals(SemVer.parse("1.2.3"), SemVer.parse("1.2.3"));
        assertEquals(SemVer.parse("1.0.0-alpha"), SemVer.parse("1.0.0-alpha"));
        assertEquals(SemVer.parse("1.0.0-alpha-alpha.1"), SemVer.parse("1.0.0-alpha-alpha.1"));
        assertEquals(SemVer.parse("1.0.0-alpha-alpha.1"), SemVer.parse("1.0.0-alpha.1-alpha"));

        SemVer v1 = SemVer.parse("1.0.0");
        SemVer v2 = SemVer.parse("2.0.0");

        assertTrue(v1.equals(v1));
        assertFalse(v1.equals(null));
        assertFalse(v1.equals(v2));

        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0")).isLessThan(SemVer.parse("2.0.0")));
        assertTrue(Objects.requireNonNull(SemVer.parse("2.0.0")).isLessThan(SemVer.parse("2.1.0")));
        assertTrue(Objects.requireNonNull(SemVer.parse("2.1.0")).isLessThan(SemVer.parse("2.1.1")));

        assertTrue(Objects.requireNonNull(SemVer.parse("2.1.1")).isGreaterThan(SemVer.parse("2.1.0")));
        assertTrue(Objects.requireNonNull(SemVer.parse("2.1.0")).isGreaterThan(SemVer.parse("2.0.0")));
        assertTrue(Objects.requireNonNull(SemVer.parse("2.0.0")).isGreaterThan(SemVer.parse("1.0.0")));

        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha")).isLessThan(SemVer.parse("1.0.0-alpha.1")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha.1")).isLessThan(SemVer.parse("1.0.0-alpha.beta")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha.beta")).isLessThan(SemVer.parse("1.0.0-beta")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-beta")).isLessThan(SemVer.parse("1.0.0-beta.2")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-beta.2")).isLessThan(SemVer.parse("1.0.0-beta.11")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-beta.11")).isLessThan(SemVer.parse("1.0.0-rc.1")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-rc.1")).isLessThan(SemVer.parse("1.0.0")));

        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha-alpha.1")).isLessThan(SemVer.parse("1.0.0-alpha-alpha.1-test")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha-alpha.1")).isGreaterThan(SemVer.parse("1.0.0-alpha-alpha.1-0")));

        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha-alpha.1-test")).isGreaterThan(SemVer.parse("1.0.0-alpha-alpha.1")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha-alpha.1-0")).isLessThan(SemVer.parse("1.0.0-alpha-alpha.1")));

        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0-alpha-alpha.1")).equals(SemVer.parse("1.0.0-alpha.1-alpha")));
    }

    @Test
    void updates() {
        assertTrue(Objects.requireNonNull(SemVer.parse("1.0.0")).isUpdateFor(SemVer.parse("0.1.0")));
        assertTrue(Objects.requireNonNull(SemVer.parse("1.1.0")).isUpdateFor(SemVer.parse("1.0.0")));
        assertTrue(Objects.requireNonNull(SemVer.parse("2.1.0")).isUpdateFor(SemVer.parse("1.1.0")));

        assertFalse(Objects.requireNonNull(SemVer.parse("1.0.0")).isUpdateFor(SemVer.parse("2.0.0")));

        assertTrue(Objects.requireNonNull(SemVer.parse("1.1.0")).isUpdateCompatibleFor(SemVer.parse("1.0.0")));

        assertFalse(Objects.requireNonNull(SemVer.parse("1.0.0")).isUpdateCompatibleFor(SemVer.parse("0.1.0")));
        assertFalse(Objects.requireNonNull(SemVer.parse("2.1.0")).isUpdateCompatibleFor(SemVer.parse("1.1.0")));
    }
}
