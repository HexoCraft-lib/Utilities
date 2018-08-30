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

import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Store version information
 */
@SuppressWarnings("WeakerAccess")
public final class Version implements Comparable<Version> {

    /**
     * Major version number.
     */
    public final int major;

    /**
     * Minor version number.
     */
    public final int minor;

    /**
     * Patch level.
     */
    public final int patch;

    /**
     * If the version is Semantic Versioning compliant.
     */
    private final SemVer semver;


    /**
     * @param major major version number (must not be negative).
     * @param minor minor version number (must not be negative).
     * @param patch patch level (must not be negative).
     */
    public Version(@Nonnegative int major, @Nonnegative int minor, @Nonnegative int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.semver = new SemVer(major, minor, patch);
    }

    /**
     * Construct a {@link Version} object by parsing a string.
     *
     * @param version semver string to parse
     */
    public Version(@Nonnull String version) {

        // Check if it is Semantic Versioning compliant
        this.semver = SemVer.parse(version);

        // The version string is Semantic Versioning compliant
        if (this.semver != null) {
            this.major = semver.major;
            this.minor = semver.minor;
            this.patch = semver.patch;
        }

        // Try to extract a version number
        else {
            Pattern pattern1 = Pattern.compile("(?:.*)([0-9]+)\\.([0-9]+)\\.([0-9]+).*", Pattern.CASE_INSENSITIVE);
            Matcher matcher1 = pattern1.matcher(version);

            if (matcher1.matches() && matcher1.groupCount() == 3) {
                this.major = Integer.parseInt(matcher1.group(1));
                this.minor = Integer.parseInt(matcher1.group(2));
                this.patch = Integer.parseInt(matcher1.group(3));
                return;
            }

            Pattern pattern2 = Pattern.compile("(?:.*)([0-9]+)\\.([0-9]+).*", Pattern.CASE_INSENSITIVE);
            Matcher matcher2 = pattern2.matcher(version);

            if (matcher2.matches() && matcher2.groupCount() == 2) {
                this.major = Integer.parseInt(matcher2.group(1));
                this.minor = Integer.parseInt(matcher2.group(2));
                this.patch = 0;
                return;
            }

            // No version found in the string
            throw new IllegalArgumentException("Invalid version number: " + version);
        }
    }


    // Helper functions
    //--------------------------------------------------------------------------

    public boolean isSemVer() {
        return semver != null;
    }

    @Override
    public String toString() {
        if (semver != null)
            return semver.toString();
        else {
            return String.valueOf(major) + '.' + minor + '.' + patch;
        }
    }

    public boolean isGreaterThan(@Nonnull Version other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessThan(@Nonnull Version other) {
        return this.compareTo(other) < 0;
    }

    /**
     * Check if this version is an update.
     *
     * @param other the other version object
     *
     * @return true if this version is newer than the other one.
     */
    public boolean isUpdateFor(@Nonnull Version other) {
        return this.isGreaterThan(other);
    }

    /**
     * Check if this version is a compatible update.
     *
     * @param other the other version object.
     *
     * @return true if this version is newer and both have the same major version.
     */
    public boolean isCompatibleUpdateFor(@Nonnull Version other) {
        return this.isUpdateFor(other) && (this.major == other.major);
    }


    // Static helper functions
    //--------------------------------------------------------------------------

    /**
     * Construct a {@link Version} object by parsing a string.
     *
     * @param version version in flat string format
     *
     * @return {@link Version} if the string is semver compliant else null
     */
    public static Version parse(String version) {
        try {
            return new Version(version);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Create a {@link Version} object from {@link JavaPlugin}
     *
     * @param plugin Instance of {@link JavaPlugin}
     *
     * @return {@link Version} if the string contain a version number else null
     */
    public static Version parse(JavaPlugin plugin) {
        return Version.parse(plugin.getDescription().getVersion());
    }

    /**
     * Check if the string is semver compliant.
     *
     * @param version update to test
     *
     * @return true if valid
     */
    public static boolean isSemVer(String version) {
        return SemVer.parse(version) != null;
    }

    /**
     * Check if the string is semver compliant.
     *
     * @param plugin Instance of {@link JavaPlugin}
     *
     * @return true if valid
     */
    public static boolean isSemVer(JavaPlugin plugin) {
        return SemVer.parse(plugin) != null;
    }


    // Comparison
    //--------------------------------------------------------------------------

    /**
     * Returns a negative integer, a positive integer, or zero as compareTo has judged the "left-hand" side as less
     * than, greater than, or equal to the "right-hand" side.
     *
     * @return final comparison result
     */
    @Override
    public int compareTo(Version other) {
        if (semver != null && other.isSemVer()) {
            return semver.compareTo(other.semver);
        } else {
            // Major
            int comparison = ((major < other.major) ? -1 : ((major > other.major) ? 1 : 0));
            if (comparison != 0) return comparison;

            // Minor
            comparison = ((minor < other.minor) ? -1 : ((minor > other.minor) ? 1 : 0));
            if (comparison != 0) return comparison;

            // Patch
            comparison = ((patch < other.patch) ? -1 : ((patch > other.patch) ? 1 : 0));
            if (comparison != 0) return comparison;
        }

        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;

        Version version = (Version) other;

        if (semver != null) {
            return semver.equals(version.semver);
        } else {

            return (major == version.major && minor == version.minor && patch == version.patch);
        }
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        return result;
    }
}
