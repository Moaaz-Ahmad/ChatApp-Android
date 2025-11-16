package com.studyconnect.app.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.studyconnect.app.ui.navigation.StudyConnectNavGraph
import com.studyconnect.app.ui.theme.StudyConnectTheme

@Composable
fun StudyConnectApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    StudyConnectTheme {
        StudyConnectNavGraph(
            navController = navController,
            snackbarHostState = snackbarHostState,
            modifier = modifier
        )
    }
}
