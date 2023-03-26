package com.epiahackers.crudarticles

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epiahackers.crudarticles.database.Article
import com.epiahackers.crudarticles.database.ArticleRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArticleAdapter
    private lateinit var articles: MutableList<Article>
    private lateinit var repository: ArticleRepository

    private var filterDescription = false
    private var filterStock = false

    private var searchBy: MenuItem? = null

    private lateinit var itemsSearch: Array<String>

    private var checkedItem = 0
    private lateinit var fieldSearch: String

    private lateinit var searchView: SearchView

    private var resultArticle = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if(result.resultCode == Activity.RESULT_OK){

            val data: Intent? = result.data

            SnackbarCustom.showSnackBarCorrect(this, data!!.getStringExtra(EXTRA_MESSAGE_OK)!!)

            updateFilter()

        }

    }

    private var miOrderType: MenuItem? = null

    private var orderBy: OrderFields = OrderFields.RECENTS
        set(value){
            field = value

            updateFilter()
        }

    private var orderAsc: Boolean = true
        set(value){
            field = value

            miOrderType?.icon = getDrawable((if(orderAsc) R.drawable.ic_baseline_order_asc_24 else R.drawable.ic_baseline_order_desc_24))



            adapter.update(adapter.articles.reversed())

        }

    companion object{

        val EXTRA_MESSAGE_OK = "MESSAGE_OK"

        fun convertDpToPixel(dp: Float, context: Context?): Float {
            return if (context != null) {
                val resources = context.resources
                val metrics = resources.displayMetrics
                dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            } else {
                val metrics = Resources.getSystem().displayMetrics
                dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        setContentView(R.layout.activity_main)

        itemsSearch = arrayOf(getString(R.string.descriptionTitle), getString(R.string.codeTitle))

        fieldSearch = itemsSearch[checkedItem]

        repository = ArticleRepository((applicationContext as App).db.articleDao())

        val context = this;

        recyclerView = findViewById(R.id.rvArticles)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val helper: ItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(
                    0,
                    ItemTouchHelper.END or ItemTouchHelper.START
                )
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when(direction){

                    ItemTouchHelper.END -> {

                        val article = (viewHolder as ArticleAdapter.ViewHolder).articleBinded

                        val dialog = DialogCustom.createDialogButtons(
                            context,
                            R.layout.buttons_dialog_delete,
                            getString(R.string.delete_title),
                            getString(R.string.delete_message),
                            R.id.btnDeleteDialog,
                            {

                                runBlocking {
                                    async { repository.delete(article!!) }.await()

                                    if(async { repository.getByCodeAsync(article!!.codi) }.await() != null){

                                        SnackbarCustom.showSnackbarError(context, getString(R.string.deleteArticleError))

                                    }else{

                                        SnackbarCustom.showSnackBarCorrect(context, getString(R.string.deleteArticleCorrect))

                                        adapter.removeAt(viewHolder.adapterPosition)

                                    }

                                }
                            },
                            R.id.btnCancelDialog,
                            {
                                adapter.notifyDataSetChanged()
                            })

                        dialog.setOnCancelListener {
                            adapter.notifyDataSetChanged()
                        }

                        dialog.show()

                    }

                    ItemTouchHelper.START -> {
                        (viewHolder as ArticleAdapter.ViewHolder).open()
                        adapter.notifyDataSetChanged()
                    }

                }
            }

            private fun slideRight(c: Canvas, dX: Float, itemView: View){

                val separationBottom = 16f

                val itemHeight = itemView.bottom - itemView.top

                val background = ColorDrawable(Color.RED)

//                background.setBounds(
//                    itemView.right + dX.toInt(),
//                    itemView.top,
//                    itemView.right,
//                    itemView.bottom
//                )

                background.setBounds(0, itemView.top,   itemView.left + dX.toInt(), itemView.bottom - convertDpToPixel(separationBottom, context).toInt());
                background.draw(c)

                val icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_baseline_delete_forever_24)

                val inHeight = itemHeight / 3
                val inWidth = inHeight

                val iconMargin = (itemHeight - inHeight) / 2
                val iconTop = itemView.top + (itemHeight - inHeight) / 2 - convertDpToPixel(separationBottom/2, context).toInt()

                // Calculate position of delete icon
//                val iconLeft = itemView.right - iconMargin - inWidth
//                val iconRight = itemView.right - iconMargin
//                val iconBottom = iconTop + inHeight

                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + inWidth
                val iconBottom = iconTop + inHeight

                icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                icon?.draw(c)


            }

            private fun slideLeft(c: Canvas, dX: Float, itemView: View){

                val separationBottom = 16f

                val itemHeight = itemView.bottom - itemView.top

                val background = ColorDrawable(getColor(R.color.green))

                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom - convertDpToPixel(separationBottom, context).toInt()
                )

//                background.setBounds(0, itemView.top,   itemView.left + dX.toInt(), itemView.bottom - convertDpToPixel(32f, context).toInt());
                background.draw(c)

                val icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_baseline_mode_edit_black_24)

                val inHeight = itemHeight / 3
                val inWidth = inHeight

                val iconMargin = (itemHeight - inHeight) / 2
                val iconTop = itemView.top + (itemHeight - inHeight) / 2 - convertDpToPixel(separationBottom/2, context).toInt()

                // Calculate position of delete icon
                val iconLeft = itemView.right - iconMargin - inWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + inHeight

