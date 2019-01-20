package com.austinh.battlebuddy.stats.matchdetails

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.otaliastudios.zoom.ZoomApi
import com.austinh.battlebuddy.R
import com.austinh.battlebuddy.models.MatchParticipant
import com.austinh.battlebuddy.viewmodels.MatchDetailViewModel
import com.austinh.battlebuddy.viewmodels.models.MatchModel
import de.blox.graphview.BaseGraphAdapter
import de.blox.graphview.Graph
import de.blox.graphview.Node
import de.blox.graphview.tree.BuchheimWalkerAlgorithm
import de.blox.graphview.tree.BuchheimWalkerConfiguration
import kotlinx.android.synthetic.main.fragment_kill_tree.*


class KillTreeFragment : Fragment() {

    var match: MatchModel? = null

    private val viewModel: MatchDetailViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(MatchDetailViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_kill_tree, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val graph = Graph()

        val adapter = object : BaseGraphAdapter<ViewHolder>(requireContext(), R.layout.tree_kill_item, graph) {
            @NonNull
            override fun onCreateViewHolder(view: View): ViewHolder {
                return ViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: ViewHolder, data: Any, position: Int) {
                //val participant = data as TreeItem
                viewHolder.mTextView.text = data.toString()

                if (match?.currentPlayer?.attributes?.stats?.name == data.toString()) {
                    viewHolder.mTextView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.rankBronze))
                } else {
                    viewHolder.mTextView.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.timelineGrey))
                }
            }
        }
        kill_tree_graph?.adapter = adapter
        kill_tree_graph?.setMaxZoom(3f, ZoomApi.TYPE_REAL_ZOOM)
        kill_tree_graph?.lineThickness = getDp(4f)

        viewModel.mMatchData.observe(this, Observer { match ->
            this.match = match
            val nodeList: MutableList<Node> = ArrayList()
            for (player in match.participantList) {
                nodeList.add(Node(player.attributes.stats.name))
            }

            var killList = match.killFeedList.reversed().filterNot { it.killer.accountId == it.victim.accountId }
            for (kill in killList) {
                if (kill.killer.name.isEmpty()) {
                    //Kill was bluezone or not player, remove from list.
                    killList = killList.filterNot { it.victim.accountId == kill.victim.accountId }
                }
            }

            for (kill in killList) {
                if (kill.killer.name.isNotEmpty() && kill.victim.name.isNotEmpty()) {
                    var killerNode = nodeList.find { it.data == kill.killer.name }!!
                    var victimNode = nodeList.find { it.data == kill.victim.name }!!

                    Log.d("KILL", "${killList.indexOf(kill)} -- ${graph.hasPredecessor(killerNode)} --- ${killerNode?.data} - ${victimNode?.data}")

                    if (killList.indexOf(kill) == 0) {
                        graph.addEdge(Node(kill.killer.name), Node(kill.victim.name))
                    } else {
                        if (graph.hasPredecessor(killerNode) || graph.getNode(0) == killerNode) {
                            graph.addEdge(killerNode, victimNode)
                        }
                    }
                }
            }

            val configuration = BuchheimWalkerConfiguration.Builder()
                    .setSiblingSeparation(100)
                    .setLevelSeparation(300)
                    .setSubtreeSeparation(300)
                    .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_LEFT_RIGHT)
                    .build()
            adapter.algorithm = BuchheimWalkerAlgorithm(configuration)
        })
    }

    private inner class ViewHolder internal constructor(view: View) {
        internal var mTextView: TextView = view.findViewById(R.id.tree_text)
    }

    data class TreeItem(
            var participant: MatchParticipant
    )

    fun getDp(dp: Float): Int {
        val r = requireContext().resources
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.displayMetrics
        ).toInt()
    }
}