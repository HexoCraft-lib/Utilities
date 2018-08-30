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

import com.github.hexocraft.lib.utilities.comparator.NumberAwareStringComparator;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>A light implementation of <a href="https://semver.org/">Semantic Versioning</a>
 *
 * <p>Check a given a version number MAJOR.MINOR.PATCH-RELEASES+BUILD:
 *
 * <ul>
 * <li>1. MAJOR version: when you make incompatible API changes,
 * <li>2. MINOR version: when you add functionality in a backwards-compatible manner, and
 * <li>3. PATCH version: when you make backwards-compatible bug fixes.
 * </ul>
 *
 * <p>Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.
 */
@SuppressWarnings("WeakerAccess")
public final class SemVer implements Comparable<SemVer> {

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
     * Pre-release tags (potentially empty, but never null).
     */
    private final @NonNull List<String> preRelease;

    /**
     * Build meta data tags (potentially empty, but never null).
     */
    private final @NonNull String buildMetaData;

    /**
     * @param major major version number (must not be negative).
     * @param minor minor version number (must not be negative).
     * @param patch patch level (must not be negative).
     */
    public SemVer(@NonNegative int major, @NonNegative int minor, @NonNegative int patch) {
        this(major, minor, patch, Collections.emptyList(), "");
    }

    /**
     * @param major major version number (must not be negative).
     * @param minor minor version number (must not be negative).
     * @param patch patch level (must not be negative).
     * @param preRelease pre release identifiers.
     */
    public SemVer(@NonNegative int major, @NonNegative int minor, @NonNegative int patch, @NonNull String preRelease) {
        this(major, minor, patch, new ArrayList<>(Collections.singletonList(preRelease)), "");
    }

    /**
     * @param major major version number (must not be negative).
     * @param minor minor version number (must not be negative).
     * @param patch patch level (must not be negative).
     * @param preRelease pre release identifiers.
     * @param buildMetaData build meta identifier.
     */
    public SemVer(@NonNegative int major, @NonNegative int minor, @NonNegative int patch, @NonNull String preRelease, @NonNull String buildMetaData) {
        this(major, minor, patch, new ArrayList<>(Collections.singletonList(preRelease)), buildMetaData);
    }