//                val iconLeft = itemView.left + iconMargin
//                val iconRight = itemView.left + iconMargin + inWidth
//                val iconBottom = iconTop + inHeight

                icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                icon?.draw(c)


            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                val itemView = viewHolder.itemView

                if(dX > 0) slideRight(c, dX, itemView)
                else slideLeft(c, dX, itemView)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        })

        helper.attachToRecyclerView(recyclerView)

        runBlocking {
            buildAdapter(context)
        }

        findViewById<FloatingActionButton>(R.id.addArticle).setOnClickListener{

            val intent = Intent(this, NewArticle::class.java)

            resultArticle.launch(intent)

        }

    }

    private suspend fun buildAdapter(context: Context){

        GlobalScope.launch {

            updateFilter()

            adapter = ArticleAdapter(articles, context) {

                val intent = Intent(context, EditArticle::class.java)

                intent.putExtra(EditArticle.EXTRA_ARTICLE_CODE, it)

                resultArticle.launch(intent)

            }

//            adapter = ArticleAdapter(articles?.value ?: arrayListOf<Article>(), context)

        }.join()

        recyclerView.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_actionbar_main, menu)

        searchBy = menu?.findItem(R.id.miSearchBy)

        searchBy?.title = fieldSearch

        miOrderType = menu?.findItem(R.id.miOrderType)

        val searchItem: MenuItem? = menu?.findItem(R.id.miSearch)

        searchView = MenuItemCompat.getActionView(searchItem) as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                search(p0)

                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                return true;
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                search(p0)
                return true
            }

        })


        searchView.setOnSearchClickListener {
            searchBy?.isVisible = true
            miOrderType?.isVisible = false
        }
        searchView.setOnCloseListener {
            searchBy?.isVisible = false
            miOrderType?.isVisible = true
            false
        }

        when(orderBy){
            OrderFields.RECENTS -> menu?.findItem(R.id.miOrderRecents)
            OrderFields.CODE -> menu?.findItem(R.id.miOrderCode)
            OrderFields.PRICE -> menu?.findItem(R.id.miOrderPrice)
            OrderFields.STOCK -> menu?.findItem(R.id.miOrderStock)
        }!!.isChecked = true

        return super.onCreateOptionsMenu(menu)
    }

    fun search(q: String?){

        val query = q?.trim()?.lowercase()

        if(query == null || query!!.trim().isEmpty()){
            updateFilter()
            return
        }

        updateFilter(query = q)

    }


    private fun updateFilter(query: String? = null){

        var elements: MutableList<Article> = mutableListOf()

        runBlocking { elements = async { repository.getArticlesAsync() }.await() }

        if((this::searchView.isInitialized && !searchView.isIconified) || query != null) runBlocking {

            val elementsFilter = async{

                when(fieldSearch){

                    getString(R.string.descriptionTitle) -> repository.searchByDescription( searchView.query.toString())
                    getString(R.string.codeTitle) -> repository.searchByCode( searchView.query.toString())
                    else -> repository.searchByCode( searchView.query.toString())

                }


            }.await()
            elements.retainAll(elementsFilter)

        }

        if(filterStock) runBlocking {
            val elementsFilter = (async{ repository.filterByStock() }.await())
            elements.retainAll(elementsFilter)
        }

        articles = elements

        elements = orderList(elements) as MutableList<Article>

        if(this::adapter.isInitialized) adapter.update(elements)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){

            R.id.miFilterStock -> {

                item.isChecked = !item.isChecked

                filterStock = item.isChecked

                updateFilter()

            }

            R.id.miSearchBy -> {

                DialogCustom.dialogRadioSelector(this, getString(R.string.search_by), getString(R.string.cancel), getString(R.string.search), { pos, text ->
                    checkedItem = pos
                    fieldSearch = text
                    searchBy?.title = text
                    searchBy?.tooltipText = text
                    search(searchView.query.toString())
                }, checkedItem, *itemsSearch).show()

            }

            R.id.miOrderCode -> {
                orderBy = OrderFields.CODE
                item.isChecked = true
            }
            R.id.miOrderRecents -> {
                orderBy = OrderFields.RECENTS
                item.isChecked = true
            }
            R.id.miOrderPrice -> {
                orderBy = OrderFields.PRICE
                item.isChecked = true
            }
            R.id.miOrderStock -> {
                orderBy = OrderFields.STOCK
                item.isChecked = true
            }
            R.id.miOrderType -> orderAsc = !orderAsc

        }


        return super.onOptionsItemSelected(item)
    }

    fun orderList(list: List<Article> = articles): List<Article>{

        var listShow = when(orderBy){
            OrderFields.RECENTS -> articles.apply { reverse() } //TEST
            OrderFields.CODE -> list.sortedBy { it.codi }
            OrderFields.PRICE -> list.sortedBy { it.preuSenseIVA }
            OrderFields.STOCK -> list.sortedBy { it.estoc }
        }

        return if(orderAsc) listShow else listShow.reversed()
    }

    private enum class OrderFields {
        CODE, RECENTS, PRICE, STOCK
    }

}