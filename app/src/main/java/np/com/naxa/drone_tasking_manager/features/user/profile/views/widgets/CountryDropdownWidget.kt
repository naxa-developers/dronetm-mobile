package np.com.naxa.drone_tasking_manager.features.user.profile.views.widgets

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent

@Composable
fun CountryDropdown(
    selectedCountry: String?,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

LaunchedEffect (selectedCountry){
    selectedCountry.let {
        searchQuery = it ?: ""
    }
}

    Log.d("TAG", "CountryDropdown: $selectedCountry")

    // Sample list of countries - you can expand this
    val countries = remember {
        countryList()
    }


    val filteredCountries = countries.filter {
        it.lowercase().contains(searchQuery.lowercase())
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                isExpanded = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) isExpanded = true },
            label = { Text("Country") },
            trailingIcon = {
                Row {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, "Clear search")
                        }
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            if (isExpanded) Icons.Filled.KeyboardArrowUp
                            else Icons.Filled.KeyboardArrowDown,
                            "Toggle dropdown"
                        )
                    }
                }
            }
        )

        if (isExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                shadowElevation = 4.dp
            ) {
                LazyColumn {
                    items(filteredCountries) { country ->
                        Text(
                            text = country,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCountrySelected(country)
                                    searchQuery = country
                                    isExpanded = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}