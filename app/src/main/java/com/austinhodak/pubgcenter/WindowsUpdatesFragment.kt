package com.austinhodak.pubgcenter


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import net.idik.lib.slimadapter.SlimAdapter
import net.idik.lib.slimadapter.SlimInjector
import java.text.SimpleDateFormat
import java.util.Locale


class WindowsUpdatesFragment : Fragment() {

    private lateinit var adapter: SlimAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_twitter, container, false)

        view.findViewById<RecyclerView>(R.id.rv).layoutManager = LinearLayoutManager(activity)

        setupAdapter(view.findViewById(R.id.rv))

        val urlString = "https://steamcommunity.com/games/578080/rss/"
        val parser = Parser()
        parser.execute(urlString)
        parser.onFinish(object : Parser.OnTaskCompleted {

            override fun onTaskCompleted(list: ArrayList<Article>) {
                //what to do when the parsing is done
                //the Array List contains all article's data. For example you can use it for your adapter.

                for (article in list) {
                    Log.d("RSS", article.title)
                }

                adapter.updateData(list)
                adapter.notifyDataSetChanged()

                view.findViewById<ProgressBar>(R.id.pg).visibility = View.GONE
            }

            override fun onError() {
                //what to do in case of error
                Log.d("RSS", "ERROR")
            }
        })

        return view
    }

    private fun setupAdapter(view: RecyclerView) {
        adapter = SlimAdapter.create().register(R.layout.update_rss_card, SlimInjector { data: Article, injector ->
            Locale.setDefault(Locale.getDefault())
            val date = data.pubDate
            var sdf = SimpleDateFormat()
            sdf = SimpleDateFormat("dd MMMM yyyy")
            val pubDateString = sdf.format(date)

            injector.text(R.id.title, data.title)
            injector.text(R.id.date, pubDateString)

            injector.clicked(R.id.card, View.OnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data.link))
                startActivity(intent)
            })

        }).attachTo(view)
    }
}
