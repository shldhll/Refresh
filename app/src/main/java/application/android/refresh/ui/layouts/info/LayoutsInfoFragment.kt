package application.android.refresh.ui.layouts.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import application.android.refresh.R
import application.android.refresh.data.db.entity.Layout
import kotlinx.android.synthetic.main.fragment_cards_info.*
import kotlinx.android.synthetic.main.fragment_layouts_info.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class LayoutsInfoFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()
    private val viewModelFactory: LayoutsInfoViewModelFactory by
    instance<LayoutsInfoViewModelFactory>()

    private lateinit var viewModel: LayoutsInfoViewModel
    private val args: LayoutsInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_layouts_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LayoutsInfoViewModel::class.java)

        setupUI()
    }

    private fun setupUI() {
        setupToolbar()
        val layoutId = args.layoutId

        // Check for default arg value
        if (layoutId == 0L) {
            findNavController().navigateUp()
            Toast.makeText(context, "Choose a layout to view", Toast.LENGTH_SHORT).show()
        }
        viewModel.layoutDetails(layoutId).observe(viewLifecycleOwner, Observer { l ->
            l?.let { layout ->
                viewModel.layoutName.postValue("${layout.name} Layout")
                viewModel.layout = layout
                setFields()
                setToolbarMenu()
                viewModel.cardCount()
            }
        })
    }

    private fun setupToolbar() {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_layouts,
                R.id.navigation_cards,
                R.id.navigation_routines
            )
        )
        layoutsInfoToolbar.setupWithNavController(navController, appBarConfiguration)
        viewModel.layoutName.observe(viewLifecycleOwner, Observer {
            layoutsInfoToolbar.title = it
        })
    }

    private fun setToolbarMenu() {
        viewModel.layout?.let { layout ->
            layoutsInfoToolbar.inflateMenu(R.menu.info_menu)
            val menu = layoutsInfoToolbar.menu
            menu.findItem(R.id.action_edit).setOnMenuItemClickListener {
                val layoutsUpdateAction =
                    LayoutsInfoFragmentDirections.layoutsUpdateAction(layout.id)
                findNavController().navigate(layoutsUpdateAction)
                return@setOnMenuItemClickListener true
            }

            menu.findItem(R.id.action_delete).setOnMenuItemClickListener {
                confirmDeleteDialog()
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun setFields() {
        viewModel.layout?.let { layout ->
            layoutsInfoFront.text = layout.front
            layoutsInfoBack.text = layout.back

            if (!layout.backExtra.isBlank()) {
                layoutsInfoBackExtraTitle.visibility = View.VISIBLE
                layoutsInfoBackExtra.visibility = View.VISIBLE
                layoutsInfoBackExtra.text = layout.backExtra
            } else {
                layoutsInfoBackExtraTitle.visibility = View.GONE
                layoutsInfoBackExtra.visibility = View.GONE
            }
        }

        viewModel.count.observe(viewLifecycleOwner, Observer {
            layoutsInfoCount.text = "$it"
        })
    }

    private fun confirmDeleteDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Layout")
        builder.setMessage("This will also delete all the associated cards")
            .setPositiveButton(
                "Delete"
            ) { dialog, _ ->
                viewModel.deleteLayout()
                viewModel.isOkayToExit.observe(viewLifecycleOwner, Observer {
                    if (it) {
                        findNavController().navigateUp()
                    }
                })
                dialog.dismiss()
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create()
        builder.show()
    }
}