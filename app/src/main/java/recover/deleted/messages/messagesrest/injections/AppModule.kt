package recover.deleted.messages.messagesrest.injections

import android.webkit.CookieManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import recover.deleted.messages.messagesrest.BuildConfig
import recover.deleted.messages.messagesrest.database.RampDao
import recover.deleted.messages.messagesrest.database.RampDatabaseOpenHelper
import recover.deleted.messages.messagesrest.database.RampDatabaseSchema
import recover.deleted.messages.messagesrest.gates.api.GateApi
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import ro.andob.outofroom.EntityManager
import ro.andob.outofroom.system_sqlite.toEntityManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
            .addNetworkInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.HEADERS)
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideGateApi(
        retrofit: Retrofit
    ): GateApi = retrofit.create(GateApi::class.java)

    @Provides
    @Singleton
    fun provideEntityManager(
        rampDatabaseOpenHelper: RampDatabaseOpenHelper
    ): EntityManager = rampDatabaseOpenHelper.toEntityManager()

    @Provides
    @Singleton
    fun provideRampDao(
        rampDatabaseSchema: RampDatabaseSchema,
        entityManager : EntityManager
    ): RampDao = RampDao(
        schema = rampDatabaseSchema,
        entityManager = entityManager
    )

    @Provides
    @Singleton
    fun provideCookieManager(): CookieManager = CookieManager.getInstance()
}