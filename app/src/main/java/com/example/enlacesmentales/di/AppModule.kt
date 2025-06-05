package com.example.enlacesmentales.di


import com.example.enlacesmentales.data.remote.FirebaseAuthService
import com.example.enlacesmentales.data.remote.FirebaseFirestoreService
import com.example.enlacesmentales.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthService(
        auth: FirebaseAuth
    ): FirebaseAuthService = FirebaseAuthService(auth)

    @Provides
    @Singleton
    fun provideFirestoreService(
        firestore: FirebaseFirestore
    ): FirebaseFirestoreService = FirebaseFirestoreService(firestore)

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        authService: FirebaseAuthService,
        firestoreService: FirebaseFirestoreService
    ): AuthRepository = AuthRepository(auth, authService, firestoreService)
}
