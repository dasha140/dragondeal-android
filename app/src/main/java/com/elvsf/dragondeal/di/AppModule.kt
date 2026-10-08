package com.elvsf.dragondeal.di

import android.content.Context
import com.elvsf.dragondeal.BuildConfig
import com.elvsf.dragondeal.api.AuthApi
import com.elvsf.dragondeal.api.AuthInterceptor
import com.elvsf.dragondeal.api.DisputeApi
import com.elvsf.dragondeal.api.ListingApi
import com.elvsf.dragondeal.api.OrderApi
import com.elvsf.dragondeal.api.OrderRepository
import com.elvsf.dragondeal.api.ReviewApi
import com.elvsf.dragondeal.api.SupportApi
import com.elvsf.dragondeal.api.UploadApi
import com.elvsf.dragondeal.api.UserApi
import com.elvsf.dragondeal.api.chat.ChatApi
import com.elvsf.dragondeal.api.chat.ChatRepository
import com.elvsf.dragondeal.api.payment.PaymentApi
import com.elvsf.dragondeal.api.payment.PaymentRepository
import com.elvsf.dragondeal.storage.TokenStorage
import com.elvsf.dragondeal.viewmodel.ChatViewModel
import com.elvsf.dragondeal.viewmodel.ListingDetailViewModel
import com.elvsf.dragondeal.viewmodel.ListingsViewModel
import com.elvsf.dragondeal.viewmodel.MyListingsViewModel
import com.elvsf.dragondeal.viewmodel.OrderDetailViewModel
import com.elvsf.dragondeal.viewmodel.OrdersViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Koin DI module — written in Kotlin to access Koin's DSL natively.
 *
 * Provides:
 * - TokenStorage, AuthInterceptor, OkHttpClient, Retrofit
 * - All API interfaces
 * - All Repositories
 * - All ViewModels
 */
val appModule = module {
    // ─── Core singletons ──────────────────────────────────────────
    single<TokenStorage> { TokenStorage(androidContext()) }
    single<AuthInterceptor> { AuthInterceptor(get()) }

    single<OkHttpClient> {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .addInterceptor(logging)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ─── API interfaces ───────────────────────────────────────────
    single<AuthApi> { get<Retrofit>().create(AuthApi::class.java) }
    single<UserApi> { get<Retrofit>().create(UserApi::class.java) }
    single<ListingApi> { get<Retrofit>().create(ListingApi::class.java) }
    single<OrderApi> { get<Retrofit>().create(OrderApi::class.java) }
    single<ChatApi> { get<Retrofit>().create(ChatApi::class.java) }
    single<PaymentApi> { get<Retrofit>().create(PaymentApi::class.java) }
    single<ReviewApi> { get<Retrofit>().create(ReviewApi::class.java) }
    single<DisputeApi> { get<Retrofit>().create(DisputeApi::class.java) }
    single<UploadApi> { get<Retrofit>().create(UploadApi::class.java) }
    single<SupportApi> { get<Retrofit>().create(SupportApi::class.java) }

    // ─── Repositories ─────────────────────────────────────────────
    single<OrderRepository> { OrderRepository(get()) }
    single<ChatRepository> { ChatRepository(get()) }
    single<PaymentRepository> { PaymentRepository(get()) }

    // ─── ViewModels ───────────────────────────────────────────────
    viewModel { ListingsViewModel() }
    viewModel { ListingDetailViewModel() }
    viewModel { OrderDetailViewModel() }
    viewModel { MyListingsViewModel() }
    viewModel { ChatViewModel() }
    viewModel { OrdersViewModel() }
}
