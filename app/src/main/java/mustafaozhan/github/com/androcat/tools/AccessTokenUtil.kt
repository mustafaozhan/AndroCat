package mustafaozhan.github.com.androcat.tools

import android.content.Context
import mustafaozhan.github.com.androcat.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class AccessTokenUtil(private val context: Context, url: String) : Callback {
    companion object {
        const val QUERY_CLIENT_ID = "client_id"
        const val QUERY_CLIENT_SECRET = "client_secret"
        const val QUERY_CODE = "code"
        const val ACCESS_TOKEN = "access_token"
        const val HEADER = "Accept"
        const val HEADER_JSON = "application/json"
    }

    init {
        handleAccessToken(url)
    }

    private fun handleAccessToken(url: String) {
        context.apply {

            val tokenCode = url.substring(
                url.lastIndexOf(getString(R.string.query_param_code)
                ) + 1)
                .split("=".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()

            val cleanToken = tokenCode[1]
                .split("&".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()

            val code = cleanToken[0]

            val urlOauth = HttpUrl.parse(getString(R.string.url_github_access_token))
                ?.newBuilder()
                ?.addQueryParameter(QUERY_CLIENT_ID, getString(R.string.client_id))
                ?.addQueryParameter(QUERY_CLIENT_SECRET, getString(R.string.client_secret))
                ?.addQueryParameter(QUERY_CODE, code)
                ?.build()
                .toString()

            val request = Request.Builder()
                .header(HEADER, HEADER_JSON)
                .url(urlOauth)
                .build()

            OkHttpClient().newCall(request).enqueue(this@AccessTokenUtil)
        }
    }

    override fun onFailure(call: Call, e: IOException) = Unit

    override fun onResponse(call: Call, response: Response) {
        if (response.isSuccessful) {
            JSONObject(response.body().toString()).getString(ACCESS_TOKEN).let {
                GeneralSharedPreferences().updateUser(token = it)
            }
        }
    }
}