    /**
     * @param major major version number (must not be negative).
     * @param minor minor version number (must not be negative).
     * @param patch patch level (must not be negative).
     * @param preRelease pre release identifiers.
     * @param buildMetaData build meta identifier.
     */
    public SemVer(@NonNegative int major, @NonNegative int minor, @NonNegative int patch, @NonNull List<String> preRelease, @NonNull String buildMetaData) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preRelease = validatePreRelease(preRelease);
        this.buildMetaData = validateBuildMetaData(buildMetaData);
    }

    /**
     * Construct a {@link SemVer} object by parsing a string.
     *
     * @param version version in flat string format
     */
    public SemVer(@NonNull String version) {

        // Pattern used to validate pre-release tags
        final Pattern p = Pattern.compile("^(?!.*\\-{2}.*)(?!.*\\+{2}.*)(?!.*\\.{2}.*)(?!.*\\+\\-.*)(?!.*\\-\\+.*)(?<Major>(?!0)(\\d*)|([0^\\d]))\\.(?<Minor>(?!0)(\\d*)|([0^\\d]))\\.(?<Patch>(?!0)(\\d*)|([0^\\d]))(?![\\+\\-][^a-zA-Z0-9])(\\-(?<PreRelease>[a-zA-Z0-9\\.-]+))?(\\+(?<Build>[a-zA-Z0-9\\.-]+))?$");
        final Matcher m = p.matcher(version);

        // Pattern must match
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid version (Not Semantic Versioning compliant): " + version);
        }

        this.major = Integer.parseInt(m.group("Major"));
        this.minor = Integer.parseInt(m.group("Minor"));
        this.patch = Integer.parseInt(m.group("Patch"));
        this.preRelease = validatePreRelease(m.group("PreRelease"));
        this.buildMetaData = validateBuildMetaData(m.group("Build"));
    }


    // Helper functions
    //--------------------------------------------------------------------------

    @Override
    public @NonNull String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append(major);
        ret.append('.');
        ret.append(minor);
        ret.append('.');
        ret.append(patch);

        if(!preRelease.isEmpty()) {
            for (String pre : preRelease) {

                ret.append('-');
                ret.append(pre);
            }
        }

        if (!buildMetaData.isEmpty()) {
            ret.append('+');
            ret.append(buildMetaData);
        }

        return ret.toString();
    }

    /**
     * Convenience method to check if this is a stable version.
     *
     * @return true if the major version number is greater than zero and there are no pre release tags.
     */
    public boolean isStable() {
        return major > 0 && preRelease.isEmpty();
    }

    /**
     * Check if this version has a given pre release tag.
     *
     * @param tag the {@link SemVer#preRelease} tag to check for
     *
     * @return true if the tag is found in {@link SemVer#preRelease}.
     */
    public boolean hasPreReleaseTag(@NonNull String tag) {
        for (String s : preRelease) {
            if (s.equals(tag))
                return true;
        }
        return false;
    }

    /**
     * Check if this version has a given build Meta tags.
     *
     * @param buildMeta the {@link SemVer#buildMetaData} tag to check for.
     *
     * @return true if the tag is found in {@link SemVer#buildMetaData}.
     */
    public boolean hasBuildMetaTag(@NonNull String buildMeta) {
        return buildMetaData.equals(buildMeta);
    }

    public boolean isGreaterThan(@NonNull SemVer other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessThan(@NonNull SemVer other) {
        return this.compareTo(other) < 0;
    }

    /**
     * Check if this version is an update.
     *
     * @param other the other version object
     *
     * @return true if this version is newer than the other one.
     */
    public boolean isUpdateFor(@NonNull SemVer other) {
        return this.isGreaterThan(other);
    }

    /**
     * Check if this version is a compatible update.
     *
     * @param other the other version object.
     *
     * @return true if this version is newer and both have the same major version.
     */
    public boolean isUpdateCompatibleFor(@NonNull SemVer other) {
        return this.isUpdateFor(other) && (this.major == other.major);
    }


    // Static helper functions
    //--------------------------------------------------------------------------

    /**
     * Construct a {@link SemVer} object by parsing a string.
     *
     * @param version version in flat string format
     *
     * @return {@link SemVer} if the string is semver compliant else null
     */
    public static @Nullable SemVer parse(@NonNull String version) {
        try {
            return new SemVer(version);
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Create a {@link SemVer} object from {@link JavaPlugin}
     *
     * @param plugin Instance of {@link JavaPlugin}
     *
     * @return {@link SemVer} if the string is semver compliant else null
     */
    public static @Nullable SemVer parse(@NonNull JavaPlugin plugin) {
        return SemVer.parse(plugin.getDescription().getVersion());
    }

    /**
     * Check if the string is semver compliant.
     *
     * @param version update to test
     *
     * @return true if valid
     */
    public static boolean isSemver(@NonNull String version) {
        return SemVer.parse(version) != null;
    }

    /**
     * Check if the string is semver compliant.
     *
     * @param plugin Instance of {@link JavaPlugin}
     *
     * @return true if valid
     */
    public static boolean isSemver(@NonNull JavaPlugin plugin) {
        return SemVer.parse(plugin) != null;
    }


    // Internal functions used during validation process
    //--------------------------------------------------------------------------

    /**
     * Validate a pre-release tag
     *
     * @param preRelease Tag to validate
     *
     * @return A list of valid pre-release tags
     */
    private @NonNull List<String> validatePreRelease(@Nullable String preRelease) {
        if (preRelease == null || preRelease.isEmpty())
            return Collections.emptyList();
        return validatePreRelease(new ArrayList<>(Collections.singletonList(preRelease)));
    }

    /**
     * Validate pre-release tags
     *
     * @param preRelease Tags to validate
     *
     * @return A list of valid pre-release tags
     */
    private @NonNull List<String> validatePreRelease(@Nullable List<String> preRelease) {
        if (preRelease == null || preRelease.isEmpty())
            return Collections.emptyList();

        // Array of valid pre-release tags
        List<String> validPreReleaseTags = new ArrayList<>();

        // Pattern used to validate pre-release tags
        Pattern p = Pattern.compile("^(?!.*\\-{2}.*)(?!.*\\.{2}.*)([a-zA-Z0-9\\.\\-]+)$");

        // Loop throw all pre-release tags
        for (String pre : preRelease) {
            // Pattern must match
            if (!p.matcher(pre).matches()) {
                throw new IllegalArgumentException("Invalid pre-release tag: " + pre);
            }
            // Add to the valid tags
            validPreReleaseTags.addAll(Arrays.asList(pre.split("\\-")));
        }

        return validPreReleaseTags;
    }

    /**
     * Validate a build meta data tag
     *
     * @param buildMetaData Tag to validate
     *
     * @return build meta data tag if valid
     */
    private @NonNull String validateBuildMetaData(@Nullable String buildMetaData) {
        if (buildMetaData == null || buildMetaData.isEmpty())
            return "";

        // Pattern used to validate build meta data
        Pattern p = Pattern.compile("^(?!.*\\-{2}.*)(?!.*\\.{2}.*)([a-zA-Z0-9\\.\\-]+)$");

        // Pattern must match
        if (!p.matcher(buildMetaData).matches()) {
            throw new IllegalArgumentException("Invalid build meta data: " + buildMetaData);
        }

        return buildMetaData;
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
    public int compareTo(@NonNull SemVer other) {
        // Major
        int comparison = (Integer.compare(major, other.major));
        if (comparison != 0) return comparison;

        // Minor
        comparison = (Integer.compare(minor, other.minor));
        if (comparison != 0) return comparison;

        // Patch
        comparison = (Integer.compare(patch, other.patch));
        if (comparison != 0) return comparison;

        // Pre-release
        comparison = comparePreReleaseTo(other.preRelease);
        if (comparison != 0) return comparison;

        return 0;
    }

    private int comparePreReleaseTo(List<String> other) {

        if (this.preRelease.isEmpty() == true  && other.isEmpty() == true) return 0;
        if (this.preRelease.isEmpty() == false && other.isEmpty() == true) return -1;
        if (this.preRelease.isEmpty() == true  && other.isEmpty() == false) return 1;

        NumberAwareStringComparator comparator = NumberAwareStringComparator.INSTANCE;

        List<String> preReleaseOrdered = new ArrayList<>(this.preRelease);
        preReleaseOrdered.sort(comparator);
        List<String> otherPreReleaseOrdered = new ArrayList<>(other);
        otherPreReleaseOrdered.sort(comparator);

        if (preReleaseOrdered.size() == otherPreReleaseOrdered.size()) {
            return comparator.compare(preReleaseOrdered.get(0), otherPreReleaseOrdered.get(0));
        }
        else if (preReleaseOrdered.size() >= otherPreReleaseOrdered.size()) {
            int comparison = 0;
            for (int i = 0; i < otherPreReleaseOrdered.size(); i++) {
                comparison = comparator.compare(preReleaseOrdered.get(i), otherPreReleaseOrdered.get(i));
                if (comparison != 0) return comparison;
            }
            return 1;
        }
        else {
            int comparison = 0;
            for (int i = 0; i < preReleaseOrdered.size(); i++) {
                comparison = comparator.compare(preReleaseOrdered.get(i), otherPreReleaseOrdered.get(i));
                if (comparison != 0) return comparison;
            }
            return -1;
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;

        SemVer semVer = (SemVer) other;

        if (major != semVer.major || minor != semVer.minor || patch != semVer.patch)
            return false;
        if (!preReleaseEquals(semVer.preRelease))
            return false;
        return buildMetaData.equals(semVer.buildMetaData);
    }

    private boolean preReleaseEquals(@NonNull List<String> other) {
        if(this.preRelease.isEmpty() && other.isEmpty())
            return true;
        // At least one tag must correspond
        List<String> tmpPreRelease = new ArrayList<>(this.preRelease);
        tmpPreRelease.retainAll(other);
        return !tmpPreRelease.isEmpty();
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + patch;
        result = 31 * result + preRelease.hashCode();
        result = 31 * result + buildMetaData.hashCode();
        return result;
    }
}
