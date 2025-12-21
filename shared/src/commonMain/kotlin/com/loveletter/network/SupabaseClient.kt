package com.loveletter.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClientProvider {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Auth configuration
            }
            install(Postgrest) {
                // Postgrest configuration
            }
            install(Realtime) {
                // Realtime configuration
            }
            install(Storage) {
                // Storage configuration
            }
        }
    }
}
