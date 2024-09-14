package com.erdees.places.domain.permissions

import org.koin.dsl.module

val permissionsModule = module {
    single<PermissionChecker> { PermissionCheckerImpl() }
}