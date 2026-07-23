package com.example.data.response

data class HealthResponse(
    val status: String,
    val components: HealthComponents? = null,
    val groups: List<String>? = null
)

data class HealthComponents(
    val db: DbComponent? = null,
    val diskSpace: DiskSpaceComponent? = null,
    val livenessState: StatusComponent? = null,
    val ping: StatusComponent? = null,
    val readinessState: StatusComponent? = null,
    val ssl: SslComponent? = null
)

data class DbComponent(
    val status: String,
    val details: DbDetails? = null
)

data class DbDetails(
    val database: String? = null,
    val validationQuery: String? = null,
    val error: String? = null
)

data class DiskSpaceComponent(
    val status: String,
    val details: DiskSpaceDetails? = null
)

data class DiskSpaceDetails(
    val total: Long,
    val free: Long,
    val threshold: Long,
    val path: String,
    val exists: Boolean
)

data class StatusComponent(
    val status: String
)

data class SslComponent(
    val status: String,
    val details: SslDetails? = null
)

data class SslDetails(
    val expiringChains: List<String>? = null,
    val invalidChains: List<String>? = null,
    val validChains: List<String>? = null
)