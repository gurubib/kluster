package hu.gurubib.util.dao

import hu.gurubib.domain.common.QueryParams
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder

typealias QueryExpression = SqlExpressionBuilder.() -> Op<Boolean>

interface ExposedQueryExpressionHolder {
    fun buildQueryExpression(): QueryExpression
}

interface ExposedQueryBuilder : ExposedQueryExpressionHolder, QueryParams
