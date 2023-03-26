package com.epiahackers.crudarticles

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.epiahackers.crudarticles.database.Article
import com.epiahackers.crudarticles.database.ArticleRepository
import com.google.android.material.internal.TextWatcherAdapter
import kotlinx.coroutines.*
import pl.droidsonroids.gif.GifImageView
import java.lang.Math.round

class NewArticle : AppCompatActivity() {

    private data class conditionError(val cond: () -> Boolean, val errorMessage: String)
    private data class fieldValueError(val idValue: Int, val idError: Int, val idErrorIcon: Int)

    private fun notNullCondition(id: Int) = conditionError({ findViewById<EditText>(id).text.toString().trim().isEmpty() }, getString(R.string.emptyFieldError))
    private fun greater0Condiditon(id: Int) = conditionError({ findViewById<EditText>(id).text.toString().toDouble() <= 0.0 }, getString(R.string.greater0Error))
    private fun greaterEqual0Condiditon(id: Int) = conditionError({ findViewById<EditText>(id).text.toString().toDouble() < 0.0 }, getString(R.string.greaterEqual0Error))

    private fun EditTextLimitDecimals(et: EditText, decimals: Int = 2){

        et.setOnFocusChangeListener { v, hasFocus ->

            if(!hasFocus) checkDecimals(et, decimals)

        }

    }

    private fun checkDecimals(et: EditText, decimals: Int = 2){

        val text = et.text.toString()

        if(text.contains(".") && text.split(".")[1].length > decimals)
            et.setText(text.substring(0, text.indexOf(".") + 3))

    }

    private lateinit var tvPriceIva: TextView

    private var repository: ArticleRepository? = null

    private lateinit var loadingGif: GifImageView

    private lateinit var conditionToPass: Map<fieldValueError, Array<conditionError>>

    private var errors = false

    private val INSTANCE_CHEKED = "CHECKED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_article)

        supportActionBar?.hide()

        repository = ArticleRepository((applicationContext as App).db.articleDao())
        loadingGif = findViewById(R.id.givLoading)

        val spFamily: Spinner = findViewById(R.id.spFamily)

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Article.getFamilyValues(this).let { it.apply { reverse() } }.plus(getString(R.string.family_empty)).apply { reverse() })
        arrayAdapter.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item))
        spFamily.adapter = arrayAdapter

        buildCondition()

        tvPriceIva = findViewById(R.id.tvPriceIvaForm)

        tvPriceIva.text = "0" + getString(R.string.decimalSeparator) + "0 " + getString(R.string.moneySymbol)

        val etPrice = findViewById<EditText>(R.id.etPrice)

        EditTextLimitDecimals(etPrice)
        EditTextLimitDecimals(findViewById(R.id.etStock))

        etPrice.addTextChangedListener {

            tvPriceIva.text = (if(it == null || it.isEmpty()) "0" else (round(it.toString().toDouble() * 1.21 * 100) / 100.0).toString().replace(".", getString(R.string.decimalSeparator))) + " " + getString(R.string.moneySymbol)

        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener{
            setResult(RESULT_CANCELED)
            finish()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener{

            checkForm()

            if(!errors) createArticle()

        }

    }

    private fun buildCondition(){

        if(this::conditionToPass.isInitialized) return

        conditionToPass = mapOf(
            fieldValueError(R.id.etCode, R.id.tvCodeError, R.id.iconCodeError) to arrayOf(
                notNullCondition(R.id.etCode), conditionError({

                    var res: Article?

                    runBlocking {
                        startLoading()
                        res = async { repository!!.getByCodeAsync(findViewById<EditText>(R.id.etCode).text.toString()) }.await()
                        endLoading()
                    }

                    res != null}, getString(R.string.codeExistsError))
            ),
            fieldValueError(R.id.etPrice, R.id.tvPriceError, R.id.iconPriceError) to arrayOf(
                notNullCondition(R.id.etPrice), greater0Condiditon(R.id.etPrice)
            ),
            fieldValueError(R.id.etStock, R.id.tvStockError, R.id.iconStockError) to arrayOf(
                notNullCondition(R.id.etStock), greaterEqual0Condiditon(R.id.etStock)
            ),
            fieldValueError(R.id.etDescription, R.id.tvDescError, R.id.iconDescError) to arrayOf(
                notNullCondition(R.id.etDescription)
            ),
        )

    }

    private fun checkForm(){

        errors = false

        for((key, value) in conditionToPass){

            var message: String? = null

            if(value.any() {
                    val result = it.cond()

                    if(result) message = it.errorMessage

                    result
                }){

                errors = true

                findViewById<TextView>(key.idError).text = message
                findViewById<ImageView>(key.idErrorIcon).visibility = View.VISIBLE

            }else{
                findViewById<TextView>(key.idError).text = ""
                findViewById<ImageView>(key.idErrorIcon).visibility = View.INVISIBLE
            }

        }

    }

    private fun createArticle(){

        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etStock = findViewById<EditText>(R.id.etStock)

        checkDecimals(etPrice)
        checkDecimals(etStock)

        val code = findViewById<EditText>(R.id.etCode).text.toString().trim()
        val family = findViewById<Spinner>(R.id.spFamily).selectedItem.toString().let { if(it == getString(R.string.family_empty)) null else it }
        val price = etPrice.text.toString().toDouble()
        val stock = findViewById<EditText>(R.id.etStock).text.toString().toDouble()
        val description = findViewById<EditText>(R.id.etDescription).text.toString().trim()

        val context = this


        runBlocking {

            startLoading()

            async {
                repository!!.insert(Article(code, description, family, price, stock))
            }.await()

            endLoading()

            GlobalScope.launch {
                if(repository?.getLastId() != code) SnackbarCustom.showSnackbarError(context, getString(R.string.insertArticleError))
                else{
                    val intent: Intent = Intent()

                    intent.putExtra(MainActivity.EXTRA_MESSAGE_OK,
                        getString(R.string.insertArticleCorrect)
                    )

                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

        }


    }

    fun startLoading(){
        loadingGif.visibility = View.VISIBLE
    }

    fun endLoading(){
        loadingGif.visibility = View.INVISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putBoolean(INSTANCE_CHEKED, errors)

        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {

        super.onRestoreInstanceState(savedInstanceState)

        val checked = savedInstanceState.getBoolean(INSTANCE_CHEKED)

        if(checked) checkForm()

    }
}