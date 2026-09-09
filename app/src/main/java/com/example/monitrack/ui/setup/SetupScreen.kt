package com.example.monitrack.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.monitrack.R
import com.example.monitrack.data.entity.ActivityConfig
import com.example.monitrack.data.Profile
import com.example.monitrack.data.enums.Sex
import com.example.monitrack.ui.common.ActivityConfigCard
import com.example.monitrack.ui.common.ProfileSection
import com.example.monitrack.ui.viewmodel.SetupViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(viewModel: SetupViewModel = viewModel()) {
    var configs by remember { mutableStateOf(ActivityConfig.defaults()) }
    var bodyFatEnabled by remember { mutableStateOf(false) }
    var sex by remember { mutableStateOf<Sex?>(null) }
    var height by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf<LocalDate?>(null) }

    val profileValid = !bodyFatEnabled ||
        (sex != null && height.toDoubleOrNull() != null && dob != null)

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.setup_title)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.setup_intro), style = MaterialTheme.typography.bodyMedium)

            configs.forEach { config ->
                ActivityConfigCard(config) { updated ->
                    configs = configs.map { if (it.type == updated.type) updated else it }
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
                        configs,
                        Profile(
                            setupComplete = true,
                            bodyFatEnabled = bodyFatEnabled,
                            sex = sex.takeIf { bodyFatEnabled },
                            heightCm = height.toDoubleOrNull().takeIf { bodyFatEnabled },
                            dateOfBirth = dob.takeIf { bodyFatEnabled },
                        ),
                    )
                },
                enabled = profileValid,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.setup_finish)) }
        }
    }
}
