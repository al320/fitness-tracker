package com.al32.fitcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.al32.fitcheck.data.preferences.UserPreferencesRepository
import com.al32.fitcheck.ui.features.analytics.AnalyticsScreen
import com.al32.fitcheck.ui.features.dashboard.DashboardScreen
import com.al32.fitcheck.ui.features.exercise_detail.ExerciseDetailScreen
import com.al32.fitcheck.ui.features.library.CreateCustomExerciseScreen
import com.al32.fitcheck.ui.features.library.LibraryScreen
import com.al32.fitcheck.ui.features.library.TemplateEditScreen
import com.al32.fitcheck.ui.features.profile.ProfileScreen
import com.al32.fitcheck.ui.features.settings.SettingsScreen
import com.al32.fitcheck.ui.features.settings.WeeklyScheduleScreen
import com.al32.fitcheck.ui.features.workout.SessionSummaryScreen
import com.al32.fitcheck.ui.features.workout.WorkoutScreen
import com.al32.fitcheck.ui.theme.FitcheckTheme
import com.al32.fitcheck.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitcheckTheme {
                FitcheckApp()
            }
        }
    }
}

@Composable
fun FitcheckApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as FitcheckApplication
    val repository = app.repository
    val preferencesRepository = UserPreferencesRepository(context)

    val strengthScoreViewModel: StrengthScoreViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return StrengthScoreViewModel(repository, preferencesRepository) as T
            }
        }
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(repository, SavedStateHandle()) as T
            }
        }
    )

    val workoutViewModel: WorkoutViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WorkoutViewModel(repository, SavedStateHandle()) as T
            }
        }
    )

    val analyticsViewModel: AnalyticsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AnalyticsViewModel(repository) as T
            }
        }
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository, preferencesRepository, strengthScoreViewModel) as T
            }
        }
    )

    val libraryViewModel: LibraryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LibraryViewModel(repository, SavedStateHandle()) as T
            }
        }
    )

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository, preferencesRepository) as T
            }
        }
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route in listOf("dashboard", "analytics", "profile", "library")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.Black) {
                    val items = listOf(
                        Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
                        Triple("library", "Library", Icons.Default.CollectionsBookmark),
                        Triple("analytics", "Analytics", Icons.Default.QueryStats),
                        Triple("profile", "Profile", Icons.Default.Person)
                    )
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFFF851B),
                                selectedTextColor = Color(0xFFFF851B),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp)),
            enterTransition = {
                fadeIn(tween(180)) + slideInHorizontally(
                    initialOffsetX = { it / 5 }, animationSpec = tween(180, easing = EaseOut))
            },
            exitTransition = {
                fadeOut(tween(130)) + slideOutHorizontally(
                    targetOffsetX = { -it / 5 }, animationSpec = tween(130, easing = EaseIn))
            },
            popEnterTransition = {
                fadeIn(tween(180)) + slideInHorizontally(
                    initialOffsetX = { -it / 5 }, animationSpec = tween(180, easing = EaseOut))
            },
            popExitTransition = {
                fadeOut(tween(130)) + slideOutHorizontally(
                    targetOffsetX = { it / 5 }, animationSpec = tween(130, easing = EaseIn))
            }
        ) {
            composable("dashboard") {
                DashboardScreen(
                    onStartWorkout = { 
                        workoutViewModel.startWorkout("Quick Session")
                        navController.navigate("workout") 
                    },
                    onContinueWorkout = { id ->
                        workoutViewModel.resumeWorkout(id)
                        navController.navigate("workout")
                    },
                    onStartTemplate = { template ->
                        workoutViewModel.startWorkout(template.name)
                        navController.navigate("workout")
                    },
                    onConfigureSchedule = {
                        navController.navigate("weekly_schedule")
                    },
                    viewModel = dashboardViewModel
                )
            }
            composable("weekly_schedule") {
                WeeklyScheduleScreen(
                    viewModel = dashboardViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("analytics") {
                AnalyticsScreen(
                    viewModel = analyticsViewModel,
                    onViewHistory = { /* Tab handles this now */ }
                )
            }
            composable("profile") {
                ProfileScreen(viewModel = profileViewModel)
            }
            composable("library") {
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onStartTemplate = { template ->
                        workoutViewModel.startWorkout(template.name)
                        navController.navigate("workout")
                    },
                    onEditTemplate = { template ->
                        workoutViewModel.resumeWorkout(template.id)
                        navController.navigate("template_edit")
                    },
                    onCreateTemplate = {
                        workoutViewModel.startWorkout("New Routine")
                        navController.navigate("template_edit")
                    },
                    onCreateExercise = {
                        navController.navigate("create_exercise")
                    }
                )
            }
            composable("create_exercise") {
                CreateCustomExerciseScreen(
                    onSave = { exercise ->
                        libraryViewModel.insertExercise(exercise)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "exercise_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                val exerciseDetailViewModel: ExerciseDetailViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return ExerciseDetailViewModel(repository, SavedStateHandle(mapOf("id" to id))) as T
                        }
                    }
                )
                ExerciseDetailScreen(viewModel = exerciseDetailViewModel, onBack = { navController.popBackStack() })
            }
            composable("template_edit") {
                TemplateEditScreen(
                    viewModel = workoutViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("workout") {
                WorkoutScreen(
                    onFinish = { id -> 
                        navController.navigate("session_summary/$id") {
                            popUpTo("dashboard") { inclusive = false }
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    viewModel = workoutViewModel
                )
            }
            composable(
                route = "session_summary/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                val sessionSummaryViewModel: SessionSummaryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return SessionSummaryViewModel(repository, SavedStateHandle(mapOf("sessionId" to sessionId))) as T
                        }
                    }
                )
                SessionSummaryScreen(
                    sessionId = sessionId,
                    navController = navController,
                    viewModel = sessionSummaryViewModel
                )
            }
        }
    }
}
