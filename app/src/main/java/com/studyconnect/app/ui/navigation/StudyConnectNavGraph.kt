package com.studyconnect.app.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.studyconnect.app.ui.screens.auth.LoginScreen
import com.studyconnect.app.ui.screens.auth.SignUpScreen
import com.studyconnect.app.ui.screens.chat.ChatListScreen
import com.studyconnect.app.ui.screens.chat.ChatRoomScreen
import com.studyconnect.app.ui.screens.chat.CreateRoomScreen
import com.studyconnect.app.ui.screens.profile.ProfileScreen
import com.studyconnect.app.ui.screens.splash.SplashScreen
import com.studyconnect.app.ui.viewmodel.AuthViewModel
import com.studyconnect.app.ui.viewmodel.ChatListViewModel
import com.studyconnect.app.ui.viewmodel.ChatRoomViewModel
import com.studyconnect.app.ui.viewmodel.CreateRoomViewModel
import com.studyconnect.app.ui.viewmodel.ProfileViewModel

@Composable
fun StudyConnectNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.Splash,
        modifier = modifier
    ) {
        composable(NavDestinations.Splash) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(NavDestinations.Login) {
                        popUpTo(NavDestinations.Splash) { inclusive = true }
                    }
                },
                onNavigateToChats = {
                    navController.navigate(NavDestinations.ChatList) {
                        popUpTo(NavDestinations.Splash) { inclusive = true }
                    }
                }
            )
        }

        navigation(
            route = NavDestinations.AuthGraph,
            startDestination = NavDestinations.Login
        ) {
            composable(NavDestinations.Login) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavDestinations.AuthGraph)
                }
                val authViewModel: AuthViewModel = viewModel(parentEntry)
                val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
                LoginScreen(
                    state = uiState,
                    snackbarHostState = snackbarHostState,
                    onEmailChanged = authViewModel::updateEmail,
                    onPasswordChanged = authViewModel::updatePassword,
                    onSignIn = authViewModel::signIn,
                    onNavigateToSignUp = { navController.navigate(NavDestinations.SignUp) },
                    authEvents = authViewModel.authEvents,
                    onAuthenticated = {
                        navController.navigate(NavDestinations.ChatList) {
                            popUpTo(NavDestinations.Splash) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavDestinations.SignUp) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(NavDestinations.AuthGraph)
                }
                val authViewModel: AuthViewModel = viewModel(parentEntry)
                val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
                SignUpScreen(
                    state = uiState,
                    snackbarHostState = snackbarHostState,
                    onEmailChanged = authViewModel::updateEmail,
                    onPasswordChanged = authViewModel::updatePassword,
                    onDisplayNameChanged = authViewModel::updateDisplayName,
                    onSignUp = authViewModel::signUp,
                    onNavigateBack = { navController.popBackStack() },
                    authEvents = authViewModel.authEvents,
                    onAuthenticated = {
                        navController.navigate(NavDestinations.ChatList) {
                            popUpTo(NavDestinations.Splash) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(NavDestinations.ChatList) {
            val chatListViewModel: ChatListViewModel = viewModel()
            val uiState by chatListViewModel.uiState.collectAsStateWithLifecycle()
            ChatListScreen(
                state = uiState,
                snackbarHostState = snackbarHostState,
                onRoomSelected = { room ->
                    navController.navigate(NavDestinations.chatRoomRoute(room.id))
                },
                onProfileClick = { navController.navigate(NavDestinations.Profile) },
                onCreateRoom = { navController.navigate(NavDestinations.CreateRoom) },
                onSignOut = {
                    chatListViewModel.signOut()
                    navController.navigate(NavDestinations.Login) {
                        popUpTo(NavDestinations.Splash) { inclusive = true }
                    }
                },
                onErrorDismissed = chatListViewModel::clearError
            )
        }

        composable(NavDestinations.CreateRoom) {
            val createRoomViewModel: CreateRoomViewModel = viewModel()
            val uiState by createRoomViewModel.uiState.collectAsStateWithLifecycle()
            CreateRoomScreen(
                state = uiState,
                snackbarHostState = snackbarHostState,
                onTitleChanged = createRoomViewModel::updateTitle,
                onMemberEmailsChanged = createRoomViewModel::updateMemberInput,
                onCreateRoom = createRoomViewModel::createRoom,
                onBack = { navController.popBackStack() },
                onErrorDismissed = createRoomViewModel::clearError,
                onRoomCreated = {
                    createRoomViewModel.consumeRoomCreated()
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = NavDestinations.chatRoomPattern,
            arguments = listOf(
                navArgument(NavDestinations.ChatRoomIdArg) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString(NavDestinations.ChatRoomIdArg)
            if (roomId.isNullOrBlank()) {
                navController.popBackStack()
            } else {
                val factory = remember(roomId) { ChatRoomViewModel.Factory(roomId) }
                val chatRoomViewModel: ChatRoomViewModel = viewModel(factory = factory)
                val uiState by chatRoomViewModel.uiState.collectAsStateWithLifecycle()
                ChatRoomScreen(
                    state = uiState,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    onProfileClick = { navController.navigate(NavDestinations.Profile) },
                    onMessageChanged = chatRoomViewModel::updateMessageInput,
                    onSendMessage = chatRoomViewModel::sendMessage,
                    onErrorDismissed = chatRoomViewModel::clearError
                )
            }
        }

        composable(NavDestinations.Profile) {
            val profileViewModel: ProfileViewModel = viewModel()
            val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
            ProfileScreen(
                state = uiState,
                snackbarHostState = snackbarHostState,
                onDisplayNameChanged = profileViewModel::updateDisplayName,
                onSave = profileViewModel::saveProfile,
                onBack = { navController.popBackStack() },
                onErrorDismissed = profileViewModel::clearError,
                onSuccessDismissed = profileViewModel::clearSuccess
            )
        }
    }
}
