/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.twelve.query

typealias Column = String

sealed interface Query {
    fun build(): String

    companion object {
        const val ARG = "?"
        const val NULL = "NULL"
    }
}

enum class Operator(val symbol: String) {
    AND("AND"),
    OR("OR"),
    EQUALS("="),
    NOT_EQUALS("!="),
    LIKE("LIKE"),
    IS("IS"),
}

class LogicalOp(private val lhs: Query, private val op: Operator, private val rhs: Query) : Query {
    override fun build() = "(${lhs.build()}) ${op.symbol} (${rhs.build()})"
}

class StringOp<T>(private val lhs: Column, private val op: Operator, private val rhs: T) : Query {
    override fun build() = "$lhs ${op.symbol} $rhs"
}

class In<T>(private val value: T, private val values: Collection<T>) : Query {
    override fun build() = "$value IN (${values.joinToString(", ")})"
}

infix fun Query.and(other: Query) = LogicalOp(this, Operator.AND, other)
infix fun Query.or(other: Query) = LogicalOp(this, Operator.OR, other)

infix fun Column.eq(other: String) = StringOp(this, Operator.EQUALS, other)
infix fun Column.neq(other: String) = StringOp(this, Operator.NOT_EQUALS, other)
infix fun Column.like(other: String) = StringOp(this, Operator.LIKE, other)
infix fun Column.`is`(other: String) = StringOp(this, Operator.IS, other)
infix fun <T> Column.`in`(values: Collection<T>) = In(this, values)

inline fun query(block: () -> Query) = block().build()
