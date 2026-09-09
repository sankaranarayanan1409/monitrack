package com.example.monitrack.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monitrack.R
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.Profile
import com.example.monitrack.data.enums.Sex
import com.example.monitrack.ui.common.ActivityConfigCard
import com.example.monitrack.ui.common.ProfileSection
import com.example.monitrack.ui.viewmodel.SettingsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel()) {
    val loadedConfigs by viewModel.configs.collectAsState()
    val loadedProfile by viewModel.profile.collectAsState()

    var configs by remember { mutableStateOf<List<ActivityConfig>?>(null) }
    var bodyFatEnabled by remember { mutableStateOf(false) }
    var sex by remember { mutableStateOf<Sex?>(null) }
    var height by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf<LocalDate?>(null) }
    var profileSeeded by remember { mutableStateOf(false) }

    // Seed each part once its own flow first delivers data. Seeding them independently avoids
    // a race where the profile arrives after the configs and its fields (e.g. bodyFatEnabled)
    // never get applied — which would let a save silently reset them.
    LaunchedEffect(loadedConfigs) {
        if (configs == null && loadedConfigs.isNotEmpty()) configs = loadedConfigs
    }
    LaunchedEffect(loadedProfile) {
        if (!profileSeeded) {
            loadedProfile?.let { profile ->
                bodyFatEnabled = profile.bodyFatEnabled
                sex = profile.sex
                height = profile.heightCm?.toString() ?: ""
                dob = profile.dateOfBirth
                profileSeeded = true
            }
        }
    }


    val editable = configs
    val profileValid = !bodyFatEnabled ||
        (sex != null && height.toDoubleOrNull() != null && dob != null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_back), contentDescription = stringResource(R.string.cancel))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (editable != null) {
                editable.forEach { config ->
                    ActivityConfigCard(config) { updated ->
                        configs = editable.map { if (it.type == updated.type) updated else it }
                    }
                }
                ProfileSection(
                    bodyFatEnabled = bodyFatEnabled,
                    onBodyFatEnabledChange = { bodyFatEnabled = it },
                    sex = sex,
                    onSexChange = { sex = it },
                    height = height,
                    onHeightChange = { height = it },
                    dateOfBirth = dob,
                    onDateOfBirthChange = { dob = it },
                )
                Button(
                    onClick = {
                        viewModel.save(
                            editable,
                            Profile(
                                setupComplete = true,
                                bodyFatEnabled = bodyFatEnabled,
                                sex = sex.takeIf { bodyFatEnabled },
                                heightCm = height.toDoubleOrNull().takeIf { bodyFatEnabled },
                                dateOfBirth = dob.takeIf { bodyFatEnabled },
                            ),
                        )
                        onBack()
                    },
                    enabled = profileValid,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.save)) }
            }
        }
    }
}
