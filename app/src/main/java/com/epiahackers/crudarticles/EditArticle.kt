package com.epiahackers.crudarticles

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.epiahackers.crudarticles.database.Article
import com.epiahackers.crudarticles.database.ArticleRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*
import pl.droidsonroids.gif.GifImageView

class EditArticle : AppCompatActivity() {

    private lateinit var etCode: EditText
    private lateinit var spFamily: Spinner
    private lateinit var etPrice: EditText
    private lateinit var etStock: EditText
    private lateinit var etDescription: EditText

    private lateinit var repository: ArticleRepository

    private lateinit var resourceValuesFamily: Array<String>

    private lateinit var loadingGif: GifImageView

    private lateinit var tvPriceIva: TextView

    var article: Article? = null

    private data class conditionError(val cond: () -> Boolean, val errorMessage: String)
    private data class fieldValueError(val idValue: Int, val idError: Int, val idErrorIcon: Int)

    private fun notNullCondition(id: Int) = conditionError({ findViewById<EditText>(id).text.toString().trim().isEmpty() }, getString(R.string.emptyFieldError))
    private fun greater0Condiditon(id: Int) = conditionError({ findViewById<EditText>(id).text.toString().toDouble() <= 0.0 }, getString(R.string.greater0Error))

    private var errors = false

    private val INSTANCE_CHEKED = "CHECKED"

    private lateinit var conditionToPass: Map<fieldValueError, Array<conditionError>>


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
    companion object{

        val EXTRA_ARTICLE_CODE = "ARTICLE_CODE"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_article)

        supportActionBar?.hide()

        etCode = findViewById(R.id.etCode)
        spFamily = findViewById(R.id.spFamily)
        etPrice = findViewById(R.id.etPrice)
        etStock = findViewById(R.id.etStock)
        etDescription = findViewById(R.id.etDescription)

        loadingGif = findViewById(R.id.givLoading)

        resourceValuesFamily = Article.getFamilyValues(this).let { it.apply { reverse() } }.plus(getString(R.string.family_empty)).apply { reverse() }

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resourceValuesFamily)
        arrayAdapter.setDropDownViewResource((android.R.layout.simple_spinner_dropdown_item))
        spFamily.adapter = arrayAdapter

        repository = ArticleRepository((applicationContext as App).db.articleDao())

        val extraArticleCode = intent.extras!!.getString(EXTRA_ARTICLE_CODE)!!

        loadData(extraArticleCode)

        buildConditions()

        tvPriceIva = findViewById(R.id.tvPriceIvaForm)

        val etPrice = findViewById<EditText>(R.id.etPrice)

        EditTextLimitDecimals(etPrice)

        tvPriceIva.text = calculateIva(etPrice.text.toString().toDouble()).toString().replace(".", getString(R.string.decimalSeparator)) + " " + getString(R.string.moneySymbol)

        etPrice.addTextChangedListener {

            tvPriceIva.text = (if(it == null || it.isEmpty()) "0" else calculateIva(it.toString().toDouble()) .toString().replace(".", getString(R.string.decimalSeparator))) + " " + getString(R.string.moneySymbol)

        }

    }

    private fun calculateIva(price: Double) = (Math.round(
        price * 1.21 * 100
    ) / 100.0)

    private fun buildConditions(){

        if(this::conditionToPass.isInitialized) return

        conditionToPass = mapOf(

            fieldValueError(R.id.etPrice, R.id.tvPriceError, R.id.iconPriceError) to arrayOf(
                notNullCondition(R.id.etPrice), greater0Condiditon(R.id.etPrice)
            ),
            fieldValueError(R.id.etDescription, R.id.tvDescError, R.id.iconDescError) to arrayOf(
                notNullCondition(R.id.etDescription)
            )
        )

        findViewById<Button>(R.id.btnCancel).setOnClickListener{
            setResult(RESULT_CANCELED)
            finish()
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener{

            confirmDelete()

        }

        findViewById<Button>(R.id.btnSave).setOnClickListener{

            checkForm()

            if(!errors) updateArticle()

        }

    }

    fun checkForm(){

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

    fun confirmDelete(){
        DialogCustom.createDialogButtons(
            this,
            R.layout.buttons_dialog_delete,
            getString(R.string.delete_title),
            getString(R.string.delete_message),
            R.id.btnDeleteDialog,
            {

                val context = this

                runBlocking {
                    async { repository.delete(article!!) }.await()
                }

                GlobalScope.launch {

                    if(repository.getByCode(article!!.codi) != null){

                        SnackbarCustom.showSnackbarError(context, getString(R.string.deleteArticleError))

                    }else{

                        val intent: Intent = Intent()

                        intent.putExtra(MainActivity.EXTRA_MESSAGE_OK,
                            getString(R.string.deleteArticleCorrect)
                        )

                        setResult(RESULT_OK, intent)

                        finish()

                    }

                }
            },
            R.id.btnCancelDialog,
            {_ -> })
            .show()

    }

    private  fun updateArticle() {

        checkDecimals(etPrice)

        val family = spFamily.selectedItem.toString().let { if(it == getString(R.string.family_empty)) null else it }
        val price = etPrice.text.toString().toDouble()
        val description = etDescription.text.toString().trim()

        val context = this


        runBlocking {

            startLoading()

            async {
                repository!!.update(Article(article!!.codi, description, family, price, article!!.estoc))
            }.await()

            endLoading()

            GlobalScope.launch {


                if(!repository?.getByCode(article!!.codi).let { it!!.familia == family && it.preuSenseIVA == price && it.descripcio == description }) SnackbarCustom.showSnackbarError(context, getString(R.string.updateArticleError))
                else{
                    val intent: Intent = Intent()

                    intent.putExtra(MainActivity.EXTRA_MESSAGE_OK,
                        getString(R.string.updateArticleCorrect)
                    )

                    setResult(RESULT_OK, intent)
                    finish()
                }
            }

        }

    }

    private fun loadData(code: String){

        runBlocking {
            article = withContext(Dispatchers.Default) {
                repository.getByCode(code)
            }
        }

        if(article == null) SnackbarCustom.showSnackbarError(this,  getString(R.string.loadingArticleError))

        etCode.setText(article?.codi ?: "")
        etPrice.setText(article?.preuSenseIVA?.toString() ?: "0")
        etStock.setText(article?.estoc?.toString() ?: "0")
        spFamily.setSelection(resourceValuesFamily.indexOfFirst { it == article?.familia ?: "" })
        etDescription.setText(article?.descripcio ?: "")


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