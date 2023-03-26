package com.epiahackers.crudarticles

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.epiahackers.crudarticles.database.Article
import java.lang.Math.round

class ArticleAdapter(var articles: MutableList<Article>, val context: Context) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>(){

    lateinit var clickArticleListener: (String) -> Unit

    private var colorsFamily = mapOf<String?, Int>(
        context.getString(R.string.family_hardware) to context.getColor(R.color.light_blue),
        context.getString(R.string.family_software) to context.getColor(R.color.purple_200),
        context.getString(R.string.family_default) to context.getColor(R.color.light_brown),
        null to context.getColor(R.color.bg_card_article)
        )

    private fun colorStock(stock: Double) = when(stock){

        0.0 -> context.getColor(R.color.red)

        in 0.1..3.0 -> context.getColor(R.color.yellow)

        else -> context.getColor(R.color.black)

    }

    constructor(articles: MutableList<Article>, context: Context, clickArticleListener: (String) -> Unit) : this(articles, context){
        this.clickArticleListener = clickArticleListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(layoutInflater.inflate(R.layout.recycle_row_element, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = articles[position]
        holder.bind(item, context)
    }

    override fun getItemCount() = articles.size

    fun update(articles: List<Article>){
        this.articles = articles.let { if(it.isEmpty()) mutableListOf() else it as MutableList<Article> }
        this.notifyDataSetChanged()
    }

    fun removeAt(position: Int){

        articles.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, articles.size)

    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        val code = view.findViewById<TextView>(R.id.tvCode)
        val desc = view.findViewById<TextView>(R.id.tvDesc)
        val family = view.findViewById<TextView>(R.id.tvFamily)
        val price = view.findViewById<TextView>(R.id.tvPrice)
        val priceIva = view.findViewById<TextView>(R.id.tvPriceIva)
        val stock = view.findViewById<TextView>(R.id.tvStock)
        val card = view.findViewById<CardView>(R.id.cardView)

        var articleBinded: Article? = null

        fun bind(article: Article, context: Context){

            articleBinded = article

            code.text = article.codi
            desc.text = article.descripcio.toString().replace("\n", " ").replace("\t", " ")
            family.text = article.familia ?: context.getString(R.string.family_empty)
            price.text = (round(article.preuSenseIVA * 100) / 100.0).toString().replace(".", context.getString(R.string.decimalSeparator)) + context.getString(R.string.moneySymbol)
            priceIva.text = (round(article.preuSenseIVA * 1.21 * 100) / 100.0).toString().replace(".", context.getString(R.string.decimalSeparator)) + context.getString(R.string.moneySymbol)
            stock.text = (round(article.estoc * 100) / 100.0).toString().replace(".", context.getString(R.string.decimalSeparator))

            card.setCardBackgroundColor(colorsFamily[article.familia] ?: context.getColor(R.color.white))

            stock.setTextColor(colorStock(article.estoc))

            itemView.setOnClickListener{
                clickArticleListener(article.codi)
            }

        }

        fun open(){
            clickArticleListener(articleBinded!!.codi)
        }

    }
}