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
    var cep by remember {
        mutableStateOf("01311100")
    }

    var address by remember {
        mutableStateOf("")
    }

    //Adicionar as outras variáveis by remember como acima para os outros campos

    var isLoading by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(16.dp)
        )
        {
            Text(
                text = "Digite o CEP para consulta"
            )
            TextField(
                value = cep,
                onValueChange = {
                    if (it.isDigitsOnly()) {
                        cep = it
                    }
                }
                , label = {Text ("CEP")}
                , modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
                    .background(Color.Gray)
                    .padding(2.dp)
                , keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            
            Button(onClick = {
                if(cep.length == 8) {
                    isLoading = true
                    findAdress(cep) {
                        result -> address = result
                        isLoading = false
                    }
                }
               // , enabled = !isLoading
            }) {
                Text(text = "Consultar CEP")
            }

            if(isLoading) {
                CircularProgressIndicator()
            }
            else {
                Text(text = address)
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

fun findAdress(cep: String, callback: (String) -> Unit) {
    val baseURL = "https://viacep.com.br/ws/$cep/json/"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val json = withContext((Dispatchers.IO)) {
                URL(baseURL).readText()
            }
            delay(5000L)
            if(!("erro" in json)) {
                val data = JSONObject(json)
                val address = data.getString("logradouro")
                val neighborhood = data.getString("bairro")
                val city = data.getString("localidade")
                val state = data.getString("uf")

                //Verificar se abaixo está correto
                val fullAddress = "$address - $city - $neighborhood - $state"
                callback(fullAddress)
            }
            else {
                callback("CEP não encontrado")
            }
        }
        catch (e: Exception) {
            callback("Erro na busca do CEP: ${e.message}")
        }
    }
}