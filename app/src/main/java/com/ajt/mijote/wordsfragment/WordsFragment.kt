package com.ajt.mijote.wordsfragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ajt.mijote.model.Events
import com.ajt.mijote.R
import com.ajt.mijote.extensions.add
import com.ajt.mijote.extensions.tintIcons
import com.ajt.mijote.extensions.appViewModel
import com.ajt.mijote.gamefragment.GameFragment
import kotlinx.android.synthetic.main.layout_words_fragment.*
import org.greenrobot.eventbus.EventBus

class WordsFragment : Fragment() {

    private val adapter by lazy { WordsAdapter(layoutInflater, requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.layout_words_fragment, container, false)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //listView.preserveFocusAfterLayout = false
        listView.adapter = adapter
        appViewModel.observableWords.observe(this, Observer {
            loadingTextView.isVisible = false
            adapter.update(it)
            setHasOptionsMenu(true)
            activity?.invalidateOptionsMenu()
            (activity as? AppCompatActivity)?.supportActionBar?.subtitle = getString(R.string.nWords, it.size)
        })
        appViewModel.triggerFetch()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        //menu.clear()
        with(appViewModel) {
            val wordsAvailable = wordsAvailable
            with(menu) {
                add(R.string.newWord) { appViewModel.addWord() }.apply {
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    setIcon(R.drawable.add)
                }
                if (wordsAvailable) {
                    add(R.string.game) { EventBus.getDefault().post(Events.AddFragment(GameFragment())) }
                    add(R.string.deleteAll) { showDeleteAllWordsDialog() }
                    add(R.string.includeAll) { toggleIncludedExcluded(true) }
                    add(R.string.excludeAll) { toggleIncludedExcluded(false) }
                }
                tintIcons(requireContext())
            }
        }
    }

    private fun showDeleteAllWordsDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setCancelable(false)
            setTitle(R.string.deleteAllWordsPrompt)
            setPositiveButton(R.string.delete) { _, _ -> appViewModel.deleteAllWords() }
            setNegativeButton(android.R.string.cancel, null)
        }.show()
    }
}