/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan

import java.io.Serializable

interface CompilerVersion : Serializable {
    val meta: MetaVersion
    val major: Int
    val minor: Int
    val maintenance: Int
    val milestone: Int
    val build: Int

    fun toString(showMeta: Boolean, showBuild: Boolean): String

    companion object {
        // major.minor.patch-meta-build where patch, meta and build are optional.
        private val versionPattern = "(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-M(\\p{Digit}))?(?:-(\\p{Alpha}\\p{Alnum}*))?(?:-(\\d+))?".toRegex()

        fun fromString(version: String): CompilerVersion {
            val (major, minor, maintenance, milestone, metaString, build) =
                    versionPattern.matchEntire(version)?.destructured
                        ?: throw IllegalArgumentException("Cannot parse Kotlin/Native version: $version")

            return CompilerVersionImpl(
                MetaVersion.findAppropriate(metaString),
                major.toInt(),
                minor.toInt(),
                maintenance.toIntOrNull() ?: 0,
                milestone.toIntOrNull() ?: -1,
                build.toIntOrNull() ?: -1
            )
        }
    }
}

fun String.parseCompilerVersion() = CompilerVersion.fromString(this)

data class CompilerVersionImpl(
    override val meta: MetaVersion = MetaVersion.DEV,
    override val major: Int,
    override val minor: Int,
    override val maintenance: Int,
    override val milestone: Int = 0,
    override val build: Int = -1
) : CompilerVersion {

    override fun toString(showMeta: Boolean, showBuild: Boolean) = buildString {
        append(major)
        append('.')
        append(minor)
        if (maintenance != 0) {
            append('.')
            append(maintenance)
        }
        if (milestone != -1) {
            append("-M")
            append(milestone)
        }
        if (showMeta) {
            append('-')
            append(meta.metaString)
        }
        if (showBuild && build != -1) {
            append('-')
            append(build)
        }
    }

    private val isRelease: Boolean
        get() = meta == MetaVersion.RELEASE

    private val versionString by lazy { toString(!isRelease, !isRelease) }

    override fun toString() = versionString
}
