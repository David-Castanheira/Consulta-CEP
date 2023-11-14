package br.com.consultacep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import br.com.consultacep.ui.theme.ConsultaCEPTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultaCEPTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConsultaCEP()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultaCEP() {
    var cep by remember { mutableStateOf("01311100") }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var cepInfoList by remember { mutableStateOf(emptyList<Pair<String, Date>>()) }

    Surface (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Informe o CEP para Busca"
            )
            Spacer(
                modifier = Modifier
                    .padding(16.dp)
            )
            TextField(
                value = cep,
                onValueChange = {
                    if(it.isDigitsOnly()) { cep = it }
                }
                , label = {Text ("CEP")}
                , modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .background(Color.Gray)
                    .padding(2.dp)
                ,  keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    if (cep.length == 8) {
                        isLoading = true
                        findAddress(cep) { result ->
                            address = result
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text(text = "Buscar Endereço")
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(text = address)

                // Implementação futura de exibição de histórico dos CEPS consultados.
                //LazyColumn(
                //    modifier = Modifier.fillMaxSize(),
                //    verticalArrangement = Arrangement.spacedBy(16.dp)
                //) {
                //    items(cepInfoList) { (cep, date) ->
                //        CepInfo(cep = cep, date = date)
                //    }
                //}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConsultaCEPreview() {
    ConsultaCEPTheme {
        ConsultaCEP()
    }
}


private fun findAddress(cep: String, callback: (String) -> Unit) {
    val baseUrl = "https://viacep.com.br/ws/"
    val url = "$baseUrl$cep/json/"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val json = withContext(Dispatchers.IO) {
                URL(url).readText()
            }
            val data = JSONObject(json)
            if (!("erro" in json)) {
                val address = data.getString("logradouro")
                val city = data.getString("localidade")
                val state = data.getString("uf")
                val fullAddress = "$address, $city - $state"
                callback(fullAddress)
            } else {
                callback("CEP não encontrado.")
            }
        } catch (e: Exception) {
            callback("Erro na busca do CEP: ${e.message}")
        }
    }
}


// Implementação futura de exibição de histórico dos CEPS consultados.
@Composable
fun CepInfo(
    cep: String,
    date: Date
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "CEP: $cep")
        Text(text = "Data da Consulta: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date)}")
    }
}