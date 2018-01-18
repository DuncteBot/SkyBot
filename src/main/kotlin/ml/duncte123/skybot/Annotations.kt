/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:JvmName("SkyBotAnnotationsKt")
@file:Author(nickname = "Sanduhr32", author = "Maurice R S")

package ml.duncte123.skybot

import kotlin.annotation.AnnotationTarget.*

/**
 * Specifies the first version of Skybot where a declaration has appeared.
 * Using the declaration and specifying an older API version (via the `-api-version` command line option) will result in an error.
 *
 * @property version the version in the following formats: `<major>.<minor>` or `<major>.<minor>.<patch>`, where major, minor and patch
 * are non-negative integer numbers without leading zeros.
 */
@Target(CLASS, PROPERTY, FIELD, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS, EXPRESSION, LOCAL_VARIABLE)
@Retention
@SinceSkybot("3.51.10")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
public annotation class SinceSkybot(val version: String = BuildConfig.VERSION)

/**
 * Specifies any part in any kind of files.
 *
 * @property specificPart is the exact part of any part which needs documentation
 */
@Target(ANNOTATION_CLASS, CLASS, PROPERTY, FIELD, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS, EXPRESSION, FILE, TYPE, VALUE_PARAMETER, TYPE_PARAMETER)
@Retention
@SinceSkybot("3.51.10")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
public annotation class DocumentationNeeded(vararg val specificPart: String = ["everything", "class"])

/**
 * Specifies any part in any kind of files.
 *
 * @property author is the real name of the author
 * @property nickname is the nickname of the author
 */
@Target(ANNOTATION_CLASS, CLASS, PROPERTY, FIELD, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS, EXPRESSION, FILE, TYPE, VALUE_PARAMETER, TYPE_PARAMETER)
@Retention
@SinceSkybot("3.51.10")
@Author(nickname = "Sanduhr32", author = "Maurice R S")
public annotation class Author(val author: String = "Duncan Sterken", val nickname: String = "duncte123")