package com.brokenstrawapps.battlebuddy.map

import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.brokenstrawapps.battlebuddy.R
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import kotlinx.android.synthetic.main.activity_map_downloader.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.toast
import java.io.File


class MapDownloadActivity : AppCompatActivity() {

    private lateinit var fetch: Fetch
    private lateinit var downloads: List<Download>
    private var buttonMaps: MutableMap<Map, Button> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_downloader)

        buttonMaps[Map.ERANGEL_LOW] = erangelLow
        buttonMaps[Map.ERANGEL_HIGH] = erangelHigh
        buttonMaps[Map.MIRAMAR_LOW] = miramarLow
        buttonMaps[Map.MIRAMAR_HIGH] = miramarHigh
        buttonMaps[Map.SANHOK_LOW] = sanhokLow
        buttonMaps[Map.SANHOK_HIGH] = sanhokHigh
        buttonMaps[Map.VIKENDI_LOW] = vikendiLow
        buttonMaps[Map.VIKENDI_HIGH] = vikendiHigh
        buttonMaps[Map.KARAKIN_LOW] = karakinLow
        buttonMaps[Map.KARAKIN_HIGH] = karakinHigh

        toolbar.setNavigationOnClickListener { onBackPressed() }
        toolbar.navigationIconResource = R.drawable.ic_arrow_back_24dp

        val fetchConfiguration = FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(4)
                .build()

        fetch = Fetch.getInstance(fetchConfiguration)

        fetch.addListener(object : FetchListener {
            override fun onAdded(download: Download) {
            }

            override fun onCancelled(download: Download) {
                updateButtons(download)
            }

            override fun onCompleted(download: Download) {
                updateButtons(download)
            }

            override fun onDeleted(download: Download) {
                updateButtons(download)
            }

            override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
            }

            override fun onPaused(download: Download) {
                updateButtons(download)
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                updateButtons(download)
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                updateButtons(download)
            }

            override fun onRemoved(download: Download) {
                updateButtons(download)
            }

            override fun onResumed(download: Download) {
                updateButtons(download)
            }

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                updateButtons(download)
            }

            override fun onWaitingNetwork(download: Download) {

            }
        })

        setupInitialListeners()

        fetch.getDownloads(Func {
            //Access all downloads here
            for (download in it) {
                updateButtons(download)
            }
        })
    }

    private fun setupInitialListeners() {
        for (item in buttonMaps) {
            val map = item.key
            val button = item.value

            button.setOnClickListener {
                fetch.enqueue(getDownloadRequest(map))
            }
        }
    }

    private fun updateButtons(download: Download) {
        val map = Map.values()[download.identifier.toInt()]
        download.status

        when (download.status) {
            Status.COMPLETED -> {
                buttonMaps[map]?.text = "DOWNLOADED"
                buttonMaps[map]?.isEnabled = true
                buttonMaps[map]?.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_green_500))
            }
            Status.QUEUED -> {
                buttonMaps[map]?.text = "QUEUED"
                buttonMaps[map]?.isEnabled = false
            }
            Status.REMOVED,
            Status.DELETED -> {
                buttonMaps[map]?.text = "DOWNLOAD"
                buttonMaps[map]?.isEnabled = true
            }
            Status.FAILED -> {
                buttonMaps[map]?.text = "FAILED"
                buttonMaps[map]?.isEnabled = true
            }
            Status.DOWNLOADING -> {
                buttonMaps[map]?.text = "DOWNLOADING... ${download.progress}%"
                buttonMaps[map]?.isEnabled = false
            }
            else -> {
                buttonMaps[map]?.text = "DOWNLOAD"
                buttonMaps[map]?.isEnabled = true
            }
        }

        updateListeners(download)
    }

    private fun updateListeners(download: Download) {
        val map = Map.values()[download.identifier.toInt()]
        val button = buttonMaps[map]

        when (download.status) {
            Status.COMPLETED -> {
                button?.setOnClickListener(null)
                button?.setOnLongClickListener {
                    fetch.delete(download.id)
                    buttonMaps[map]?.text = "DOWNLOAD"

                    toast("Deleted Map")

                    button.setOnClickListener {
                        fetch.enqueue(getDownloadRequest(map))
                        button.setOnLongClickListener(null)
                    }
                    true
                }
            }
            Status.QUEUED -> {
                button?.setOnClickListener(null)
                button?.setOnLongClickListener(null)
            }
            Status.REMOVED,
            Status.DELETED,
            Status.FAILED -> {
                button?.setOnLongClickListener(null)
                button?.setOnClickListener {
                    fetch.enqueue(getDownloadRequest(map))
                }
            }
            Status.DOWNLOADING -> {
                button?.setOnClickListener(null)
                button?.setOnLongClickListener(null)
            }
            else -> {
                button?.setOnLongClickListener(null)
                button?.setOnClickListener {
                    fetch.enqueue(getDownloadRequest(map))
                }
            }
        }
    }

    private fun getDownloadRequest(map: Map) : Request {
        val request = Request("https://github.com/pubg/api-assets/raw/master/Assets/Maps/${map.fileName}", Uri.fromFile(File(filesDir, map.fileName)))
        request.priority = Priority.HIGH
        request.networkType = NetworkType.ALL
        request.identifier = map.ordinal.toLong()
        return request
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(R.anim.nav_default_pop_enter_anim, R.anim.nav_default_pop_exit_anim)
    }

    override fun onDestroy() {
        super.onDestroy()
        fetch.close()
    }
}

enum class Map (var fileName: String) {
    ERANGEL_LOW("Erangel_Main_Low_Res.png"),
    ERANGEL_HIGH("Erangel_Main_High_Res.png"),
    MIRAMAR_LOW("Miramar_Main_Low_Res.png"),
    MIRAMAR_HIGH("Miramar_Main_High_Res.png"),
    SANHOK_LOW("Sanhok_Main_Low_Res.png"),
    SANHOK_HIGH("Sanhok_Main_High_Res.png"),
    VIKENDI_LOW("Vikendi_Main_Low_Res.png"),
    VIKENDI_HIGH("Vikendi_Main_High_Res.png"),
    KARAKIN_LOW("Karakin_Main_Low_Res.png"),
    KARAKIN_HIGH("Karakin_Main_High_Res.png")
}